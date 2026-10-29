package edu.unla.epicentro_gourmet;

import edu.unla.epicentro_gourmet.dtos.RespuestaFestival;
import edu.unla.epicentro_gourmet.entities.Costos;
import edu.unla.epicentro_gourmet.entities.EnumTemporadaFestival;
import edu.unla.epicentro_gourmet.entities.Festival;
import edu.unla.epicentro_gourmet.entities.FoodTruck;
import edu.unla.epicentro_gourmet.repositories.IFestivalRepository;
import edu.unla.epicentro_gourmet.repositories.IUnidadVentaRepository;
import edu.unla.epicentro_gourmet.services.IFestivalService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;

import java.time.LocalDate;
import java.util.LinkedHashMap;
import java.util.Map;

import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.hasSize;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.flash;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.model;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.redirectedUrl;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.view;

@SpringBootTest
@AutoConfigureMockMvc
@WithMockUser(authorities = "ADMINISTRADOR")
class PruebasAbmFestivalesVistasTests {
    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private IFestivalRepository festivalRepository;

    @Autowired
    private IUnidadVentaRepository unidadVentaRepository;

    @Autowired
    private IFestivalService festivalService;

    @BeforeEach
    void limpiarDatos() {
        unidadVentaRepository.deleteAll();
        festivalRepository.deleteAll();
    }

    @Test
    void testListarFestivalesMuestraLosFestivalesOrdenadosPorFecha() throws Exception {
        festivalRepository.save(festival("Festival Diciembre", LocalDate.of(2026, 12, 1)));
        festivalRepository.save(festival("Festival Noviembre", LocalDate.of(2026, 11, 1)));

        mockMvc.perform(get("/admin/festivales"))
                .andExpect(status().isOk())
                .andExpect(view().name("admin/festivales"))
                .andExpect(model().attribute("festivales", hasSize(2)))
                .andExpect(content().string(containsString("Festival Noviembre")))
                .andExpect(content().string(containsString("Festival Diciembre")));
    }

    @Test
    void testFormularioDeAltaSeMuestraConSusCampos() throws Exception {
        mockMvc.perform(get("/admin/festivales/nuevo"))
                .andExpect(status().isOk())
                .andExpect(view().name("admin/festivales-form"))
                .andExpect(model().attributeExists("festival"))
                .andExpect(content().string(containsString("Costos del festival")));
    }

    @Test
    void testAltaDeFestivalGuardaFestivalYCostos() throws Exception {
        mockMvc.perform(formularioFestival(post("/admin/festivales/guardar"), "Festival de Verano", "2026-11-01", "2026-11-03"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/admin/festivales"));

        RespuestaFestival creado = festivalService.listar().get(0);
        assertEquals("Festival de Verano", creado.getNombre());
        assertEquals(500f, creado.getCostos().getCostoSuperficie());
        assertEquals(100000f, creado.getCostos().getSueldoBase());
    }

    @Test
    void testAltaConPeriodoInvalidoVuelveAlFormularioConErrores() throws Exception {
        mockMvc.perform(formularioFestival(post("/admin/festivales/guardar"), "Festival Inválido", "2026-11-05", "2026-11-01"))
                .andExpect(status().isOk())
                .andExpect(view().name("admin/festivales-form"))
                .andExpect(model().attributeHasErrors("festival"))
                .andExpect(content().string(containsString("La fecha de fin no puede ser anterior a la fecha de inicio")));

        assertTrue(festivalService.listar().isEmpty());
    }

    @Test
    void testAltaConCostoNegativoVuelveAlFormularioConErrores() throws Exception {
        mockMvc.perform(formularioFestival(post("/admin/festivales/guardar"), "Festival costo negativo", "2026-11-01", "2026-11-03",
                        "costos.costoSuperficie", "-1"))
                .andExpect(status().isOk())
                .andExpect(view().name("admin/festivales-form"))
                .andExpect(model().attributeHasErrors("festival"));

        assertTrue(festivalService.listar().isEmpty());
    }

    @Test
    void testAltaConPlusElectricoAplicadoAPuestosVuelveAlFormularioConErrores() throws Exception {
        mockMvc.perform(formularioFestival(post("/admin/festivales/guardar"), "Festival electricidad", "2026-11-01", "2026-11-03",
                        "costos.aplicacionPlusElectricidad", "PUESTO_DESARMABLE"))
                .andExpect(status().isOk())
                .andExpect(view().name("admin/festivales-form"))
                .andExpect(model().attributeHasErrors("festival"));

        assertTrue(festivalService.listar().isEmpty());
    }

    @Test
    void testFormularioDeEdicionCargaLosDatosDelFestival() throws Exception {
        Festival festival = festivalRepository.save(festival("Festival a editar", LocalDate.of(2026, 11, 1)));

        mockMvc.perform(get("/admin/festivales/editar/{id}", festival.getId()))
                .andExpect(status().isOk())
                .andExpect(view().name("admin/festivales-edit"))
                .andExpect(model().attributeExists("festival"))
                .andExpect(content().string(containsString("Festival a editar")));
    }

    @Test
    void testEdicionActualizaFestivalYCostos() throws Exception {
        Festival festival = festivalRepository.save(festival("Festival Inicial", LocalDate.of(2026, 11, 1)));

        mockMvc.perform(formularioFestival(post("/admin/festivales/actualizar"), "Festival Actualizado", "2026-11-02", "2026-11-04",
                        "costos.costoSuperficie", "600")
                        .param("id", festival.getId().toString()))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/admin/festivales"));

        RespuestaFestival actualizado = festivalService.buscar(festival.getId());
        assertEquals("Festival Actualizado", actualizado.getNombre());
        assertEquals(LocalDate.of(2026, 11, 2), actualizado.getFechaInicio());
        assertEquals(600f, actualizado.getCostos().getCostoSuperficie());
    }

    @Test
    void testEdicionInvalidaVuelveAlFormularioDeEdicion() throws Exception {
        Festival festival = festivalRepository.save(festival("Festival Inicial", LocalDate.of(2026, 11, 1)));

        mockMvc.perform(formularioFestival(post("/admin/festivales/actualizar"), "Festival Inicial", "2026-11-05", "2026-11-01")
                        .param("id", festival.getId().toString()))
                .andExpect(status().isOk())
                .andExpect(view().name("admin/festivales-edit"))
                .andExpect(model().attributeHasErrors("festival"));
    }

    @Test
    void testEliminarFestivalSinUnidades() throws Exception {
        Festival festival = festivalRepository.save(festival("Festival a eliminar", LocalDate.of(2026, 11, 1)));

        mockMvc.perform(post("/admin/festivales/eliminar/{id}", festival.getId()))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/admin/festivales"));

        assertTrue(festivalService.listar().isEmpty());
    }

    @Test
    void testNoSePuedeEliminarUnFestivalConUnidadesAsociadas() throws Exception {
        Festival festival = festivalRepository.save(festival("Festival con unidad", LocalDate.of(2026, 11, 1)));
        unidadVentaRepository.save(new FoodTruck("Food Truck Uno", "FT-001", 20.0, festival, "ABC123", true, null));

        mockMvc.perform(post("/admin/festivales/eliminar/{id}", festival.getId()))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/admin/festivales"))
                .andExpect(flash().attribute("error",
                        "No se puede eliminar un festival con unidades de venta asociadas"));

        assertEquals(1, festivalService.listar().size());
    }

    @Test
    @WithMockUser(authorities = "RESPONSABLE")
    void testUnResponsableNoPuedeAccederAlAbmDeFestivales() throws Exception {
        mockMvc.perform(get("/admin/festivales"))
                .andExpect(status().isForbidden());
    }

    private MockHttpServletRequestBuilder formularioFestival(MockHttpServletRequestBuilder request,
                                                             String nombre, String fechaInicio, String fechaFin,
                                                             String... reemplazos) {
        Map<String, String> campos = new LinkedHashMap<>();
        campos.put("nombre", nombre);
        campos.put("temporada", "VERANO");
        campos.put("fechaInicio", fechaInicio);
        campos.put("fechaFin", fechaFin);
        campos.put("costos.sueldoBase", "100000");
        campos.put("costos.costoSuperficie", "500");
        campos.put("costos.costoMontaje", "10");
        campos.put("costos.plusElectricidad", "2000");
        campos.put("costos.plusLavaplatos", "50000");
        campos.put("costos.plusAyudante", "100000");
        campos.put("costos.plusCocinero", "150000");
        campos.put("costos.plusAntiguedad", "5000");
        campos.put("costos.aplicacionCostoSuperficie", "TODAS");
        campos.put("costos.aplicacionCostoMontaje", "PUESTO_DESARMABLE");
        campos.put("costos.aplicacionPlusElectricidad", "FOOD_TRUCK");
        for (int i = 0; i < reemplazos.length; i += 2) {
            campos.put(reemplazos[i], reemplazos[i + 1]);
        }
        campos.forEach(request::param);
        return request;
    }

    private Festival festival(String nombre, LocalDate fechaInicio) {
        return new Festival(nombre, EnumTemporadaFestival.VERANO, fechaInicio, fechaInicio.plusDays(2),
                new Costos(500, 10, 2000, 50000, 100000, 150000, 5000, 100000));
    }
}
