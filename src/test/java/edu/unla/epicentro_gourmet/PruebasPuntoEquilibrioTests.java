package edu.unla.epicentro_gourmet;

import edu.unla.epicentro_gourmet.dtos.PuntoEquilibrioDTO;
import edu.unla.epicentro_gourmet.dtos.ReporteRentabilidadDTO;
import edu.unla.epicentro_gourmet.entities.EnumTemporadaFestival;
import edu.unla.epicentro_gourmet.entities.Festival;
import edu.unla.epicentro_gourmet.entities.PuestoDesarmable;
import edu.unla.epicentro_gourmet.entities.UnidadVenta;
import edu.unla.epicentro_gourmet.services.EnumEstadoFestival;
import edu.unla.epicentro_gourmet.services.IRentabilidadService;
import edu.unla.epicentro_gourmet.services.implementation.PuntoEquilibrioService;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class PruebasPuntoEquilibrioTests {
    private static final LocalDate INICIO = LocalDate.of(2026, 11, 1);
    private static final LocalDate FIN = LocalDate.of(2026, 11, 10);
    private static final LocalDate HOY = LocalDate.of(2026, 11, 5);

    private final IRentabilidadService rentabilidadService = mock(IRentabilidadService.class);
    private final PuntoEquilibrioService puntoEquilibrioService = new PuntoEquilibrioService(rentabilidadService);

    private final Festival festival = new Festival("Festival de prueba", EnumTemporadaFestival.PRIMAVERA, INICIO, FIN, null);
    private final UnidadVenta unidad = new PuestoDesarmable("Puesto propio", "PD-0000001", 20, festival, 2, 45, null);

    // Canon 9.550 y sueldos 250.000: hay que cubrir 259.550.
    private PuntoEquilibrioDTO calcular(double ventas, double costoPlatos, double sueldos, double canon) {
        when(rentabilidadService.rentabilidad(unidad)).thenReturn(Optional.of(new ReporteRentabilidadDTO(
                "Festival de prueba", "Puesto propio", "PD-0000001", null, null,
                ventas, costoPlatos, sueldos, canon, ventas - costoPlatos - sueldos - canon)));
        return puntoEquilibrioService.calcular(unidad, HOY).orElseThrow();
    }

    @Test
    void testCuandoLaGananciaBrutaCubreLosCostosElEquilibrioEstaAlcanzado() {
        PuntoEquilibrioDTO equilibrio = calcular(700000, 240000, 250000, 9550);

        assertEquals(259550.0, equilibrio.getCostosFijos());
        assertEquals(460000.0, equilibrio.getGananciaBruta());
        assertTrue(equilibrio.isEquilibrioAlcanzado());
        assertEquals(0.0, equilibrio.getGananciaFaltante());
        assertEquals(460000.0 / 259550.0 * 100, equilibrio.getPorcentajeCubierto(), 0.0001);
        assertEquals(0.0, equilibrio.getVentasFaltantes());
    }

    @Test
    void testCuandoNoAlcanzaSeInformaCuantoFaltaGanarYVender() {
        PuntoEquilibrioDTO equilibrio = calcular(100000, 40000, 250000, 9550);

        assertFalse(equilibrio.isEquilibrioAlcanzado());
        assertEquals(259550.0 - 60000, equilibrio.getGananciaFaltante());
        assertEquals(60000.0 / 259550.0 * 100, equilibrio.getPorcentajeCubierto(), 0.0001);
        // Con un margen de 60% hacen falta 259.550 / 0,6 de ventas.
        assertEquals(259550.0 / 0.6, equilibrio.getVentasNecesarias(), 0.01);
        assertEquals(259550.0 / 0.6 - 100000, equilibrio.getVentasFaltantes(), 0.01);
    }

    @Test
    void testSinVentasNoSePuedeEstimarLoQueHayQueVender() {
        PuntoEquilibrioDTO equilibrio = calcular(0, 0, 250000, 9550);

        assertEquals(0.0, equilibrio.getPorcentajeCubierto());
        assertEquals(259550.0, equilibrio.getGananciaFaltante());
        assertNull(equilibrio.getVentasNecesarias());
        assertNull(equilibrio.getVentasFaltantes());
    }

    @Test
    void testConGananciaBrutaNegativaElPorcentajeCubiertoEsCero() {
        PuntoEquilibrioDTO equilibrio = calcular(10000, 30000, 250000, 9550);

        assertEquals(0.0, equilibrio.getPorcentajeCubierto());
        assertNull(equilibrio.getVentasNecesarias());
    }

    @Test
    void testSinCostosFijosElEquilibrioEstaCubierto() {
        PuntoEquilibrioDTO equilibrio = calcular(0, 0, 0, 0);

        assertTrue(equilibrio.isEquilibrioAlcanzado());
        assertEquals(100.0, equilibrio.getPorcentajeCubierto());
    }

    @Test
    void testElEstadoDelFestivalDependeDeLaFechaDeHoy() {
        when(rentabilidadService.rentabilidad(unidad)).thenReturn(Optional.of(new ReporteRentabilidadDTO(
                "Festival de prueba", "Puesto propio", "PD-0000001", null, null, 0, 0, 0, 0, 0)));

        assertEquals(EnumEstadoFestival.PROXIMO, puntoEquilibrioService.calcular(unidad, INICIO.minusDays(1)).orElseThrow().getEstadoFestival());
        assertEquals(EnumEstadoFestival.EN_CURSO, puntoEquilibrioService.calcular(unidad, INICIO).orElseThrow().getEstadoFestival());
        assertEquals(EnumEstadoFestival.EN_CURSO, puntoEquilibrioService.calcular(unidad, FIN).orElseThrow().getEstadoFestival());
        assertEquals(EnumEstadoFestival.FINALIZADO, puntoEquilibrioService.calcular(unidad, FIN.plusDays(1)).orElseThrow().getEstadoFestival());
    }

    @Test
    void testSinRentabilidadCalculableNoHayPuntoDeEquilibrio() {
        when(rentabilidadService.rentabilidad(unidad)).thenReturn(Optional.empty());

        assertTrue(puntoEquilibrioService.calcular(unidad, HOY).isEmpty());
    }
}
