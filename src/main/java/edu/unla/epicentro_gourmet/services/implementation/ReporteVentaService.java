package edu.unla.epicentro_gourmet.services.implementation;

import edu.unla.epicentro_gourmet.dtos.ReporteVentaDTO;
import edu.unla.epicentro_gourmet.dtos.RankingUnidadVentaDTO;
import edu.unla.epicentro_gourmet.entities.ItemPedido;
import edu.unla.epicentro_gourmet.entities.Pedido;
import edu.unla.epicentro_gourmet.entities.UnidadVenta;
import edu.unla.epicentro_gourmet.repositories.IFestivalRepository;
import edu.unla.epicentro_gourmet.repositories.IPedidoRepository;
import edu.unla.epicentro_gourmet.repositories.IUnidadVentaRepository;
import edu.unla.epicentro_gourmet.services.IReporteVentaService;
import edu.unla.epicentro_gourmet.dtos.TemporadaRecaudacionDTO;
import edu.unla.epicentro_gourmet.entities.EnumTemporadaFestival;
import edu.unla.epicentro_gourmet.entities.Festival;
import org.hibernate.Hibernate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.ArrayList;
import java.util.EnumMap;
import java.util.Map;
import java.time.LocalDate;

@Service("reporteVentaService")
public class ReporteVentaService implements IReporteVentaService {

    private final IPedidoRepository pedidoRepository;
    private final IFestivalRepository festivalRepository;
    private final IUnidadVentaRepository unidadVentaRepository;

    public ReporteVentaService(IPedidoRepository pedidoRepository, IFestivalRepository festivalRepository,
                               IUnidadVentaRepository unidadVentaRepository) {
        this.pedidoRepository = pedidoRepository;
        this.festivalRepository = festivalRepository;
        this.unidadVentaRepository = unidadVentaRepository;
    }

    // Solo se recauda con pedidos cerrados: uno abierto todavía puede cambiar o cancelarse.
    @Override
    @Transactional(readOnly = true)
    public ReporteVentaDTO reporteDeUnidad(UnidadVenta unidad) {
        UnidadVenta real = (UnidadVenta) Hibernate.unproxy(unidad);
        List<Pedido> pedidos = pedidoRepository.findByUnidadVentaAndCerradoTrue(real);
        return armar(real.getFestival().getNombre(), real.getNombreComercial(), real.getCodigo(), pedidos);
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<ReporteVentaDTO> reporteDeFestival(long idFestival) {
        return festivalRepository.findById(idFestival).map(festival -> armar(festival.getNombre(), null, null,
                pedidoRepository.findByFestival_IdAndCerradoTrue(idFestival)));
    }

    @Override
    @Transactional(readOnly = true)
    public List<RankingUnidadVentaDTO> rankingUnidadesPorRecaudacion(long idFestival) {
        return unidadVentaRepository.findRankingByRecaudacion(idFestival);
    }

    @Override
    @Transactional(readOnly = true)
    public List<TemporadaRecaudacionDTO> recaudacionPorTemporada(){
        Map<EnumTemporadaFestival, AcumuladoTemporada> acumulados = new EnumMap<>(EnumTemporadaFestival.class);

        for(EnumTemporadaFestival temporada : EnumTemporadaFestival.values()){
            acumulados.put(temporada, new AcumuladoTemporada());
        }

        LocalDate hoy = LocalDate.now();

        for(Festival festival : festivalRepository.findAll()){
            if(festival.getFechaFin().isBefore(hoy)){
                ReporteVentaDTO reporte = reporteDeFestival(festival.getId()).orElseThrow();
                AcumuladoTemporada acumulado = acumulados.get(festival.getTemporada());
                acumulado.totalRecaudado += reporte.getRecaudacion();
                acumulado.cantidadFestivales++;

                if (acumulado.festivalMayorRecaudacion == null
                        || reporte.getRecaudacion() > acumulado.mayorRecaudacion
                        || (reporte.getRecaudacion() == acumulado.mayorRecaudacion
                        && festival.getNombre().compareTo(acumulado.festivalMayorRecaudacion) < 0)) {
                    acumulado.mayorRecaudacion = reporte.getRecaudacion();
                    acumulado.festivalMayorRecaudacion = festival.getNombre();
                }
            }
        }

        List<TemporadaRecaudacionDTO> resultado = new ArrayList<>();
        for(EnumTemporadaFestival temporada : EnumTemporadaFestival.values()){
            AcumuladoTemporada acumulado = acumulados.get(temporada);
            double promedio = acumulado.cantidadFestivales == 0 ? 0 :
                    acumulado.totalRecaudado / acumulado.cantidadFestivales;
            resultado.add(new TemporadaRecaudacionDTO(temporada, promedio, acumulado.festivalMayorRecaudacion == null ?
                    "Sin festivales" : acumulado.festivalMayorRecaudacion));
        }
        return resultado;
    }

    private static class AcumuladoTemporada{
        private double totalRecaudado;
        private double mayorRecaudacion;
        private int cantidadFestivales;
        private String festivalMayorRecaudacion;
    }

    private ReporteVentaDTO armar(String festival, String unidad, String codigo, List<Pedido> pedidos) {
        long platos = pedidos.stream()
                .flatMap(pedido -> pedido.getItems().stream())
                .mapToLong(ItemPedido::getCantidad)
                .sum();
        double recaudacion = pedidos.stream().mapToDouble(Pedido::getTotal).sum();
        return new ReporteVentaDTO(festival, unidad, codigo, pedidos.size(), platos, recaudacion);
    }
}
