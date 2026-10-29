package edu.unla.epicentro_gourmet;

import edu.unla.epicentro_gourmet.entities.Cajero;
import edu.unla.epicentro_gourmet.entities.Cocinero;
import edu.unla.epicentro_gourmet.entities.Costos;
import edu.unla.epicentro_gourmet.entities.EnumCategoriaCocinero;
import edu.unla.epicentro_gourmet.entities.EnumRolUsuario;
import edu.unla.epicentro_gourmet.entities.EnumTemporadaFestival;
import edu.unla.epicentro_gourmet.entities.EnumTurnoCajero;
import edu.unla.epicentro_gourmet.entities.Festival;
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

import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.not;
import static org.hamcrest.Matchers.nullValue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.model;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.redirectedUrl;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.view;

@SpringBootTest
@AutoConfigureMockMvc
@WithMockUser(username = PruebasLiquidacionVistasTests.EMAIL_RESPONSABLE, authorities = "RESPONSABLE")
class PruebasLiquidacionVistasTests {
    static final String EMAIL_RESPONSABLE = "responsable.liquidacion@epicentro.com";

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

    private Usuario responsable;

    @BeforeEach
    void limpiarDatos() {
        pedidoRepository.deleteAll();
        empleadoRepository.deleteAll();
        platoRepository.deleteAll();
        unidadVentaRepository.deleteAll();
        festivalRepository.deleteAll();
        usuarioRepository.deleteAll();
        responsable = usuarioRepository.save(new Usuario(EMAIL_RESPONSABLE, "clave", EnumRolUsuario.RESPONSABLE, true));
    }

    private Festival festivalDe20Dias() {
        Costos costos = new Costos(500, 10, 2000, 50000, 100000, 150000, 5000, 100000);
        return festivalRepository.save(new Festival("Festival con costos", EnumTemporadaFestival.PRIMAVERA,
                LocalDate.of(2026, 11, 1), LocalDate.of(2026, 11, 20), costos));
    }

    private UnidadVenta unidadPropia(Festival festival) {
        return unidadVentaRepository.save(new PuestoDesarmable("Puesto propio", "PD-0001", 20.0, festival, 2, 45, responsable));
    }

    @Test
    void testListaLaLiquidacionDelPersonalActivoDeLaUnidad() throws Exception {
        UnidadVenta unidad = unidadPropia(festivalDe20Dias());
        UnidadVenta ajena = unidadVentaRepository.save(new PuestoDesarmable("Puesto ajeno", "PD-0002", 10.0,
                unidad.getFestival(), 1, 30, null));
        empleadoRepository.save(new Cocinero("Lucia", "Fernandez", 30123456, LocalDate.of(1995, 3, 12),
                LocalDate.of(2016, 1, 1), 1, unidad, EnumCategoriaCocinero.COCINERO, null));
        empleadoRepository.save(new Cajero("Mateo", "Gomez", 28987654, LocalDate.of(1993, 7, 20),
                LocalDate.of(2023, 11, 1), 1, unidad, EnumTurnoCajero.MANANA, null));
        Cajero dadoDeBaja = new Cajero("Baja", "Dada", 20111222, LocalDate.of(1990, 1, 1),
                LocalDate.of(2020, 1, 1), 1, unidad, EnumTurnoCajero.NOCHE, null);
        dadoDeBaja.setActivo(false);
        empleadoRepository.save(dadoDeBaja);
        empleadoRepository.save(new Cajero("Otro", "Equipo", 20333444, LocalDate.of(1990, 1, 1),
                LocalDate.of(2020, 1, 1), 1, ajena, EnumTurnoCajero.TARDE, null));

        mockMvc.perform(get("/responsable/liquidacion"))
                .andExpect(status().isOk())
                .andExpect(view().name("responsable/liquidacion"))
                .andExpect(model().attribute("liquidaciones", hasSize(2)))
                .andExpect(model().attribute("totalPersonal", is(365000.0)))
                .andExpect(content().string(containsString("Lucia Fernandez")))
                .andExpect(content().string(containsString("Mateo Gomez")))
                .andExpect(content().string(containsString("Festival con costos")))
                .andExpect(content().string(not(containsString("Baja Dada"))))
                .andExpect(content().string(not(containsString("Otro Equipo"))));
    }

    @Test
    void testUnidadSinPersonalMuestraElAviso() throws Exception {
        unidadPropia(festivalDe20Dias());

        mockMvc.perform(get("/responsable/liquidacion"))
                .andExpect(status().isOk())
                .andExpect(model().attribute("liquidaciones", hasSize(0)))
                .andExpect(content().string(containsString("No hay empleados activos")));
    }

    @Test
    void testFestivalSinCostosAvisaQueNoSePuedeLiquidar() throws Exception {
        Festival sinCostos = festivalRepository.save(new Festival("Sin costos", EnumTemporadaFestival.VERANO,
                LocalDate.of(2026, 11, 1), LocalDate.of(2026, 11, 3), null));
        unidadPropia(sinCostos);

        mockMvc.perform(get("/responsable/liquidacion"))
                .andExpect(status().isOk())
                .andExpect(model().attribute("liquidaciones", nullValue()))
                .andExpect(content().string(containsString("no tiene costos configurados")));
    }

    @Test
    void testResponsableSinUnidadEsRedirigido() throws Exception {
        mockMvc.perform(get("/responsable/liquidacion"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/responsable/empleados"));
    }

    @Test
    @WithMockUser(username = "cajero@epicentro.com", authorities = "EMPLEADO")
    void testUnEmpleadoNoPuedeVerLaLiquidacion() throws Exception {
        mockMvc.perform(get("/responsable/liquidacion"))
                .andExpect(status().isForbidden());
    }
}
