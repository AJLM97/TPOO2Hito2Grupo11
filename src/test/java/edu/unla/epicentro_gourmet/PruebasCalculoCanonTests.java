package edu.unla.epicentro_gourmet;

import edu.unla.epicentro_gourmet.dtos.DetalleCanonDTO;
import edu.unla.epicentro_gourmet.entities.Costos;
import edu.unla.epicentro_gourmet.entities.EnumAplicacionCosto;
import edu.unla.epicentro_gourmet.entities.EnumTemporadaFestival;
import edu.unla.epicentro_gourmet.entities.Festival;
import edu.unla.epicentro_gourmet.entities.FoodTruck;
import edu.unla.epicentro_gourmet.entities.PuestoDesarmable;
import edu.unla.epicentro_gourmet.services.implementation.CanonService;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class PruebasCalculoCanonTests {
    private final CanonService canonService = new CanonService();

    private Festival festival(float costoSuperficie, float costoMontaje, float plusElectricidad) {
        Costos costos = new Costos(costoSuperficie, costoMontaje, plusElectricidad, 50000, 100000, 150000, 5000, 100000);
        return new Festival("Festival de prueba", EnumTemporadaFestival.PRIMAVERA,
                LocalDate.of(2026, 11, 1), LocalDate.of(2026, 11, 3), costos);
    }

    private FoodTruck foodTruck(Festival festival, double superficie, boolean conexionElectrica) {
        return new FoodTruck("Truck", "FT-0000001", superficie, festival, "AB123CD", conexionElectrica, null);
    }

    private PuestoDesarmable puesto(Festival festival, double superficie, long minutosMontaje) {
        return new PuestoDesarmable("Puesto", "PD-0000001", superficie, festival, 2, minutosMontaje, null);
    }

    @Test
    void testFoodTruckConElectricidadSumaElPlus() {
        Festival festival = festival(500, 10, 2000);

        DetalleCanonDTO detalle = canonService.calcular(foodTruck(festival, 20, true)).orElseThrow();

        assertEquals(12000.0, detalle.getCanon());
        assertEquals("Food Truck", detalle.getTipoUnidad());
    }

    @Test
    void testFoodTruckSinElectricidadNoPagaElPlus() {
        Festival festival = festival(500, 10, 2000);

        assertEquals(10000.0, canonService.calcular(foodTruck(festival, 20, false)).orElseThrow().getCanon());
    }

    @Test
    void testPuestoRestaElTiempoDeMontaje() {
        Festival festival = festival(500, 10, 2000);

        DetalleCanonDTO detalle = canonService.calcular(puesto(festival, 20, 45)).orElseThrow();

        assertEquals(9550.0, detalle.getCanon());
        assertEquals(450.0, detalle.getDescuentoMontaje());
        assertEquals("Puesto Desarmable", detalle.getTipoUnidad());
    }

    @Test
    void testLosValoresSalenDeLosCostosDelFestival() {
        Festival festival = festival(800, 25, 3500);

        assertEquals(20 * 800 + 3500.0, canonService.calcular(foodTruck(festival, 20, true)).orElseThrow().getCanon());
        assertEquals(20 * 800 - 45 * 25.0, canonService.calcular(puesto(festival, 20, 45)).orElseThrow().getCanon());
    }

    @Test
    void testElAlcanceDelCostoPorSuperficieLimitaLaUnidadQueLoPaga() {
        Festival festival = festival(500, 10, 2000);
        festival.getCosto().setAplicacionCostoSuperficie(EnumAplicacionCosto.FOOD_TRUCK);

        assertEquals(10000.0, canonService.calcular(foodTruck(festival, 20, false)).orElseThrow().getCanon());
        assertEquals(0.0, canonService.calcular(puesto(festival, 20, 45)).orElseThrow().getImporteSuperficie());
    }

    @Test
    void testElPlusDeElectricidadNoAplicaSiElAlcanceNoEsFoodTruck() {
        Festival festival = festival(500, 10, 2000);
        festival.getCosto().setAplicacionPlusElectricidad(EnumAplicacionCosto.PUESTO_DESARMABLE);

        assertEquals(0.0, canonService.calcular(foodTruck(festival, 20, true)).orElseThrow().getPlusElectricidad());
    }

    @Test
    void testElCanonNuncaEsNegativo() {
        Festival festival = festival(500, 10, 2000);

        assertEquals(0.0, canonService.calcular(puesto(festival, 1, 100)).orElseThrow().getCanon());
    }

    @Test
    void testFestivalSinCostosNoTieneCanon() {
        Festival festival = new Festival("Sin costos", EnumTemporadaFestival.VERANO,
                LocalDate.of(2026, 11, 1), LocalDate.of(2026, 11, 3), null);

        assertTrue(canonService.calcular(foodTruck(festival, 20, true)).isEmpty());
    }
}
