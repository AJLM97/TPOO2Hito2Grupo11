package edu.unla.epicentro_gourmet;

import edu.unla.epicentro_gourmet.entities.Costos;
import edu.unla.epicentro_gourmet.entities.EnumTemporadaFestival;
import edu.unla.epicentro_gourmet.entities.Festival;
import edu.unla.epicentro_gourmet.entities.FoodTruck;
import edu.unla.epicentro_gourmet.entities.PuestoDesarmable;
import edu.unla.epicentro_gourmet.repositories.IEmpleadoRepository;
import edu.unla.epicentro_gourmet.repositories.IFestivalRepository;
import edu.unla.epicentro_gourmet.repositories.IPlatoRepository;
import edu.unla.epicentro_gourmet.repositories.IUnidadVentaRepository;
import edu.unla.epicentro_gourmet.repositories.IUsuarioRepository;
import edu.unla.epicentro_gourmet.services.IReporteMayoresCanonService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDate;
import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.hasProperty;
import static org.hamcrest.Matchers.is;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.model;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.view;

@SpringBootTest
@AutoConfigureMockMvc
@WithMockUser(authorities = "ADMINISTRADOR")
class PruebasMayoresCanonTests {
    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private IReporteMayoresCanonService reporteMayoresCanonService;

    @Autowired
    private IUnidadVentaRepository unidadVentaRepository;

    @Autowired
    private IPlatoRepository platoRepository;

    @Autowired
    private IEmpleadoRepository empleadoRepository;

    @Autowired
    private IFestivalRepository festivalRepository;

    @Autowired
    private IUsuarioRepository usuarioRepository;

    private Festival festival;

    @BeforeEach
    void prepararDatos() {
        empleadoRepository.deleteAll();
        platoRepository.deleteAll();
        unidadVentaRepository.deleteAll();
        festivalRepository.deleteAll();
        usuarioRepository.deleteAll();

        Costos costos = new Costos(100, 1, 500, 100, 100, 100, 0, 100);
        festival = festivalRepository.save(new Festival("Festival con canon", EnumTemporadaFestival.PRIMAVERA,
                LocalDate.of(2026, 11, 1), LocalDate.of(2026, 11, 3), costos));

        unidadVentaRepository.save(
                new PuestoDesarmable("Puesto mayor", "PD-0001", 20, festival, 2, 20, null));
        unidadVentaRepository.save(
                new FoodTruck("Food truck", "FT-0001", 12, festival, "AB123CD", true, null));
        unidadVentaRepository.save(
                new FoodTruck("Food truck sin electricidad", "FT-0002", 15, festival, "AB123EF", false, null));
        unidadVentaRepository.save(
                new PuestoDesarmable("Puesto menor", "PD-0002", 10, festival, 1, 30, null));
    }

    @Test
    void devuelveSoloLasUnidadesConMayorCanonEnOrdenDescendente() {
        var resultados = reporteMayoresCanonService.mayoresCanon(festival.getId(), 2);

        assertEquals(2, resultados.size());
        assertEquals("Puesto mayor", resultados.get(0).getNombreComercial());
        assertEquals(1980.0, resultados.get(0).getCanon());
        assertEquals(2000.0, resultados.get(0).getDetalleCanon().getImporteSuperficie());
        assertEquals(20.0, resultados.get(0).getDetalleCanon().getDescuentoMontaje());
        assertEquals("Food Truck", resultados.get(1).getTipoUnidad());
        assertEquals(1700.0, resultados.get(1).getCanon());
        assertEquals(500.0, resultados.get(1).getDetalleCanon().getPlusElectricidad());
    }

    @Test
    void rechazaUnaCantidadMenorQueUno() {
        assertThrows(IllegalArgumentException.class,
                () -> reporteMayoresCanonService.mayoresCanon(festival.getId(), 0));
    }

    @Test
    void muestraElReporteParaElFestivalYLaCantidadSolicitados() throws Exception {
        mockMvc.perform(get("/admin/mayores-canon")
                        .param("festivalId", festival.getId().toString())
                        .param("x", "2"))
                .andExpect(status().isOk())
                .andExpect(view().name("admin/mayores-canon"))
                .andExpect(model().attribute("unidades", contains(
                        hasProperty("nombreComercial", is("Puesto mayor")),
                        hasProperty("nombreComercial", is("Food truck")))))
                .andExpect(content().string(containsString("Unidades con mayor canon")))
                .andExpect(content().string(containsString("Puesto mayor")))
                .andExpect(content().string(containsString("Food Truck")))
                .andExpect(content().string(containsString("Ver costos")))
                .andExpect(content().string(containsString("Superficie")))
                .andExpect(content().string(containsString("Plus por electricidad")))
                .andExpect(content().string(containsString("Descuento por montaje")));
    }

    @Test
    void muestraErrorParaCantidadInvalida() throws Exception {
        mockMvc.perform(get("/admin/mayores-canon")
                        .param("festivalId", festival.getId().toString())
                        .param("x", "0"))
                .andExpect(status().isOk())
                .andExpect(model().attributeExists("error"))
                .andExpect(content().string(containsString("mayor que cero")));
    }

    @Test
    @WithMockUser(authorities = "RESPONSABLE")
    void restringeElReporteAlAdministrador() throws Exception {
        mockMvc.perform(get("/admin/mayores-canon"))
                .andExpect(status().isForbidden());
    }
}
