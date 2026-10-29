package edu.unla.epicentro_gourmet;

import edu.unla.epicentro_gourmet.dtos.PlatoEstrellaDTO;
import edu.unla.epicentro_gourmet.entities.EnumRolUsuario;
import edu.unla.epicentro_gourmet.entities.EnumTemporadaFestival;
import edu.unla.epicentro_gourmet.entities.Festival;
import edu.unla.epicentro_gourmet.entities.ItemPedido;
import edu.unla.epicentro_gourmet.entities.Pedido;
import edu.unla.epicentro_gourmet.entities.Plato;
import edu.unla.epicentro_gourmet.entities.PuestoDesarmable;
import edu.unla.epicentro_gourmet.entities.UnidadVenta;
import edu.unla.epicentro_gourmet.entities.Usuario;
import edu.unla.epicentro_gourmet.repositories.IEmpleadoRepository;
import edu.unla.epicentro_gourmet.repositories.IFestivalRepository;
import edu.unla.epicentro_gourmet.repositories.IPedidoRepository;
import edu.unla.epicentro_gourmet.repositories.IPlatoRepository;
import edu.unla.epicentro_gourmet.repositories.IUnidadVentaRepository;
import edu.unla.epicentro_gourmet.repositories.IUsuarioRepository;
import edu.unla.epicentro_gourmet.services.IPlatoEstrellaService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDate;
import java.time.LocalDateTime;

import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.nullValue;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.model;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.redirectedUrl;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.view;

@SpringBootTest
@AutoConfigureMockMvc
@WithMockUser(username = PruebasPlatoEstrellaTests.EMAIL_RESPONSABLE, authorities = "RESPONSABLE")
class PruebasPlatoEstrellaTests {
    static final String EMAIL_RESPONSABLE = "responsable.estrella@epicentro.com";

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private IPlatoEstrellaService platoEstrellaService;

    @Autowired
    private IPedidoRepository pedidoRepository;

    @Autowired
    private IEmpleadoRepository empleadoRepository;

    @Autowired
    private IPlatoRepository platoRepository;

    @Autowired
    private IUnidadVentaRepository unidadVentaRepository;

    @Autowired
    private IFestivalRepository festivalRepository;

    @Autowired
    private IUsuarioRepository usuarioRepository;

    private Festival festival;
    private UnidadVenta unidad;
    private UnidadVenta unidadAjena;
    private Plato empanada;
    private Plato sanguche;

    @BeforeEach
    void prepararDatos() {
        pedidoRepository.deleteAll();
        empleadoRepository.deleteAll();
        platoRepository.deleteAll();
        unidadVentaRepository.deleteAll();
        festivalRepository.deleteAll();
        usuarioRepository.deleteAll();

        festival = festivalRepository.save(new Festival("Festival con ventas", EnumTemporadaFestival.PRIMAVERA,
                LocalDate.of(2026, 11, 1), LocalDate.of(2026, 11, 3), null));
        Usuario responsable = usuarioRepository.save(new Usuario(EMAIL_RESPONSABLE, "clave", EnumRolUsuario.RESPONSABLE, true));
        unidad = unidadVentaRepository.save(new PuestoDesarmable("Puesto propio", "PD-0001", 12.0, festival, 2, 45, responsable));
        unidadAjena = unidadVentaRepository.save(new PuestoDesarmable("Puesto ajeno", "PD-0002", 10.0, festival, 1, 30, null));
        empanada = platoRepository.save(new Plato("Empanada", 3500, 1200, unidad));
        sanguche = platoRepository.save(new Plato("Sanguche", 6200, 2300, unidad));
    }

    private void guardarPedido(UnidadVenta unidadDelPedido, boolean cerrado, Object... platosYCantidades) {
        Pedido pedido = new Pedido(LocalDateTime.now(), unidadDelPedido);
        pedido.setFestival(festival);
        pedido.setCerrado(cerrado);
        for (int i = 0; i < platosYCantidades.length; i += 2) {
            pedido.getItems().add(new ItemPedido((Plato) platosYCantidades[i], pedido, (int) platosYCantidades[i + 1]));
        }
        pedidoRepository.save(pedido);
    }

    @Test
    void testElPlatoEstrellaEsElDeMasUnidadesAunqueAparezcaEnMenosPedidos() {
        guardarPedido(unidad, true, empanada, 3, sanguche, 5);
        guardarPedido(unidad, true, empanada, 1);

        PlatoEstrellaDTO estrella = platoEstrellaService.platoEstrella(unidad).orElseThrow();

        assertEquals("Sanguche", estrella.getPlato().getNombre());
        assertEquals(5, estrella.getUnidadesPedidas());
        assertEquals(6200.0, estrella.getPlato().getPrecioVenta());
        assertEquals(2300.0, estrella.getPlato().getCostoProduccion());
    }

    @Test
    void testSeSumanLasUnidadesDeTodosLosPedidos() {
        guardarPedido(unidad, true, empanada, 3, sanguche, 5);
        guardarPedido(unidad, true, empanada, 4);

        PlatoEstrellaDTO estrella = platoEstrellaService.platoEstrella(unidad).orElseThrow();

        assertEquals("Empanada", estrella.getPlato().getNombre());
        assertEquals(7, estrella.getUnidadesPedidas());
    }

    @Test
    void testNoCuentanLosPedidosAbiertosNiLosDeOtraUnidad() {
        guardarPedido(unidad, true, empanada, 2);
        guardarPedido(unidad, false, sanguche, 50);
        Plato platoAjeno = platoRepository.save(new Plato("Plato ajeno", 1000, 400, unidadAjena));
        guardarPedido(unidadAjena, true, platoAjeno, 99);

        PlatoEstrellaDTO estrella = platoEstrellaService.platoEstrella(unidad).orElseThrow();

        assertEquals("Empanada", estrella.getPlato().getNombre());
        assertEquals(2, estrella.getUnidadesPedidas());
    }

    @Test
    void testEnUnEmpateGanaElPlatoCargadoPrimero() {
        guardarPedido(unidad, true, sanguche, 4, empanada, 4);

        assertEquals("Empanada", platoEstrellaService.platoEstrella(unidad).orElseThrow().getPlato().getNombre());
    }

    @Test
    void testUnPlatoDadoDeBajaSigueContandoPorLoYaVendido() {
        guardarPedido(unidad, true, sanguche, 9, empanada, 2);
        sanguche.setActivo(false);
        platoRepository.save(sanguche);

        assertEquals("Sanguche", platoEstrellaService.platoEstrella(unidad).orElseThrow().getPlato().getNombre());
    }

    @Test
    void testSinPedidosCerradosNoHayPlatoEstrella() {
        guardarPedido(unidad, false, empanada, 5);

        assertTrue(platoEstrellaService.platoEstrella(unidad).isEmpty());
    }

    @Test
    void testLaVistaMuestraElPlatoEstrella() throws Exception {
        guardarPedido(unidad, true, sanguche, 5, empanada, 3);

        mockMvc.perform(get("/responsable/reportes/plato-estrella"))
                .andExpect(status().isOk())
                .andExpect(view().name("responsable/reporte-plato-estrella"))
                .andExpect(model().attributeExists("estrella"))
                .andExpect(content().string(containsString("Sanguche")))
                .andExpect(content().string(containsString("Puesto propio")))
                .andExpect(content().string(containsString("Festival con ventas")));
    }

    @Test
    void testLaVistaAvisaSiNoHayPedidosCerrados() throws Exception {
        mockMvc.perform(get("/responsable/reportes/plato-estrella"))
                .andExpect(status().isOk())
                .andExpect(model().attribute("estrella", nullValue()))
                .andExpect(content().string(containsString("no hay un plato estrella")));
    }

    @Test
    void testResponsableSinUnidadEsRedirigido() throws Exception {
        mockMvc.perform(get("/responsable/reportes/plato-estrella")
                        .with(SecurityMockMvcRequestPostProcessors.user("sin.unidad@epicentro.com")
                                .authorities(new SimpleGrantedAuthority("RESPONSABLE"))))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/responsable/empleados"));
    }

    @Test
    @WithMockUser(username = "cajero@epicentro.com", authorities = "EMPLEADO")
    void testUnEmpleadoNoPuedeVerElPlatoEstrella() throws Exception {
        mockMvc.perform(get("/responsable/reportes/plato-estrella"))
                .andExpect(status().isForbidden());
    }
}
