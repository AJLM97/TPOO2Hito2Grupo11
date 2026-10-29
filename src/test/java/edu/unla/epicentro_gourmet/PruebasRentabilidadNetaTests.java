package edu.unla.epicentro_gourmet;

import edu.unla.epicentro_gourmet.dtos.ReporteRentabilidadDTO;
import edu.unla.epicentro_gourmet.entities.Cocinero;
import edu.unla.epicentro_gourmet.entities.Costos;
import edu.unla.epicentro_gourmet.entities.EnumCategoriaCocinero;
import edu.unla.epicentro_gourmet.entities.EnumTemporadaFestival;
import edu.unla.epicentro_gourmet.entities.Festival;
import edu.unla.epicentro_gourmet.entities.ItemPedido;
import edu.unla.epicentro_gourmet.entities.Pedido;
import edu.unla.epicentro_gourmet.entities.Plato;
import edu.unla.epicentro_gourmet.entities.PuestoDesarmable;
import edu.unla.epicentro_gourmet.entities.UnidadVenta;
import edu.unla.epicentro_gourmet.repositories.IEmpleadoRepository;
import edu.unla.epicentro_gourmet.repositories.IPedidoRepository;
import edu.unla.epicentro_gourmet.services.EnumResultadoRangoFechas;
import edu.unla.epicentro_gourmet.services.implementation.CanonService;
import edu.unla.epicentro_gourmet.services.implementation.LiquidacionService;
import edu.unla.epicentro_gourmet.services.implementation.RentabilidadService;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class PruebasRentabilidadNetaTests {
    private static final LocalDate INICIO = LocalDate.of(2026, 11, 1);
    private static final LocalDate FIN = LocalDate.of(2026, 11, 20);

    private final IPedidoRepository pedidoRepository = mock(IPedidoRepository.class);
    private final IEmpleadoRepository empleadoRepository = mock(IEmpleadoRepository.class);
    private final RentabilidadService rentabilidadService = new RentabilidadService(pedidoRepository,
            new LiquidacionService(empleadoRepository), new CanonService());

    // Festival de 20 días: el cocinero cobra 100% de (100.000 + 150.000) y el canon del puesto de 20 m2 y 45 min es 9.550.
    private final Costos costos = new Costos(500, 10, 2000, 50000, 100000, 150000, 5000, 100000);
    private final Festival festival = new Festival("Festival de prueba", EnumTemporadaFestival.PRIMAVERA,
            INICIO, FIN, costos);
    private final UnidadVenta unidad = new PuestoDesarmable("Puesto propio", "PD-0000001", 20, festival, 2, 45, null);
    private final Plato empanada = new Plato("Empanada", 3500, 1200, unidad);

    private Pedido pedidoCerrado(int cantidadDeEmpanadas) {
        Pedido pedido = new Pedido(LocalDateTime.of(2026, 11, 5, 12, 0), unidad);
        pedido.setCerrado(true);
        pedido.getItems().add(new ItemPedido(empanada, pedido, cantidadDeEmpanadas));
        return pedido;
    }

    private void conUnCocinero() {
        when(empleadoRepository.findByUnidadAndActivoTrue(unidad)).thenReturn(List.of(
                new Cocinero("Lucia", "Fernandez", 30123456, LocalDate.of(1995, 3, 12), LocalDate.of(2016, 1, 1),
                        unidad, EnumCategoriaCocinero.COCINERO, null)));
    }

    @Test
    void testRentabilidadRestaCostoDePlatosSueldosYCanon() {
        conUnCocinero();
        when(pedidoRepository.findByUnidadVentaAndCerradoTrue(unidad)).thenReturn(List.of(pedidoCerrado(200)));

        ReporteRentabilidadDTO reporte = rentabilidadService.rentabilidad(unidad).orElseThrow();

        assertEquals(700000.0, reporte.getIngresos());
        assertEquals(240000.0, reporte.getCostoPlatos());
        assertEquals(460000.0, reporte.getGananciaBruta());
        assertEquals(250000.0, reporte.getSueldos());
        assertEquals(9550.0, reporte.getCanon());
        assertEquals(200450.0, reporte.getRentabilidadNeta());
        assertNull(reporte.getDesde());
        assertNull(reporte.getHasta());
    }

    @Test
    void testSinVentasLaRentabilidadEsNegativaPorSueldosYCanon() {
        conUnCocinero();
        when(pedidoRepository.findByUnidadVentaAndCerradoTrue(unidad)).thenReturn(List.of());

        ReporteRentabilidadDTO reporte = rentabilidadService.rentabilidad(unidad).orElseThrow();

        assertEquals(-259550.0, reporte.getRentabilidadNeta());
    }

    @Test
    void testEntreFechasUsaLosPedidosDelRangoYRestaLosCostosCompletos() {
        conUnCocinero();
        LocalDate desde = LocalDate.of(2026, 11, 1);
        LocalDate hasta = LocalDate.of(2026, 11, 10);
        when(pedidoRepository.findByUnidadVentaAndCerradoTrueAndFechaTransaccionBetween(unidad,
                desde.atStartOfDay(), hasta.atTime(LocalTime.MAX))).thenReturn(List.of(pedidoCerrado(100)));

        ReporteRentabilidadDTO reporte = rentabilidadService.rentabilidad(unidad, desde, hasta).orElseThrow();

        assertEquals(350000.0, reporte.getIngresos());
        assertEquals(120000.0, reporte.getCostoPlatos());
        assertEquals(250000.0, reporte.getSueldos());
        assertEquals(9550.0, reporte.getCanon());
        assertEquals(350000.0 - 120000 - 250000 - 9550, reporte.getRentabilidadNeta());
        assertEquals(desde, reporte.getDesde());
        assertEquals(hasta, reporte.getHasta());
    }

    @Test
    void testFestivalSinCostosNoTieneRentabilidad() {
        Festival sinCostos = new Festival("Sin costos", EnumTemporadaFestival.VERANO, INICIO, FIN, null);
        UnidadVenta unidadSinCostos = new PuestoDesarmable("Puesto", "PD-0000002", 20, sinCostos, 2, 45, null);
        when(pedidoRepository.findByUnidadVentaAndCerradoTrue(unidadSinCostos)).thenReturn(List.of());

        assertTrue(rentabilidadService.rentabilidad(unidadSinCostos).isEmpty());
    }

    @Test
    void testRangoDentroDelFestivalEsValidoIncluyendoLosExtremos() {
        assertEquals(EnumResultadoRangoFechas.OK, rentabilidadService.validarRango(unidad, INICIO, FIN));
        assertEquals(EnumResultadoRangoFechas.OK,
                rentabilidadService.validarRango(unidad, LocalDate.of(2026, 11, 5), LocalDate.of(2026, 11, 5)));
    }

    @Test
    void testRangoConDesdePosteriorAHastaEsInvalido() {
        assertEquals(EnumResultadoRangoFechas.DESDE_POSTERIOR_A_HASTA,
                rentabilidadService.validarRango(unidad, LocalDate.of(2026, 11, 10), LocalDate.of(2026, 11, 5)));
    }

    @Test
    void testRangoFueraDelFestivalEsInvalido() {
        assertEquals(EnumResultadoRangoFechas.FUERA_DEL_FESTIVAL,
                rentabilidadService.validarRango(unidad, INICIO.minusDays(1), LocalDate.of(2026, 11, 5)));
        assertEquals(EnumResultadoRangoFechas.FUERA_DEL_FESTIVAL,
                rentabilidadService.validarRango(unidad, LocalDate.of(2026, 11, 5), FIN.plusDays(1)));
    }

    @Test
    void testRangoSinUnaDeLasFechasEsInvalido() {
        assertEquals(EnumResultadoRangoFechas.FALTA_FECHA, rentabilidadService.validarRango(unidad, INICIO, null));
        assertEquals(EnumResultadoRangoFechas.FALTA_FECHA, rentabilidadService.validarRango(unidad, null, FIN));
    }

    @Test
    void testCalcularConUnRangoInvalidoLanzaExcepcion() {
        assertThrows(IllegalArgumentException.class, () -> rentabilidadService.rentabilidad(unidad,
                LocalDate.of(2026, 11, 10), LocalDate.of(2026, 11, 5)));
    }
}
