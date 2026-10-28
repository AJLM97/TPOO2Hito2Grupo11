package edu.unla.epicentro_gourmet;

import edu.unla.epicentro_gourmet.entities.EnumRolUsuario;
import edu.unla.epicentro_gourmet.entities.EnumTemporadaFestival;
import edu.unla.epicentro_gourmet.entities.Festival;
import edu.unla.epicentro_gourmet.entities.Plato;
import edu.unla.epicentro_gourmet.entities.PuestoDesarmable;
import edu.unla.epicentro_gourmet.entities.UnidadVenta;
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
import java.util.List;

import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.not;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.model;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.redirectedUrl;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.view;

@SpringBootTest
@AutoConfigureMockMvc
@WithMockUser(username = PruebasAbmPlatosVistasTests.EMAIL_RESPONSABLE, authorities = "RESPONSABLE")
class PruebasAbmPlatosVistasTests {
    static final String EMAIL_RESPONSABLE = "responsable.platos@epicentro.com";

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

    private UnidadVenta unidad;
    private UnidadVenta unidadAjena;

    @BeforeEach
    void prepararDatos() {
        platoRepository.deleteAll();
        unidadVentaRepository.deleteAll();
        festivalRepository.deleteAll();
        usuarioRepository.deleteAll();

        Festival festival = festivalRepository.save(new Festival("Festival de prueba",
                EnumTemporadaFestival.PRIMAVERA, LocalDate.of(2026, 11, 1), LocalDate.of(2026, 11, 3), null));
        Usuario responsable = usuarioRepository.save(new Usuario(EMAIL_RESPONSABLE, "clave", EnumRolUsuario.RESPONSABLE, true));
        unidad = unidadVentaRepository.save(new PuestoDesarmable(
                "Puesto propio", "PD-0001", 12.0, festival, 2, 45, responsable));
        unidadAjena = unidadVentaRepository.save(new PuestoDesarmable(
                "Puesto ajeno", "PD-0002", 10.0, festival, 1, 30, null));
    }

    @Test
    void testListadoMuestraSoloLosPlatosActivosDeLaUnidad() throws Exception {
        platoRepository.save(new Plato("Empanada", 3500, 1200, unidad));
        Plato dadoDeBaja = new Plato("Plato eliminado", 100, 50, unidad);
        dadoDeBaja.setActivo(false);
        platoRepository.save(dadoDeBaja);
        platoRepository.save(new Plato("Plato de otra unidad", 900, 300, unidadAjena));

        mockMvc.perform(get("/responsable/platos"))
                .andExpect(status().isOk())
                .andExpect(view().name("responsable/platos"))
                .andExpect(model().attribute("platos", hasSize(1)))
                .andExpect(content().string(containsString("Empanada")))
                .andExpect(content().string(not(containsString("Plato eliminado"))))
                .andExpect(content().string(not(containsString("Plato de otra unidad"))));
    }

    @Test
    void testFormularioDeAltaSeMuestraConSusCampos() throws Exception {
        mockMvc.perform(get("/responsable/platos/nuevo"))
                .andExpect(status().isOk())
                .andExpect(view().name("responsable/platos-form"))
                .andExpect(model().attributeExists("plato"))
                .andExpect(content().string(containsString("Costo de producción")));
    }

    @Test
    void testAltaCreaElPlatoEnLaUnidadDelResponsable() throws Exception {
        mockMvc.perform(post("/responsable/platos/guardar")
                        .param("nombre", "Sanguche de vegetales")
                        .param("precioVenta", "6200.50")
                        .param("costoProduccion", "2300"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/responsable/platos"));

        List<Plato> platos = platoRepository.findByUnidadAndActivoTrue(unidad);
        assertEquals(1, platos.size());
        assertEquals("Sanguche de vegetales", platos.get(0).getNombre());
        assertEquals(6200.50, platos.get(0).getPrecioVenta());
        assertEquals(2300, platos.get(0).getCostoProduccion());
        assertTrue(platoRepository.findByUnidadAndActivoTrue(unidadAjena).isEmpty());
    }

    @Test
    void testAltaConDatosInvalidosVuelveAlFormularioConErrores() throws Exception {
        mockMvc.perform(post("/responsable/platos/guardar")
                        .param("nombre", "ab")
                        .param("precioVenta", "0")
                        .param("costoProduccion", "-5"))
                .andExpect(status().isOk())
                .andExpect(view().name("responsable/platos-form"))
                .andExpect(model().attributeHasFieldErrors("plato", "nombre", "precioVenta", "costoProduccion"))
                .andExpect(content().string(containsString("El nombre debe tener entre 3 y 50 caracteres")));

        assertTrue(platoRepository.findByUnidadAndActivoTrue(unidad).isEmpty());
    }

    @Test
    void testAltaSinPrecioNiCostoVuelveAlFormularioConErrores() throws Exception {
        mockMvc.perform(post("/responsable/platos/guardar")
                        .param("nombre", "Plato sin importes"))
                .andExpect(status().isOk())
                .andExpect(view().name("responsable/platos-form"))
                .andExpect(model().attributeHasFieldErrors("plato", "precioVenta", "costoProduccion"));

        assertTrue(platoRepository.findByUnidadAndActivoTrue(unidad).isEmpty());
    }

    @Test
    void testAltaConNombreRepetidoEnLaUnidadSeRechaza() throws Exception {
        platoRepository.save(new Plato("Empanada", 3500, 1200, unidad));

        mockMvc.perform(post("/responsable/platos/guardar")
                        .param("nombre", "  empanada ")
                        .param("precioVenta", "4000")
                        .param("costoProduccion", "1500"))
                .andExpect(status().isOk())
                .andExpect(view().name("responsable/platos-form"))
                .andExpect(model().attributeHasFieldErrors("plato", "nombre"))
                .andExpect(content().string(containsString("Ya existe un plato con ese nombre")));

        assertEquals(1, platoRepository.findByUnidadAndActivoTrue(unidad).size());
    }

    @Test
    void testElMismoNombreSePuedeUsarEnOtraUnidad() throws Exception {
        platoRepository.save(new Plato("Empanada", 3500, 1200, unidadAjena));

        mockMvc.perform(post("/responsable/platos/guardar")
                        .param("nombre", "Empanada")
                        .param("precioVenta", "4000")
                        .param("costoProduccion", "1500"))
                .andExpect(status().is3xxRedirection());

        assertEquals(1, platoRepository.findByUnidadAndActivoTrue(unidad).size());
    }

    @Test
    void testAltaConNombreDeUnPlatoDadoDeBajaLoReactivaConLosNuevosDatos() throws Exception {
        Plato antiguo = new Plato("Empanada", 3500, 1200, unidad);
        antiguo.setActivo(false);
        antiguo = platoRepository.save(antiguo);

        mockMvc.perform(post("/responsable/platos/guardar")
                        .param("nombre", "Empanada")
                        .param("precioVenta", "5000")
                        .param("costoProduccion", "1800"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/responsable/platos"));

        List<Plato> delaUnidad = platoRepository.findAll().stream()
                .filter(p -> p.getUnidad().getId().equals(unidad.getId()))
                .toList();
        assertEquals(1, delaUnidad.size());
        assertEquals(antiguo.getId(), delaUnidad.get(0).getId());
        assertTrue(delaUnidad.get(0).isActivo());
        assertEquals(5000, delaUnidad.get(0).getPrecioVenta());
        assertEquals(1800, delaUnidad.get(0).getCostoProduccion());
    }

    @Test
    void testFormularioDeEdicionCargaLosDatosDelPlato() throws Exception {
        Plato plato = platoRepository.save(new Plato("Limonada de temporada", 2800, 700, unidad));

        mockMvc.perform(get("/responsable/platos/editar/{id}", plato.getId()))
                .andExpect(status().isOk())
                .andExpect(view().name("responsable/platos-edit"))
                .andExpect(model().attributeExists("plato"))
                .andExpect(content().string(containsString("Limonada de temporada")))
                .andExpect(content().string(containsString("2800.0")));
    }

    @Test
    void testNoSePuedeEditarUnPlatoDeOtraUnidad() throws Exception {
        Plato ajeno = platoRepository.save(new Plato("Plato ajeno", 900, 300, unidadAjena));

        mockMvc.perform(get("/responsable/platos/editar/{id}", ajeno.getId()))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/responsable/platos"));

        mockMvc.perform(post("/responsable/platos/actualizar")
                        .param("id", ajeno.getId().toString())
                        .param("nombre", "Plato hackeado")
                        .param("precioVenta", "1")
                        .param("costoProduccion", "1"))
                .andExpect(status().is3xxRedirection());

        assertEquals("Plato ajeno", platoRepository.findById(ajeno.getId()).orElseThrow().getNombre());
    }

    @Test
    void testActualizarModificaNombrePrecioYCosto() throws Exception {
        Plato plato = platoRepository.save(new Plato("Empanada", 3500, 1200, unidad));

        mockMvc.perform(post("/responsable/platos/actualizar")
                        .param("id", plato.getId().toString())
                        .param("nombre", "Empanada de carne")
                        .param("precioVenta", "4200")
                        .param("costoProduccion", "1400"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/responsable/platos"));

        Plato actualizado = platoRepository.findById(plato.getId()).orElseThrow();
        assertEquals("Empanada de carne", actualizado.getNombre());
        assertEquals(4200, actualizado.getPrecioVenta());
        assertEquals(1400, actualizado.getCostoProduccion());
    }

    @Test
    void testActualizarConDatosInvalidosVuelveAlFormularioDeEdicion() throws Exception {
        Plato plato = platoRepository.save(new Plato("Empanada", 3500, 1200, unidad));

        mockMvc.perform(post("/responsable/platos/actualizar")
                        .param("id", plato.getId().toString())
                        .param("nombre", "Empanada")
                        .param("precioVenta", "-10")
                        .param("costoProduccion", "1200"))
                .andExpect(status().isOk())
                .andExpect(view().name("responsable/platos-edit"))
                .andExpect(model().attributeHasFieldErrors("plato", "precioVenta"));

        assertEquals(3500, platoRepository.findById(plato.getId()).orElseThrow().getPrecioVenta());
    }

    @Test
    void testActualizarConElNombreDeOtroPlatoSeRechaza() throws Exception {
        platoRepository.save(new Plato("Empanada", 3500, 1200, unidad));
        Plato otro = platoRepository.save(new Plato("Limonada", 2800, 700, unidad));

        mockMvc.perform(post("/responsable/platos/actualizar")
                        .param("id", otro.getId().toString())
                        .param("nombre", "EMPANADA")
                        .param("precioVenta", "2800")
                        .param("costoProduccion", "700"))
                .andExpect(status().isOk())
                .andExpect(view().name("responsable/platos-edit"))
                .andExpect(model().attributeHasFieldErrors("plato", "nombre"));

        assertEquals("Limonada", platoRepository.findById(otro.getId()).orElseThrow().getNombre());
    }

    @Test
    void testActualizarSinCambiarElNombreNoLoTomaComoDuplicado() throws Exception {
        Plato plato = platoRepository.save(new Plato("Empanada", 3500, 1200, unidad));

        mockMvc.perform(post("/responsable/platos/actualizar")
                        .param("id", plato.getId().toString())
                        .param("nombre", "Empanada")
                        .param("precioVenta", "3900")
                        .param("costoProduccion", "1200"))
                .andExpect(status().is3xxRedirection());

        assertEquals(3900, platoRepository.findById(plato.getId()).orElseThrow().getPrecioVenta());
    }

    @Test
    void testBajaLogicaConservaElPlatoYLoOcultaDelListado() throws Exception {
        Plato plato = platoRepository.save(new Plato("Empanada", 3500, 1200, unidad));

        mockMvc.perform(post("/responsable/platos/baja/{id}", plato.getId()))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/responsable/platos"));

        assertFalse(platoRepository.findById(plato.getId()).orElseThrow().isActivo());
        assertTrue(platoRepository.findByUnidadAndActivoTrue(unidad).isEmpty());
        mockMvc.perform(get("/responsable/platos"))
                .andExpect(content().string(not(containsString("Empanada"))));
    }

    @Test
    void testNoSePuedeDarDeBajaUnPlatoDeOtraUnidad() throws Exception {
        Plato ajeno = platoRepository.save(new Plato("Plato ajeno", 900, 300, unidadAjena));

        mockMvc.perform(post("/responsable/platos/baja/{id}", ajeno.getId()))
                .andExpect(status().is3xxRedirection());

        assertTrue(platoRepository.findById(ajeno.getId()).orElseThrow().isActivo());
    }

    @Test
    @WithMockUser(authorities = "ADMINISTRADOR")
    void testUnAdministradorNoPuedeAccederAlAbmDePlatos() throws Exception {
        mockMvc.perform(get("/responsable/platos"))
                .andExpect(status().isForbidden());
    }
}
