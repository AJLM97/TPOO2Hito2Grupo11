package edu.unla.epicentro_gourmet;

import edu.unla.epicentro_gourmet.dtos.ReporteVentaDTO;
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
import edu.unla.epicentro_gourmet.services.IReporteVentaService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDate;
import java.time.LocalDateTime;

import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.allOf;
import static org.hamcrest.Matchers.hasProperty;
import static org.hamcrest.Matchers.is;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.model;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.redirectedUrl;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.view;

@SpringBootTest
@AutoConfigureMockMvc
@WithMockUser(username = PruebasReporteRecaudacionVistasTests.EMAIL_RESPONSABLE, authorities = "RESPONSABLE")
class PruebasReporteRecaudacionVistasTests {
    static final String EMAIL_RESPONSABLE = "responsable.recaudacion@epicentro.com";

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private IReporteVentaService reporteVentaService;

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
    }

    private void guardarPedido(UnidadVenta unidadDelPedido, boolean cerrado, int cantidad) {
        Pedido pedido = new Pedido(LocalDateTime.now(), unidadDelPedido);
        pedido.setFestival(festival);
        pedido.setCerrado(cerrado);
        pedido.getItems().add(new ItemPedido(empanada, pedido, cantidad));
        pedidoRepository.save(pedido);
    }

    @Test
    void testElReporteCuentaSoloLosPedidosCerradosDeLaUnidad() throws Exception {
        guardarPedido(unidad, true, 3);
        guardarPedido(unidad, true, 2);
        guardarPedido(unidad, false, 10);
        guardarPedido(unidadAjena, true, 7);

        mockMvc.perform(get("/responsable/reportes/recaudacion"))
                .andExpect(status().isOk())
                .andExpect(view().name("responsable/reporte-recaudacion"))
                .andExpect(model().attribute("reporte", hasProperty("recaudacion", is(5 * 3500.0))))
                .andExpect(model().attribute("reporte", hasProperty("cantidadPedidos", is(2L))))
                .andExpect(model().attribute("reporte", hasProperty("cantidadPlatos", is(5L))))
                .andExpect(content().string(containsString("Puesto propio")))
                .andExpect(content().string(containsString("Festival con ventas")));
    }

    @Test
    void testUnidadSinVentasMuestraElReporteEnCero() throws Exception {
        mockMvc.perform(get("/responsable/reportes/recaudacion"))
                .andExpect(status().isOk())
                .andExpect(model().attribute("reporte", hasProperty("recaudacion", is(0.0))))
                .andExpect(model().attribute("reporte", hasProperty("cantidadPedidos", is(0L))));
    }

    @Test
    void testElReporteDelFestivalSumaTodasLasUnidades() {
        guardarPedido(unidad, true, 3);
        guardarPedido(unidadAjena, true, 7);
        guardarPedido(unidadAjena, false, 4);

        ReporteVentaDTO reporte = reporteVentaService.reporteDeFestival(festival.getId()).orElseThrow();

        assertEquals(10 * 3500.0, reporte.getRecaudacion());
        assertEquals(2, reporte.getCantidadPedidos());
        assertEquals(10, reporte.getCantidadPlatos());
    }

    @Test
    @WithMockUser(authorities = "ADMINISTRADOR")
    void testAdminVeRankingDeUnidadesOrdenadoPorRecaudacionDePedidosCerrados() throws Exception {
        guardarPedido(unidad, true, 3);
        guardarPedido(unidad, false, 10);
        guardarPedido(unidadAjena, true, 7);
        UnidadVenta unidadSinVentas = unidadVentaRepository.save(
                new PuestoDesarmable("Puesto sin ventas", "PD-0003", 8.0, festival, 1, 30, null));
        Festival otroFestival = festivalRepository.save(new Festival("Otro festival", EnumTemporadaFestival.VERANO,
                LocalDate.of(2027, 1, 1), LocalDate.of(2027, 1, 3), null));
        UnidadVenta unidadOtroFestival = unidadVentaRepository.save(
                new PuestoDesarmable("Puesto de otro festival", "PD-0004", 8.0, otroFestival, 1, 30, null));
        Pedido pedidoOtroFestival = new Pedido(LocalDateTime.now(), unidadOtroFestival);
        pedidoOtroFestival.setFestival(otroFestival);
        pedidoOtroFestival.setCerrado(true);
        pedidoOtroFestival.getItems().add(new ItemPedido(empanada, pedidoOtroFestival, 20));
        pedidoRepository.save(pedidoOtroFestival);

        mockMvc.perform(get("/admin/ranking-recaudacion").param("festivalId", festival.getId().toString()))
                .andExpect(status().isOk())
                .andExpect(view().name("admin/ranking-recaudacion"))
                .andExpect(model().attribute("ranking", contains(
                        allOf(hasProperty("nombreUnidad", is("Puesto ajeno")),
                                hasProperty("recaudacion", is(7 * 3500.0))),
                        allOf(hasProperty("nombreUnidad", is("Puesto propio")),
                                hasProperty("recaudacion", is(3 * 3500.0))),
                        allOf(hasProperty("nombreUnidad", is(unidadSinVentas.getNombreComercial())),
                                hasProperty("recaudacion", is(0.0))))))
                .andExpect(content().string(containsString("Ranking de unidades por recaudación")))
                .andExpect(content().string(containsString("Festival: Festival con ventas")))
                .andExpect(content().string(org.hamcrest.Matchers.not(containsString("Puesto de otro festival"))));
    }

    @Test
    void testResponsableNoPuedeVerRankingDeAdmin() throws Exception {
        mockMvc.perform(get("/admin/ranking-recaudacion"))
                .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(authorities = "ADMINISTRADOR")
    void testRankingRedirigeSiElFestivalNoExiste() throws Exception {
        mockMvc.perform(get("/admin/ranking-recaudacion").param("festivalId", "999999"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/admin/festivales"));
    }

    @Test
    void testResponsableSinUnidadEsRedirigido() throws Exception {
        usuarioRepository.save(new Usuario("sin.unidad@epicentro.com", "clave", EnumRolUsuario.RESPONSABLE, true));

        mockMvc.perform(get("/responsable/reportes/recaudacion")
                        .with(org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors
                                .user("sin.unidad@epicentro.com").authorities(
                                        new org.springframework.security.core.authority.SimpleGrantedAuthority("RESPONSABLE"))))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/responsable/empleados"));
    }

    @Test
    @WithMockUser(username = "cajero@epicentro.com", authorities = "EMPLEADO")
    void testUnEmpleadoNoPuedeVerElReporte() throws Exception {
        mockMvc.perform(get("/responsable/reportes/recaudacion"))
                .andExpect(status().isForbidden());
    }
}
