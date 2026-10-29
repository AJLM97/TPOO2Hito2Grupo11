package edu.unla.epicentro_gourmet.services.implementation;

import edu.unla.epicentro_gourmet.dtos.DetalleCanonDTO;
import edu.unla.epicentro_gourmet.dtos.LiquidacionHaberesDTO;
import edu.unla.epicentro_gourmet.dtos.ReporteRentabilidadDTO;
import edu.unla.epicentro_gourmet.entities.Festival;
import edu.unla.epicentro_gourmet.entities.ItemPedido;
import edu.unla.epicentro_gourmet.entities.Pedido;
import edu.unla.epicentro_gourmet.entities.UnidadVenta;
import edu.unla.epicentro_gourmet.repositories.IPedidoRepository;
import edu.unla.epicentro_gourmet.services.EnumResultadoRangoFechas;
import edu.unla.epicentro_gourmet.services.ICanonService;
import edu.unla.epicentro_gourmet.services.ILiquidacionService;
import edu.unla.epicentro_gourmet.services.IRentabilidadService;
import org.hibernate.Hibernate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.Optional;

@Service("rentabilidadService")
public class RentabilidadService implements IRentabilidadService {

    private final IPedidoRepository pedidoRepository;
    private final ILiquidacionService liquidacionService;
    private final ICanonService canonService;

    public RentabilidadService(IPedidoRepository pedidoRepository, ILiquidacionService liquidacionService,
                               ICanonService canonService) {
        this.pedidoRepository = pedidoRepository;
        this.liquidacionService = liquidacionService;
        this.canonService = canonService;
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<ReporteRentabilidadDTO> rentabilidad(UnidadVenta unidad) {
        UnidadVenta real = (UnidadVenta) Hibernate.unproxy(unidad);
        return armar(real, null, null, pedidoRepository.findByUnidadVentaAndCerradoTrue(real));
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<ReporteRentabilidadDTO> rentabilidad(UnidadVenta unidad, LocalDate desde, LocalDate hasta) {
        UnidadVenta real = (UnidadVenta) Hibernate.unproxy(unidad);
        EnumResultadoRangoFechas rango = validarRango(real, desde, hasta);
        if (rango != EnumResultadoRangoFechas.OK) {
            throw new IllegalArgumentException("Rango de fechas inválido: " + rango);
        }
        List<Pedido> pedidos = pedidoRepository.findByUnidadVentaAndCerradoTrueAndFechaTransaccionBetween(
                real, desde.atStartOfDay(), hasta.atTime(LocalTime.MAX));
        return armar(real, desde, hasta, pedidos);
    }

    @Override
    @Transactional(readOnly = true)
    public EnumResultadoRangoFechas validarRango(UnidadVenta unidad, LocalDate desde, LocalDate hasta) {
        if (desde == null || hasta == null) {
            return EnumResultadoRangoFechas.FALTA_FECHA;
        }
        if (desde.isAfter(hasta)) {
            return EnumResultadoRangoFechas.DESDE_POSTERIOR_A_HASTA;
        }
        UnidadVenta real = (UnidadVenta) Hibernate.unproxy(unidad);
        Festival festival = (Festival) Hibernate.unproxy(real.getFestival());
        if (desde.isBefore(festival.getFechInicio()) || hasta.isAfter(festival.getFechaFin())) {
            return EnumResultadoRangoFechas.FUERA_DEL_FESTIVAL;
        }
        return EnumResultadoRangoFechas.OK;
    }

    // Rentabilidad neta = pedidos totales - costo de los platos - sueldos - canon.
    private Optional<ReporteRentabilidadDTO> armar(UnidadVenta unidad, LocalDate desde, LocalDate hasta,
                                                    List<Pedido> pedidos) {
        Optional<List<LiquidacionHaberesDTO>> liquidaciones = liquidacionService.liquidarUnidad(unidad);
        Optional<DetalleCanonDTO> canon = canonService.calcular(unidad);
        if (liquidaciones.isEmpty() || canon.isEmpty()) {
            return Optional.empty();
        }

        double ingresos = pedidos.stream().mapToDouble(Pedido::getTotal).sum();
        double costoPlatos = pedidos.stream()
                .flatMap(pedido -> pedido.getItems().stream())
                .mapToDouble(this::costoDe)
                .sum();
        double sueldos = liquidaciones.get().stream().mapToDouble(LiquidacionHaberesDTO::getTotal).sum();
        double montoCanon = canon.get().getCanon();
        double neta = ingresos - costoPlatos - sueldos - montoCanon;

        return Optional.of(new ReporteRentabilidadDTO(canon.get().getNombreFestival(), unidad.getNombreComercial(),
                unidad.getCodigo(), desde, hasta, ingresos, costoPlatos, sueldos, montoCanon, neta));
    }

    private double costoDe(ItemPedido item) {
        return item.getPlato().getCostoProduccion() * item.getCantidad();
    }
}
