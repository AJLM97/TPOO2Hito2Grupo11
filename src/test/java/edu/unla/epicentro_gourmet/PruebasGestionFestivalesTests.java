package edu.unla.epicentro_gourmet;

import com.jayway.jsonpath.JsonPath;
import edu.unla.epicentro_gourmet.entities.Costos;
import edu.unla.epicentro_gourmet.entities.EnumAplicacionCosto;
import edu.unla.epicentro_gourmet.entities.EnumTemporadaFestival;
import edu.unla.epicentro_gourmet.entities.Festival;
import edu.unla.epicentro_gourmet.entities.FoodTruck;
import edu.unla.epicentro_gourmet.entities.PuestoDesarmable;
import edu.unla.epicentro_gourmet.repositories.IFestivalRepository;
import edu.unla.epicentro_gourmet.repositories.IUnidadVentaRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDate;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@WithMockUser
class PruebasGestionFestivalesTests {
    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private IFestivalRepository festivalRepositorio;

    @Autowired
    private IUnidadVentaRepository unidadVentaRepositorio;

    @BeforeEach
    void limpiarDatos() {
        unidadVentaRepositorio.deleteAll();
        festivalRepositorio.deleteAll();
    }

    @Test
    void testCU23CrearFestival() throws Exception {
        mockMvc.perform(post("/api/v1/festivales")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonFestival("Festival de Verano", "2026-11-01", "2026-11-03")))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").isNumber())
                .andExpect(jsonPath("$.nombre").value("Festival de Verano"))
                .andExpect(jsonPath("$.fechaInicio").value("2026-11-01"))
                .andExpect(jsonPath("$.costos.sueldoBase").value(100000.0))
                .andExpect(jsonPath("$.costos.costoSuperficie").value(500.0))
                .andExpect(jsonPath("$.costos.aplicacionCostoSuperficie").value("TODAS"))
                .andExpect(jsonPath("$.costos.aplicacionCostoMontaje").value("PUESTO_DESARMABLE"))
                .andExpect(jsonPath("$.costos.aplicacionPlusElectricidad").value("FOOD_TRUCK"));
    }

    @Test
    void testConsultarFestivalIncluyeCostos() throws Exception {
        Festival festival = festivalRepositorio.save(festival("Festival Consultado"));

        mockMvc.perform(get("/api/v1/festivales/{id}", festival.getId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.costos.costoSuperficie").value(500.0))
                .andExpect(jsonPath("$.costos.plusElectricidad").value(2000.0))
                .andExpect(jsonPath("$.costos.sueldoBase").value(100000.0));
    }

            @Test
            void testListarFestivalesOrdenadosPorFechaInicioIncluyendoCostos() throws Exception {
            festivalRepositorio.save(festival("Festival Diciembre", LocalDate.of(2026, 12, 1)));
            festivalRepositorio.save(festival("Festival Noviembre", LocalDate.of(2026, 11, 1)));

            mockMvc.perform(get("/api/v1/festivales"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].nombre").value("Festival Noviembre"))
                .andExpect(jsonPath("$[0].fechaInicio").value("2026-11-01"))
                .andExpect(jsonPath("$[0].costos.sueldoBase").value(100000.0))
                .andExpect(jsonPath("$[1].nombre").value("Festival Diciembre"));
            }

            @Test
            void testListarUnidadesDeFestivalIncluyeDatosEspecificosPorTipo() throws Exception {
            Festival festival = festivalRepositorio.save(festival("Festival con unidades"));
            unidadVentaRepositorio.save(new FoodTruck(
                "Food Truck Uno", "FT-001", 20.0, festival, "ABC123", true, null));
            unidadVentaRepositorio.save(new PuestoDesarmable(
                "Puesto Uno", "PD-001", 12.0, festival, 2, 45, null));

            mockMvc.perform(get("/api/v1/festivales/{id}/unidades", festival.getId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].codigo").value("FT-001"))
                .andExpect(jsonPath("$[0].tipo").value("FoodTruck"))
                .andExpect(jsonPath("$[0].patente").value("ABC123"))
                .andExpect(jsonPath("$[0].conexionElectrica").value(true))
                .andExpect(jsonPath("$[1].codigo").value("PD-001"))
                .andExpect(jsonPath("$[1].tipo").value("PuestoDesarmable"))
                .andExpect(jsonPath("$[1].cantidadDeCarpas").value(2))
                .andExpect(jsonPath("$[1].tiempoMontajeMinutos").value(45));
            }

            @Test
            void testCostosDelFestivalAplicanSegunTipoDeUnidad() {
            Costos costos = costos();
            Festival festival = festival("Festival reglas de costos");
            FoodTruck foodTruckConElectricidad = new FoodTruck(
                "Food Truck eléctrico", "FT-EL-01", 20.0, festival, "ABC123", true, null);
            FoodTruck foodTruckSinElectricidad = new FoodTruck(
                "Food Truck sin electricidad", "FT-SE-01", 15.0, festival, "DEF456", false, null);
            PuestoDesarmable puesto = new PuestoDesarmable(
                "Puesto", "PD-001", 12.0, festival, 2, 45, null);

            org.junit.jupiter.api.Assertions.assertTrue(costos.aplicaCostoSuperficie(foodTruckConElectricidad));
            org.junit.jupiter.api.Assertions.assertTrue(costos.aplicaCostoSuperficie(puesto));
            org.junit.jupiter.api.Assertions.assertFalse(costos.aplicaCostoMontaje(foodTruckConElectricidad));
            org.junit.jupiter.api.Assertions.assertTrue(costos.aplicaCostoMontaje(puesto));
            org.junit.jupiter.api.Assertions.assertTrue(costos.aplicaPlusElectricidad(foodTruckConElectricidad));
            org.junit.jupiter.api.Assertions.assertFalse(costos.aplicaPlusElectricidad(foodTruckSinElectricidad));
            org.junit.jupiter.api.Assertions.assertFalse(costos.aplicaPlusElectricidad(puesto));
            }

            @Test
            void testListarUnidadesParaFestivalInexistenteRespondeNoEncontrado() throws Exception {
            mockMvc.perform(get("/api/v1/festivales/99999/unidades"))
                .andExpect(status().isNotFound());
            }

    @Test
    void testCU23ActualizarFestival() throws Exception {
        Festival festival = festivalRepositorio.save(festival("Festival Inicial"));

        mockMvc.perform(put("/api/v1/festivales/{id}", festival.getId())
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonFestival("Festival Actualizado", "2026-11-02", "2026-11-04")
                            .replace("\"costoSuperficie\": 500", "\"costoSuperficie\": 600")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.nombre").value("Festival Actualizado"))
                .andExpect(jsonPath("$.temporada").value("VERANO"))
                    .andExpect(jsonPath("$.costos.costoSuperficie").value(600.0))
                .andExpect(jsonPath("$.costos.sueldoBase").value(100000.0));
    }

    @Test
    void testCU23RechazarRangoDeFechasInvalido() throws Exception {
        mockMvc.perform(post("/api/v1/festivales")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonFestival("Festival Inválido", "2026-11-05", "2026-11-01")))
                .andExpect(status().isBadRequest());
    }

    @Test
    void testCU23EliminarFestivalSinUnidades() throws Exception {
        Festival festival = festivalRepositorio.save(festival("Festival a eliminar"));

        mockMvc.perform(delete("/api/v1/festivales/{id}", festival.getId()).with(csrf()))
                .andExpect(status().isNoContent());

        org.junit.jupiter.api.Assertions.assertFalse(festivalRepositorio.existsById(festival.getId()));
    }

    @Test
    void testCU23ImpedirEliminarFestivalConUnidadesAsociadas() throws Exception {
        Festival festival = festivalRepositorio.save(festival("Festival con unidad"));
        unidadVentaRepositorio.save(new FoodTruck(
                "Food Truck Uno", "FT-001", 20.0, festival, "ABC123", true, null));

        mockMvc.perform(delete("/api/v1/festivales/{id}", festival.getId()).with(csrf()))
                .andExpect(status().isConflict());
    }

    @Test
    void testCU23NoCrearFestivalConFechasObligatoriasAusentes() throws Exception {
        mockMvc.perform(post("/api/v1/festivales")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"nombre\":\"Festival incompleto\",\"temporada\":\"VERANO\"}"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void testCU23NoCrearFestivalSinCostos() throws Exception {
        mockMvc.perform(post("/api/v1/festivales")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"nombre":"Festival sin costos","temporada":"VERANO",
                                 "fechaInicio":"2026-11-01","fechaFin":"2026-11-03"}
                                """))
                .andExpect(status().isBadRequest());
    }

    @Test
    void testCU23RechazarPlusElectricidadAplicadoAPuestos() throws Exception {
        mockMvc.perform(post("/api/v1/festivales")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonFestival("Festival con costo eléctrico inválido", "2026-11-01", "2026-11-03")
                                .replace("\"aplicacionPlusElectricidad\": \"FOOD_TRUCK\"",
                                        "\"aplicacionPlusElectricidad\": \"PUESTO_DESARMABLE\"")))
                .andExpect(status().isBadRequest());
    }

    private Festival festival(String nombre) {
        return festival(nombre, LocalDate.of(2026, 11, 1));
    }

    private Festival festival(String nombre, LocalDate fechaInicio) {
        return new Festival(nombre, EnumTemporadaFestival.VERANO,
                fechaInicio, fechaInicio.plusDays(2), costos());
    }

    private Costos costos() {
        Costos costos = new Costos(500, 10, 2000, 50000, 100000, 150000, 5000, 100000);
        costos.setAplicacionCostoSuperficie(EnumAplicacionCosto.TODAS);
        costos.setAplicacionCostoMontaje(EnumAplicacionCosto.PUESTO_DESARMABLE);
        costos.setAplicacionPlusElectricidad(EnumAplicacionCosto.FOOD_TRUCK);
        return costos;
    }

    private String jsonFestival(String nombre, String fechaInicio, String fechaFin) {
        return """
                {
                  "nombre": "%s",
                  "temporada": "VERANO",
                  "fechaInicio": "%s",
                                    "fechaFin": "%s",
                                    "costos": {
                                        "costoSuperficie": 500,
                                        "costoMontaje": 10,
                                        "plusElectricidad": 2000,
                                        "plusLavaplatos": 50000,
                                        "plusAyudante": 100000,
                                        "plusCocinero": 150000,
                                        "plusAntiguedad": 5000,
                                        "sueldoBase": 100000,
                                        "aplicacionCostoSuperficie": "TODAS",
                                        "aplicacionCostoMontaje": "PUESTO_DESARMABLE",
                                        "aplicacionPlusElectricidad": "FOOD_TRUCK"
                                    }
                }
                """.formatted(nombre, fechaInicio, fechaFin);
    }
}