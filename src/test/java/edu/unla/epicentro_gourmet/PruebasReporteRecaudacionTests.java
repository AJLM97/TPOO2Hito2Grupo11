package edu.unla.epicentro_gourmet;

import edu.unla.epicentro_gourmet.dtos.ReporteVentaDTO;
import edu.unla.epicentro_gourmet.entities.EnumTemporadaFestival;
import edu.unla.epicentro_gourmet.entities.Festival;
import edu.unla.epicentro_gourmet.entities.ItemPedido;
import edu.unla.epicentro_gourmet.entities.Pedido;
import edu.unla.epicentro_gourmet.entities.Plato;
import edu.unla.epicentro_gourmet.entities.PuestoDesarmable;
import edu.unla.epicentro_gourmet.entities.UnidadVenta;
import edu.unla.epicentro_gourmet.repositories.IFestivalRepository;
import edu.unla.epicentro_gourmet.repositories.IPedidoRepository;
import edu.unla.epicentro_gourmet.repositories.IUnidadVentaRepository;
import edu.unla.epicentro_gourmet.services.implementation.ReporteVentaService;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class PruebasReporteRecaudacionTests {
    private final IPedidoRepository pedidoRepository = mock(IPedidoRepository.class);
    private final IFestivalRepository festivalRepository = mock(IFestivalRepository.class);
    private final IUnidadVentaRepository unidadVentaRepository = mock(IUnidadVentaRepository.class);
    private final ReporteVentaService reporteVentaService = new ReporteVentaService(
            pedidoRepository, festivalRepository, unidadVentaRepository);

    private final Festival festival = new Festival("Festival de prueba", EnumTemporadaFestival.PRIMAVERA,
            LocalDate.of(2026, 11, 1), LocalDate.of(2026, 11, 3), null);
    private final UnidadVenta unidad = new PuestoDesarmable("Puesto propio", "PD-0000001", 20, festival, 2, 45, null);
    private final Plato empanada = new Plato("Empanada", 3500, 1200, unidad);
    private final Plato sanguche = new Plato("Sanguche", 6200, 2300, unidad);

    private Pedido pedidoCerrado(Object... platosYCantidades) {
        Pedido pedido = new Pedido(LocalDateTime.now(), unidad);
        pedido.setCerrado(true);
        for (int i = 0; i < platosYCantidades.length; i += 2) {
            pedido.getItems().add(new ItemPedido((Plato) platosYCantidades[i], pedido, (int) platosYCantidades[i + 1]));
        }
        return pedido;
    }

    @Test
    void testRecaudacionSumaLosPedidosCerradosDeLaUnidad() {
        when(pedidoRepository.findByUnidadVentaAndCerradoTrue(unidad)).thenReturn(List.of(
                pedidoCerrado(empanada, 3, sanguche, 2),
                pedidoCerrado(empanada, 1)));

        ReporteVentaDTO reporte = reporteVentaService.reporteDeUnidad(unidad);

        assertEquals(3 * 3500 + 2 * 6200 + 3500.0, reporte.getRecaudacion());
        assertEquals(2, reporte.getCantidadPedidos());
        assertEquals(6, reporte.getCantidadPlatos());
        assertEquals("Puesto propio", reporte.getNombreUnidad());
        assertEquals("PD-0000001", reporte.getCodigoUnidad());
        assertEquals("Festival de prueba", reporte.getNombreFestival());
    }

    @Test
    void testUnidadSinPedidosCerradosRecaudaCero() {
        when(pedidoRepository.findByUnidadVentaAndCerradoTrue(unidad)).thenReturn(List.of());

        ReporteVentaDTO reporte = reporteVentaService.reporteDeUnidad(unidad);

        assertEquals(0.0, reporte.getRecaudacion());
        assertEquals(0, reporte.getCantidadPedidos());
        assertEquals(0, reporte.getCantidadPlatos());
    }

    @Test
    void testReporteDelFestivalNoTieneDatosDeUnidad() {
        when(festivalRepository.findById(7L)).thenReturn(Optional.of(festival));
        when(pedidoRepository.findByFestival_IdAndCerradoTrue(7L)).thenReturn(List.of(
                pedidoCerrado(sanguche, 2)));

        ReporteVentaDTO reporte = reporteVentaService.reporteDeFestival(7L).orElseThrow();

        assertEquals(12400.0, reporte.getRecaudacion());
        assertEquals("Festival de prueba", reporte.getNombreFestival());
        assertNull(reporte.getNombreUnidad());
        assertNull(reporte.getCodigoUnidad());
    }

    @Test
    void testFestivalInexistenteNoTieneReporte() {
        when(festivalRepository.findById(99L)).thenReturn(Optional.empty());

        assertTrue(reporteVentaService.reporteDeFestival(99L).isEmpty());
    }
}
