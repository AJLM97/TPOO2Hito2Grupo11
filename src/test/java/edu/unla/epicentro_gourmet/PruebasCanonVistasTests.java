package edu.unla.epicentro_gourmet;

import edu.unla.epicentro_gourmet.entities.Costos;
import edu.unla.epicentro_gourmet.entities.EnumRolUsuario;
import edu.unla.epicentro_gourmet.entities.EnumTemporadaFestival;
import edu.unla.epicentro_gourmet.entities.Festival;
import edu.unla.epicentro_gourmet.entities.FoodTruck;
import edu.unla.epicentro_gourmet.entities.PuestoDesarmable;
import edu.unla.epicentro_gourmet.entities.Usuario;
import edu.unla.epicentro_gourmet.repositories.IFestivalRepository;
import edu.unla.epicentro_gourmet.repositories.IPlatoRepository;
import edu.unla.epicentro_gourmet.repositories.IUnidadVentaRepository;
import edu.unla.epicentro_gourmet.repositories.IUsuarioRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDate;

import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.hasProperty;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.nullValue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.model;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.redirectedUrl;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.view;

@SpringBootTest
@AutoConfigureMockMvc
@WithMockUser(username = PruebasCanonVistasTests.EMAIL_RESPONSABLE, authorities = "RESPONSABLE")
class PruebasCanonVistasTests {
    static final String EMAIL_RESPONSABLE = "responsable.canon@epicentro.com";

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private IPlatoRepository platoRepository;

    @Autowired
    private IUnidadVentaRepository unidadVentaRepository;

    @Autowired
    private IFestivalRepository festivalRepository;

    @Autowired
    private IUsuarioRepository usuarioRepository;

    private Usuario responsable;

    @BeforeEach
    void limpiarDatos() {
        platoRepository.deleteAll();
        unidadVentaRepository.deleteAll();
        festivalRepository.deleteAll();
        usuarioRepository.deleteAll();
        responsable = usuarioRepository.save(new Usuario(EMAIL_RESPONSABLE, "clave", EnumRolUsuario.RESPONSABLE, true));
    }

    private Festival festivalConCostos() {
        Costos costos = new Costos(500, 10, 2000, 50000, 100000, 150000, 5000, 100000);
        return festivalRepository.save(new Festival("Festival con costos", EnumTemporadaFestival.PRIMAVERA,
                LocalDate.of(2026, 11, 1), LocalDate.of(2026, 11, 3), costos));
    }

    @Test
    void testMuestraElCanonDeUnPuestoConSuDesglose() throws Exception {
        unidadVentaRepository.save(new PuestoDesarmable("Puesto propio", "PD-0001", 20.0, festivalConCostos(), 2, 45, responsable));

        mockMvc.perform(get("/responsable/canon"))
                .andExpect(status().isOk())
                .andExpect(view().name("responsable/canon"))
                .andExpect(model().attribute("canon", hasProperty("canon", is(9550.0))))
                .andExpect(content().string(containsString("Puesto propio")))
                .andExpect(content().string(containsString("Festival con costos")));
    }

    @Test
    void testMuestraElCanonDeUnFoodTruckConElectricidad() throws Exception {
        unidadVentaRepository.save(new FoodTruck("Truck propio", "FT-0001", 20.0, festivalConCostos(), "AB123CD", true, responsable));

        mockMvc.perform(get("/responsable/canon"))
                .andExpect(status().isOk())
                .andExpect(model().attribute("canon", hasProperty("canon", is(12000.0))))
                .andExpect(content().string(containsString("Food Truck")));
    }

    @Test
    void testFestivalSinCostosAvisaQueNoSePuedeCalcular() throws Exception {
        Festival sinCostos = festivalRepository.save(new Festival("Sin costos", EnumTemporadaFestival.VERANO,
                LocalDate.of(2026, 11, 1), LocalDate.of(2026, 11, 3), null));
        unidadVentaRepository.save(new PuestoDesarmable("Puesto propio", "PD-0001", 20.0, sinCostos, 2, 45, responsable));

        mockMvc.perform(get("/responsable/canon"))
                .andExpect(status().isOk())
                .andExpect(model().attribute("canon", nullValue()))
                .andExpect(content().string(containsString("no tiene costos configurados")));
    }

    @Test
    void testResponsableSinUnidadEsRedirigido() throws Exception {
        mockMvc.perform(get("/responsable/canon"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/responsable/empleados"));
    }

    @Test
    @WithMockUser(username = "cajero@epicentro.com", authorities = "EMPLEADO")
    void testUnEmpleadoNoPuedeVerElCanon() throws Exception {
        mockMvc.perform(get("/responsable/canon"))
                .andExpect(status().isForbidden());
    }
}
