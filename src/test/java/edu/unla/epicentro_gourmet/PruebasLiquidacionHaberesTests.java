package edu.unla.epicentro_gourmet;

import edu.unla.epicentro_gourmet.dtos.LiquidacionHaberesDTO;
import edu.unla.epicentro_gourmet.entities.Cajero;
import edu.unla.epicentro_gourmet.entities.Cocinero;
import edu.unla.epicentro_gourmet.entities.Costos;
import edu.unla.epicentro_gourmet.entities.EnumCategoriaCocinero;
import edu.unla.epicentro_gourmet.entities.EnumTemporadaFestival;
import edu.unla.epicentro_gourmet.entities.EnumTurnoCajero;
import edu.unla.epicentro_gourmet.entities.Festival;
import edu.unla.epicentro_gourmet.entities.PuestoDesarmable;
import edu.unla.epicentro_gourmet.entities.UnidadVenta;
import edu.unla.epicentro_gourmet.repositories.IEmpleadoRepository;
import edu.unla.epicentro_gourmet.services.implementation.LiquidacionService;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class PruebasLiquidacionHaberesTests {
    private static final LocalDate INICIO = LocalDate.of(2026, 11, 1);
    private static final LocalDate INGRESO_ANTIGUO = LocalDate.of(2016, 1, 1);

    private final IEmpleadoRepository empleadoRepository = mock(IEmpleadoRepository.class);
    private final LiquidacionService liquidacionService = new LiquidacionService(empleadoRepository);

    private Festival festivalDeDias(long dias) {
        Costos costos = new Costos(500, 10, 2000, 50000, 100000, 150000, 5000, 100000);
        return new Festival("Festival de prueba", EnumTemporadaFestival.PRIMAVERA,
                INICIO, INICIO.plusDays(dias - 1), costos);
    }

    private UnidadVenta unidadEn(Festival festival) {
        return new PuestoDesarmable("Puesto", "PD-0000001", 20, festival, 2, 45, null);
    }

    private Cocinero cocinero(UnidadVenta unidad, EnumCategoriaCocinero categoria, LocalDate ingreso) {
        return new Cocinero("Lucia", "Fernandez", 30123456, LocalDate.of(1995, 3, 12), ingreso, 1, unidad, categoria, null);
    }

    private Cajero cajero(UnidadVenta unidad, LocalDate ingreso) {
        return new Cajero("Mateo", "Gomez", 28987654, LocalDate.of(1993, 7, 20), ingreso, 1, unidad,
                EnumTurnoCajero.MANANA, null);
    }

    private double totalCocinero(long diasFestival, EnumCategoriaCocinero categoria) {
        UnidadVenta unidad = unidadEn(festivalDeDias(diasFestival));
        return liquidacionService.liquidar(cocinero(unidad, categoria, INGRESO_ANTIGUO)).orElseThrow().getTotal();
    }

    @Test
    void testEjemploDeLaConsignaCocinaDe38Dias() {
        assertEquals(225000.0, totalCocinero(38, EnumCategoriaCocinero.LAVAPLATOS));
    }

    @Test
    void testEjemploDeLaConsignaCajeroDe52DiasConTresAniosDeAntiguedad() {
        UnidadVenta unidad = unidadEn(festivalDeDias(52));

        LiquidacionHaberesDTO liquidacion = liquidacionService
                .liquidar(cajero(unidad, LocalDate.of(2023, 11, 1))).orElseThrow();

        assertEquals(230000.0, liquidacion.getTotal());
        assertEquals(15000.0, liquidacion.getAdicional());
        assertEquals("Cajero", liquidacion.getRol());
    }

    @Test
    void testHastaQuinceDiasPagaElCincuentaPorCiento() {
        assertEquals(0.5 * (100000 + 150000), totalCocinero(3, EnumCategoriaCocinero.COCINERO));
        assertEquals(0.5 * (100000 + 150000), totalCocinero(15, EnumCategoriaCocinero.COCINERO));
    }

    @Test
    void testDeDieciseisATreintaDiasPagaElCienPorCiento() {
        assertEquals(100000 + 150000.0, totalCocinero(16, EnumCategoriaCocinero.COCINERO));
        assertEquals(100000 + 150000.0, totalCocinero(30, EnumCategoriaCocinero.COCINERO));
    }

    @Test
    void testPasadosLosTreintaDiasSeSumanTramosProporcionales() {
        assertEquals(1.5 * (100000 + 150000), totalCocinero(31, EnumCategoriaCocinero.COCINERO));
        assertEquals(2.0 * (100000 + 150000), totalCocinero(45, EnumCategoriaCocinero.COCINERO));
    }

    @Test
    void testCadaCategoriaDeCocinaTomaSuPlus() {
        assertEquals(100000 + 150000.0, totalCocinero(20, EnumCategoriaCocinero.COCINERO));
        assertEquals(100000 + 100000.0, totalCocinero(20, EnumCategoriaCocinero.COCINERO_AYUDANTE));
        assertEquals(100000 + 50000.0, totalCocinero(20, EnumCategoriaCocinero.LAVAPLATOS));
    }

    @Test
    void testElCocineroNoCobraAntiguedad() {
        UnidadVenta unidad = unidadEn(festivalDeDias(20));

        LiquidacionHaberesDTO liquidacion = liquidacionService
                .liquidar(cocinero(unidad, EnumCategoriaCocinero.COCINERO, INGRESO_ANTIGUO)).orElseThrow();

        assertEquals(250000.0, liquidacion.getTotal());
        assertEquals("Cocinero", liquidacion.getRol());
    }

    @Test
    void testLaAntiguedadCuentaAniosCompletosHastaElInicioDelFestival() {
        UnidadVenta unidad = unidadEn(festivalDeDias(20));

        assertEquals(15000.0, liquidacionService.liquidar(cajero(unidad, LocalDate.of(2023, 11, 1)))
                .orElseThrow().getAdicional());
        assertEquals(10000.0, liquidacionService.liquidar(cajero(unidad, LocalDate.of(2023, 11, 2)))
                .orElseThrow().getAdicional());
        assertEquals(0.0, liquidacionService.liquidar(cajero(unidad, LocalDate.of(2026, 12, 1)))
                .orElseThrow().getAdicional());
    }

    @Test
    void testLosValoresSalenDeLosCostosDelFestival() {
        Festival festival = festivalDeDias(20);
        festival.getCosto().setSueldoBase(120000);
        festival.getCosto().setPlusAntiguedad(8000);
        UnidadVenta unidad = unidadEn(festival);

        double total = liquidacionService.liquidar(cajero(unidad, LocalDate.of(2024, 11, 1))).orElseThrow().getTotal();

        assertEquals(120000 + 2 * 8000.0, total);
    }

    @Test
    void testElSueldoBaseDelEmpleadoNoReemplazaAlDelFestival() {
        UnidadVenta unidad = unidadEn(festivalDeDias(20));
        Cajero cajero = cajero(unidad, INGRESO_ANTIGUO);
        cajero.setSueldoBase(999999);

        assertEquals(100000.0, liquidacionService.liquidar(cajero).orElseThrow().getSueldoBase());
    }

    @Test
    void testLiquidarUnidadLiquidaATodoElPersonalActivo() {
        UnidadVenta unidad = unidadEn(festivalDeDias(20));
        when(empleadoRepository.findByUnidadAndActivoTrue(unidad)).thenReturn(List.of(
                cocinero(unidad, EnumCategoriaCocinero.COCINERO, INGRESO_ANTIGUO),
                cajero(unidad, LocalDate.of(2023, 11, 1))));

        List<LiquidacionHaberesDTO> liquidaciones = liquidacionService.liquidarUnidad(unidad).orElseThrow();

        assertEquals(2, liquidaciones.size());
        assertEquals(250000.0, liquidaciones.get(0).getTotal());
        assertEquals(115000.0, liquidaciones.get(1).getTotal());
    }

    @Test
    void testFestivalSinCostosNoSeLiquida() {
        Festival festival = new Festival("Sin costos", EnumTemporadaFestival.VERANO, INICIO, INICIO.plusDays(5), null);
        UnidadVenta unidad = unidadEn(festival);

        assertTrue(liquidacionService.liquidarUnidad(unidad).isEmpty());
        assertTrue(liquidacionService.liquidar(cajero(unidad, INGRESO_ANTIGUO)).isEmpty());
    }
}
