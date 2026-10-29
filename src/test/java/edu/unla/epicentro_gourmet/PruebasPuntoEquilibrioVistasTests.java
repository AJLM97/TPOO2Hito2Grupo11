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
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors;
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
@WithMockUser(username = PruebasPuntoEquilibrioVistasTests.EMAIL_RESPONSABLE, authorities = "RESPONSABLE")
class PruebasPuntoEquilibrioVistasTests {
    static final String EMAIL_RESPONSABLE = "responsable.equilibrio@epicentro.com";

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
    private Plato empanada;
    private Usuario responsable;

    // Festival de 7 días en curso: el cocinero cobra 50% de 250.000 = 125.000 y el canon es 9.550.
    @BeforeEach
    void prepararDatos() {
        pedidoRepository.deleteAll();
        empleadoRepository.deleteAll();
        platoRepository.deleteAll();
        unidadVentaRepository.deleteAll();
        festivalRepository.deleteAll();
        usuarioRepository.deleteAll();

        Costos costos = new Costos(500, 10, 2000, 50000, 100000, 150000, 5000, 100000);
        festival = festivalRepository.save(new Festival("Festival en curso", EnumTemporadaFestival.PRIMAVERA,
                LocalDate.now().minusDays(1), LocalDate.now().plusDays(5), costos));
        responsable = usuarioRepository.save(new Usuario(EMAIL_RESPONSABLE, "clave", EnumRolUsuario.RESPONSABLE, true));
        unidad = unidadVentaRepository.save(new PuestoDesarmable("Puesto propio", "PD-0001", 20.0, festival, 2, 45, responsable));
        empanada = platoRepository.save(new Plato("Empanada", 3500, 1200, unidad));
        empleadoRepository.save(new Cocinero("Lucia", "Fernandez", 30123456, LocalDate.of(1995, 3, 12),
                LocalDate.of(2016, 1, 1), unidad, EnumCategoriaCocinero.COCINERO, null));
    }

    private void guardarPedidoCerrado(int cantidad) {
        Pedido pedido = new Pedido(LocalDateTime.now(), unidad);
        pedido.setFestival(festival);
        pedido.setCerrado(true);
        pedido.getItems().add(new ItemPedido(empanada, pedido, cantidad));
        pedidoRepository.save(pedido);
    }

    @Test
    void testMuestraQueLaUnidadYaEstaGanando() throws Exception {
        guardarPedidoCerrado(200);

        mockMvc.perform(get("/responsable/reportes/punto-equilibrio"))
                .andExpect(status().isOk())
                .andExpect(view().name("responsable/punto-equilibrio"))
                .andExpect(model().attribute("equilibrio", hasProperty("equilibrioAlcanzado", is(true))))
                .andExpect(model().attribute("equilibrio", hasProperty("sueldos", is(125000.0))))
                .andExpect(model().attribute("equilibrio", hasProperty("costosFijos", is(134550.0))))
                .andExpect(content().string(containsString("está ganando")))
                .andExpect(content().string(containsString("en curso")));
    }

    @Test
    void testMuestraCuantoFaltaSiTodaviaNoCubreLosCostos() throws Exception {
        guardarPedidoCerrado(10);

        mockMvc.perform(get("/responsable/reportes/punto-equilibrio"))
                .andExpect(status().isOk())
                .andExpect(model().attribute("equilibrio", hasProperty("equilibrioAlcanzado", is(false))))
                .andExpect(model().attribute("equilibrio", hasProperty("gananciaFaltante", is(134550.0 - 23000))))
                .andExpect(content().string(containsString("todavía no cubre")))
                .andExpect(content().string(containsString("Falta vender")));
    }

    @Test
    void testSinVentasMuestraLosCostosCompletosPorCubrir() throws Exception {
        mockMvc.perform(get("/responsable/reportes/punto-equilibrio"))
                .andExpect(status().isOk())
                .andExpect(model().attribute("equilibrio", hasProperty("gananciaFaltante", is(134550.0))))
                .andExpect(model().attribute("equilibrio", hasProperty("ventasNecesarias", nullValue())));
    }

    @Test
    void testFestivalSinCostosAvisaQueNoSePuedeCalcular() throws Exception {
        pedidoRepository.deleteAll();
        empleadoRepository.deleteAll();
        platoRepository.deleteAll();
        unidadVentaRepository.deleteAll();
        Festival sinCostos = festivalRepository.save(new Festival("Sin costos", EnumTemporadaFestival.VERANO,
                LocalDate.now().minusDays(1), LocalDate.now().plusDays(5), null));
        unidadVentaRepository.save(new PuestoDesarmable("Puesto sin costos", "PD-0009", 20.0, sinCostos, 2, 45, responsable));

        mockMvc.perform(get("/responsable/reportes/punto-equilibrio"))
                .andExpect(status().isOk())
                .andExpect(model().attribute("equilibrio", nullValue()))
                .andExpect(content().string(containsString("no tiene costos configurados")));
    }

    @Test
    void testResponsableSinUnidadEsRedirigido() throws Exception {
        mockMvc.perform(get("/responsable/reportes/punto-equilibrio")
                        .with(SecurityMockMvcRequestPostProcessors.user("sin.unidad@epicentro.com")
                                .authorities(new SimpleGrantedAuthority("RESPONSABLE"))))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/responsable/empleados"));
    }

    @Test
    @WithMockUser(username = "cajero@epicentro.com", authorities = "EMPLEADO")
    void testUnEmpleadoNoPuedeVerElPuntoDeEquilibrio() throws Exception {
        mockMvc.perform(get("/responsable/reportes/punto-equilibrio"))
                .andExpect(status().isForbidden());
    }
}
