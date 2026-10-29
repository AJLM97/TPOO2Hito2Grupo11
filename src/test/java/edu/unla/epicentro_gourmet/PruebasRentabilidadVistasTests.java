package edu.unla.epicentro_gourmet;

import edu.unla.epicentro_gourmet.entities.Cocinero;
import edu.unla.epicentro_gourmet.entities.Costos;
import edu.unla.epicentro_gourmet.entities.EnumCategoriaCocinero;
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
@WithMockUser(username = PruebasRentabilidadVistasTests.EMAIL_RESPONSABLE, authorities = "RESPONSABLE")
class PruebasRentabilidadVistasTests {
    static final String EMAIL_RESPONSABLE = "responsable.rentabilidad@epicentro.com";
    private static final double NETA_DE_200_EMPANADAS = 700000.0 - 240000 - 250000 - 9550;

    @Autowired
    private MockMvc mockMvc;

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

        Costos costos = new Costos(500, 10, 2000, 50000, 100000, 150000, 5000, 100000);
        festival = festivalRepository.save(new Festival("Festival con ventas", EnumTemporadaFestival.PRIMAVERA,
                LocalDate.of(2026, 11, 1), LocalDate.of(2026, 11, 20), costos));
        Usuario responsable = usuarioRepository.save(new Usuario(EMAIL_RESPONSABLE, "clave", EnumRolUsuario.RESPONSABLE, true));
        unidad = unidadVentaRepository.save(new PuestoDesarmable("Puesto propio", "PD-0001", 20.0, festival, 2, 45, responsable));
        unidadAjena = unidadVentaRepository.save(new PuestoDesarmable("Puesto ajeno", "PD-0002", 10.0, festival, 1, 30, null));
        empanada = platoRepository.save(new Plato("Empanada", 3500, 1200, unidad));
        empleadoRepository.save(new Cocinero("Lucia", "Fernandez", 30123456, LocalDate.of(1995, 3, 12),
                LocalDate.of(2016, 1, 1), unidad, EnumCategoriaCocinero.COCINERO, null));
    }

    private void guardarPedido(UnidadVenta unidadDelPedido, boolean cerrado, int cantidad, LocalDateTime fecha) {
        Pedido pedido = new Pedido(fecha, unidadDelPedido);
        pedido.setFestival(festival);
        pedido.setCerrado(cerrado);
        pedido.getItems().add(new ItemPedido(empanada, pedido, cantidad));
        pedidoRepository.save(pedido);
    }

    @Test
    void testRentabilidadDeTodoElFestivalCuentaSoloPedidosCerradosDeLaUnidad() throws Exception {
        guardarPedido(unidad, true, 120, LocalDateTime.of(2026, 11, 5, 12, 0));
        guardarPedido(unidad, true, 80, LocalDateTime.of(2026, 11, 15, 12, 0));
        guardarPedido(unidad, false, 50, LocalDateTime.of(2026, 11, 6, 12, 0));
        guardarPedido(unidadAjena, true, 70, LocalDateTime.of(2026, 11, 6, 12, 0));

        mockMvc.perform(get("/responsable/reportes/rentabilidad"))
                .andExpect(status().isOk())
                .andExpect(view().name("responsable/reporte-rentabilidad"))
                .andExpect(model().attribute("reporte", hasProperty("ingresos", is(700000.0))))
                .andExpect(model().attribute("reporte", hasProperty("rentabilidadNeta", is(NETA_DE_200_EMPANADAS))))
                .andExpect(content().string(containsString("Puesto propio")));
    }

    @Test
    void testRentabilidadEntreFechasCuentaSoloLosPedidosDelRango() throws Exception {
        guardarPedido(unidad, true, 200, LocalDateTime.of(2026, 11, 5, 12, 0));
        guardarPedido(unidad, true, 100, LocalDateTime.of(2026, 11, 15, 12, 0));
        guardarPedido(unidad, true, 30, LocalDateTime.of(2026, 11, 10, 23, 59));

        mockMvc.perform(get("/responsable/reportes/rentabilidad").param("desde", "2026-11-01").param("hasta", "2026-11-10"))
                .andExpect(status().isOk())
                .andExpect(model().attribute("reporte", hasProperty("ingresos", is(230 * 3500.0))))
                .andExpect(model().attribute("reporte", hasProperty("sueldos", is(250000.0))))
                .andExpect(model().attribute("reporte", hasProperty("canon", is(9550.0))))
                .andExpect(content().string(containsString("2026-11-10")));
    }

    @Test
    void testDesdePosteriorAHastaMuestraElError() throws Exception {
        mockMvc.perform(get("/responsable/reportes/rentabilidad").param("desde", "2026-11-10").param("hasta", "2026-11-05"))
                .andExpect(status().isOk())
                .andExpect(model().attribute("reporte", nullValue()))
                .andExpect(content().string(containsString("no puede ser posterior")));
    }

    @Test
    void testRangoFueraDelFestivalMuestraElError() throws Exception {
        mockMvc.perform(get("/responsable/reportes/rentabilidad").param("desde", "2026-10-01").param("hasta", "2026-11-05"))
                .andExpect(status().isOk())
                .andExpect(model().attribute("reporte", nullValue()))
                .andExpect(content().string(containsString("dentro de las fechas del festival")));
    }

    @Test
    void testConUnaSolaFechaPideLasDos() throws Exception {
        mockMvc.perform(get("/responsable/reportes/rentabilidad").param("desde", "2026-11-05"))
                .andExpect(status().isOk())
                .andExpect(model().attribute("reporte", nullValue()))
                .andExpect(content().string(containsString("Indicá las dos fechas")));
    }

    @Test
    void testFestivalSinCostosAvisaQueNoSePuedeCalcular() throws Exception {
        pedidoRepository.deleteAll();
        empleadoRepository.deleteAll();
        platoRepository.deleteAll();
        unidadVentaRepository.deleteAll();
        Festival sinCostos = festivalRepository.save(new Festival("Sin costos", EnumTemporadaFestival.VERANO,
                LocalDate.of(2026, 11, 1), LocalDate.of(2026, 11, 20), null));
        unidadVentaRepository.save(new PuestoDesarmable("Puesto sin costos", "PD-0009", 20.0, sinCostos, 2, 45,
                usuarioRepository.findAll().get(0)));

        mockMvc.perform(get("/responsable/reportes/rentabilidad"))
                .andExpect(status().isOk())
                .andExpect(model().attribute("reporte", nullValue()))
                .andExpect(content().string(containsString("no tiene costos configurados")));
    }

    @Test
    void testResponsableSinUnidadEsRedirigido() throws Exception {
        mockMvc.perform(get("/responsable/reportes/rentabilidad")
                        .with(org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors
                                .user("sin.unidad@epicentro.com").authorities(
                                        new org.springframework.security.core.authority.SimpleGrantedAuthority("RESPONSABLE"))))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/responsable/empleados"));
    }

    @Test
    @WithMockUser(username = "cajero@epicentro.com", authorities = "EMPLEADO")
    void testUnEmpleadoNoPuedeVerLaRentabilidad() throws Exception {
        mockMvc.perform(get("/responsable/reportes/rentabilidad"))
                .andExpect(status().isForbidden());
    }
}
