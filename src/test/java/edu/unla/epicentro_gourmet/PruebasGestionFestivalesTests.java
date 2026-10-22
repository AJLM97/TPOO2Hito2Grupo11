package edu.unla.epicentro_gourmet;

import com.jayway.jsonpath.JsonPath;
import edu.unla.epicentro_gourmet.entities.EnumTemporadaFestival;
import edu.unla.epicentro_gourmet.entities.Festival;
import edu.unla.epicentro_gourmet.entities.FoodTruck;
import edu.unla.epicentro_gourmet.repositories.IFestivalRepositorio;
import edu.unla.epicentro_gourmet.repositories.IUnidadVentaRepositorio;
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
    private IFestivalRepositorio festivalRepositorio;

    @Autowired
    private IUnidadVentaRepositorio unidadVentaRepositorio;

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
                .andExpect(jsonPath("$.fechaInicio").value("2026-11-01"));
    }

    @Test
    void testCU23ActualizarFestival() throws Exception {
        Festival festival = festivalRepositorio.save(festival("Festival Inicial"));

        mockMvc.perform(put("/api/v1/festivales/{id}", festival.getId())
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonFestival("Festival Actualizado", "2026-11-02", "2026-11-04")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.nombre").value("Festival Actualizado"))
                .andExpect(jsonPath("$.temporada").value("VERANO"));
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

    private Festival festival(String nombre) {
        return new Festival(nombre, EnumTemporadaFestival.VERANO,
                LocalDate.of(2026, 11, 1), LocalDate.of(2026, 11, 3), null);
    }

    private String jsonFestival(String nombre, String fechaInicio, String fechaFin) {
        return """
                {
                  "nombre": "%s",
                  "temporada": "VERANO",
                  "fechaInicio": "%s",
                  "fechaFin": "%s"
                }
                """.formatted(nombre, fechaInicio, fechaFin);
    }
}