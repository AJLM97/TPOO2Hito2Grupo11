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
import org.hibernate.Hibernate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

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

    private ReporteVentaDTO armar(String festival, String unidad, String codigo, List<Pedido> pedidos) {
        long platos = pedidos.stream()
                .flatMap(pedido -> pedido.getItems().stream())
                .mapToLong(ItemPedido::getCantidad)
                .sum();
        double recaudacion = pedidos.stream().mapToDouble(Pedido::getTotal).sum();
        return new ReporteVentaDTO(festival, unidad, codigo, pedidos.size(), platos, recaudacion);
    }
}
