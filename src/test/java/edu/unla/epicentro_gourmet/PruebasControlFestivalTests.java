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
import org.springframework.test.context.ActiveProfiles;
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
@ActiveProfiles("local")
@WithMockUser
class PruebasControlFestivalTests {
    @Autowired
    private MockMvc mockMvc;

    @Autowired
        private IFestivalRepositorio repositorioFestival;

    @Autowired
        private IUnidadVentaRepositorio repositorioUnidadVenta;

    @BeforeEach
    void limpiarDatos() {
        repositorioUnidadVenta.deleteAll();
        repositorioFestival.deleteAll();
    }

    @Test
    void testCU01ListarFestivalesOrdenadosPorFecha() throws Exception {
        repositorioFestival.save(festival("Festival A", LocalDate.of(2026, 11, 1)));
        repositorioFestival.save(festival("Festival B", LocalDate.of(2026, 12, 1)));

        mockMvc.perform(get("/api/v1/festivales"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].nombre").value("Festival A"))
                .andExpect(jsonPath("$[1].nombre").value("Festival B"));
    }

    @Test
    void testCU01ResponderNoEncontradoSiElFestivalNoExiste() throws Exception {
        mockMvc.perform(get("/api/v1/festivales/99999"))
                .andExpect(status().isNotFound());
    }

    @Test
    void testCU23CrearActualizarYEliminarFestival() throws Exception {
        String festivalJson = """
                {
                  "nombre": "Festival Inicial",
                  "temporada": "VERANO",
                  "fechaInicio": "2026-11-01",
                  "fechaFin": "2026-11-03"
                }
                """;

        String response = mockMvc.perform(post("/api/v1/festivales")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(festivalJson))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.nombre").value("Festival Inicial"))
                .andReturn().getResponse().getContentAsString();

        long festivalId = ((Number) JsonPath.read(response, "$.id")).longValue();
        String actualizacionJson = """
                {
                  "id": %d,
                  "nombre": "Festival Actualizado",
                  "temporada": "OTONO",
                  "fechaInicio": "2026-11-02",
                  "fechaFin": "2026-11-04"
                }
                """.formatted(festivalId);

        mockMvc.perform(put("/api/v1/festivales")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(actualizacionJson))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.nombre").value("Festival Actualizado"))
                .andExpect(jsonPath("$.temporada").value("OTONO"));

        mockMvc.perform(delete("/api/v1/festivales/{id}", festivalId).with(csrf()))
                .andExpect(status().isOk());

        mockMvc.perform(get("/api/v1/festivales/{id}", festivalId))
                .andExpect(status().isNotFound());
    }

    @Test
    void testCU23RechazarRangoDeFechasInvalido() throws Exception {
        String festivalJson = """
                {
                  "nombre": "Festival Inválido",
                  "temporada": "VERANO",
                  "fechaInicio": "2026-11-05",
                  "fechaFin": "2026-11-01"
                }
                """;

        mockMvc.perform(post("/api/v1/festivales")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(festivalJson))
                .andExpect(status().isBadRequest());
    }

    @Test
    void testCU23ImpedirEliminarFestivalConUnidadesAsociadas() throws Exception {
        Festival festival = repositorioFestival.save(festival("Festival con unidad", LocalDate.of(2026, 11, 1)));
        repositorioUnidadVenta.save(new FoodTruck(
                "Food Truck Uno", "FT-001", 20.0, festival, "ABC123", true, null));

        mockMvc.perform(delete("/api/v1/festivales/{id}", festival.getId()).with(csrf()))
                .andExpect(status().isConflict());
    }

    @Test
    void testCU02ListarUnidadesDelFestival() throws Exception {
        Festival festival = repositorioFestival.save(festival("Festival Gastronómico", LocalDate.of(2026, 11, 1)));
        repositorioUnidadVenta.save(new FoodTruck(
                "Food Truck Uno", "FT-001", 20.0, festival, "ABC123", true, null));

        mockMvc.perform(get("/api/v1/festivales/{id}/unidades", festival.getId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].codigo").value("FT-001"))
                .andExpect(jsonPath("$[0].nombreComercial").value("Food Truck Uno"))
                .andExpect(jsonPath("$[0].tipo").value("FoodTruck"))
                .andExpect(jsonPath("$[0].patente").value("ABC123"))
                .andExpect(jsonPath("$[0].conexionElectrica").value(true));
    }

    private Festival festival(String nombre, LocalDate fechaInicio) {
        return new Festival(nombre, EnumTemporadaFestival.VERANO, fechaInicio, fechaInicio.plusDays(2), null);
    }
}