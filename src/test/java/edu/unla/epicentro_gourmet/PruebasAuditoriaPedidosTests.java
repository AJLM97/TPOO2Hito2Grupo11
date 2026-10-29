package edu.unla.epicentro_gourmet;

import edu.unla.epicentro_gourmet.dtos.PedidoAuditoriaDTO;
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
import edu.unla.epicentro_gourmet.services.EnumFiltroEstadoPedido;
import edu.unla.epicentro_gourmet.services.IAuditoriaPedidosService;
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
import java.util.List;

import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.empty;
import static org.hamcrest.Matchers.hasSize;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.model;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.redirectedUrl;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.view;

@SpringBootTest
@AutoConfigureMockMvc
@WithMockUser(username = PruebasAuditoriaPedidosTests.EMAIL_RESPONSABLE, authorities = "RESPONSABLE")
class PruebasAuditoriaPedidosTests {
    static final String EMAIL_RESPONSABLE = "responsable.auditoria@epicentro.com";

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private IAuditoriaPedidosService auditoriaPedidosService;

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
    private long pedido1;
    private long pedido2;
    private long pedido3;

    @BeforeEach
    void prepararDatos() {
        pedidoRepository.deleteAll();
        empleadoRepository.deleteAll();
        platoRepository.deleteAll();
        unidadVentaRepository.deleteAll();
        festivalRepository.deleteAll();
        usuarioRepository.deleteAll();

        festival = festivalRepository.save(new Festival("Festival con ventas", EnumTemporadaFestival.PRIMAVERA,
                LocalDate.of(2026, 11, 1), LocalDate.of(2026, 11, 10), null));
        Usuario responsable = usuarioRepository.save(new Usuario(EMAIL_RESPONSABLE, "clave", EnumRolUsuario.RESPONSABLE, true));
        unidad = unidadVentaRepository.save(new PuestoDesarmable("Puesto propio", "PD-0001", 12.0, festival, 2, 45, responsable));
        unidadAjena = unidadVentaRepository.save(new PuestoDesarmable("Puesto ajeno", "PD-0002", 10.0, festival, 1, 30, null));
        empanada = platoRepository.save(new Plato("Empanada", 3500, 1200, unidad));
        sanguche = platoRepository.save(new Plato("Sanguche", 6200, 2300, unidad));

        pedido1 = guardarPedido(unidad, true, LocalDateTime.of(2026, 11, 2, 10, 0), empanada, 3, sanguche, 2);
        pedido2 = guardarPedido(unidad, false, LocalDateTime.of(2026, 11, 3, 18, 30), empanada, 1);
        pedido3 = guardarPedido(unidad, true, LocalDateTime.of(2026, 11, 5, 9, 0), sanguche, 4);
        Plato platoAjeno = platoRepository.save(new Plato("Plato ajeno", 1000, 400, unidadAjena));
        guardarPedido(unidadAjena, true, LocalDateTime.of(2026, 11, 3, 12, 0), platoAjeno, 9);
    }

    private long guardarPedido(UnidadVenta unidadDelPedido, boolean cerrado, LocalDateTime fecha, Object... platosYCantidades) {
        Pedido pedido = new Pedido(fecha, unidadDelPedido);
        pedido.setFestival(festival);
        pedido.setCerrado(cerrado);
        for (int i = 0; i < platosYCantidades.length; i += 2) {
            pedido.getItems().add(new ItemPedido((Plato) platosYCantidades[i], pedido, (int) platosYCantidades[i + 1]));
        }
        return pedidoRepository.save(pedido).getId();
    }

    private List<Long> ids(List<PedidoAuditoriaDTO> pedidos) {
        return pedidos.stream().map(PedidoAuditoriaDTO::getId).toList();
    }

    @Test
    void testSinFiltrosDevuelveLosPedidosDeLaUnidadDelMasRecienteAlMasAntiguo() {
        List<PedidoAuditoriaDTO> pedidos = auditoriaPedidosService.buscar(unidad, null, null, null, null);

        assertEquals(List.of(pedido3, pedido2, pedido1), ids(pedidos));
    }

    @Test
    void testCadaPedidoInformaItemsPlatosYTotal() {
        PedidoAuditoriaDTO primero = auditoriaPedidosService.buscar(unidad, null, null, null, null).stream()
                .filter(pedido -> pedido.getId() == pedido1).findFirst().orElseThrow();

        assertEquals(2, primero.getCantidadItems());
        assertEquals(5, primero.getCantidadPlatos());
        assertEquals(3 * 3500 + 2 * 6200.0, primero.getTotal());
        assertTrue(primero.isCerrado());
    }

    @Test
    void testFiltraPorEstado() {
        assertEquals(List.of(pedido2), ids(auditoriaPedidosService.buscar(unidad, null, null, EnumFiltroEstadoPedido.ABIERTO, null)));
        assertEquals(List.of(pedido3, pedido1), ids(auditoriaPedidosService.buscar(unidad, null, null, EnumFiltroEstadoPedido.CERRADO, null)));
        assertEquals(3, auditoriaPedidosService.buscar(unidad, null, null, EnumFiltroEstadoPedido.TODOS, null).size());
    }

    @Test
    void testFiltraPorPlato() {
        assertEquals(List.of(pedido2, pedido1), ids(auditoriaPedidosService.buscar(unidad, null, null, null, empanada.getId())));
        assertEquals(List.of(pedido3, pedido1), ids(auditoriaPedidosService.buscar(unidad, null, null, null, sanguche.getId())));
    }

    @Test
    void testFiltraPorRangoDeFechasIncluyendoLosExtremos() {
        List<PedidoAuditoriaDTO> pedidos = auditoriaPedidosService.buscar(unidad,
                LocalDate.of(2026, 11, 3), LocalDate.of(2026, 11, 5), null, null);

        assertEquals(List.of(pedido3, pedido2), ids(pedidos));
    }

    @Test
    void testAdmiteUnSoloExtremoDelRango() {
        assertEquals(List.of(pedido3), ids(auditoriaPedidosService.buscar(unidad, LocalDate.of(2026, 11, 4), null, null, null)));
        assertEquals(List.of(pedido2, pedido1), ids(auditoriaPedidosService.buscar(unidad, null, LocalDate.of(2026, 11, 3), null, null)));
    }

    @Test
    void testCombinaLosTresFiltros() {
        List<PedidoAuditoriaDTO> pedidos = auditoriaPedidosService.buscar(unidad, LocalDate.of(2026, 11, 3), null,
                EnumFiltroEstadoPedido.CERRADO, sanguche.getId());

        assertEquals(List.of(pedido3), ids(pedidos));
    }

    @Test
    void testSinCoincidenciasDevuelveListaVacia() {
        assertTrue(auditoriaPedidosService.buscar(unidad, null, null, EnumFiltroEstadoPedido.ABIERTO, sanguche.getId()).isEmpty());
    }

    @Test
    void testNoMuestraPedidosDeOtraUnidadAunqueSePidaSuPlato() {
        Plato platoAjeno = platoRepository.findAll().stream()
                .filter(plato -> plato.getNombre().equals("Plato ajeno")).findFirst().orElseThrow();

        assertTrue(auditoriaPedidosService.buscar(unidad, null, null, null, platoAjeno.getId()).isEmpty());
    }

    @Test
    void testRangoConDesdePosteriorAHastaEsInvalido() {
        LocalDate desde = LocalDate.of(2026, 11, 5);
        LocalDate hasta = LocalDate.of(2026, 11, 1);

        assertFalse(auditoriaPedidosService.rangoDeFechasValido(desde, hasta));
        assertTrue(auditoriaPedidosService.rangoDeFechasValido(hasta, desde));
        assertTrue(auditoriaPedidosService.rangoDeFechasValido(desde, desde));
        assertThrows(IllegalArgumentException.class, () -> auditoriaPedidosService.buscar(unidad, desde, hasta, null, null));
    }

    @Test
    void testLaVistaListaLosPedidosYLosPlatosParaFiltrar() throws Exception {
        mockMvc.perform(get("/responsable/pedidos"))
                .andExpect(status().isOk())
                .andExpect(view().name("responsable/auditoria-pedidos"))
                .andExpect(model().attribute("pedidos", hasSize(3)))
                .andExpect(model().attribute("platos", hasSize(2)))
                .andExpect(content().string(containsString("Abierto")))
                .andExpect(content().string(containsString("Cerrado")));
    }

    @Test
    void testLaVistaAplicaLosFiltros() throws Exception {
        mockMvc.perform(get("/responsable/pedidos")
                        .param("estado", "CERRADO")
                        .param("plato", String.valueOf(sanguche.getId()))
                        .param("desde", "2026-11-03"))
                .andExpect(status().isOk())
                .andExpect(model().attribute("pedidos", hasSize(1)));
    }

    @Test
    void testLaVistaAvisaSiNoHayCoincidencias() throws Exception {
        mockMvc.perform(get("/responsable/pedidos").param("estado", "ABIERTO").param("plato", String.valueOf(sanguche.getId())))
                .andExpect(status().isOk())
                .andExpect(model().attribute("pedidos", empty()))
                .andExpect(content().string(containsString("No hay pedidos que coincidan")));
    }

    @Test
    void testLaVistaMuestraElErrorSiDesdeEsPosteriorAHasta() throws Exception {
        mockMvc.perform(get("/responsable/pedidos").param("desde", "2026-11-05").param("hasta", "2026-11-01"))
                .andExpect(status().isOk())
                .andExpect(model().attribute("pedidos", empty()))
                .andExpect(content().string(containsString("no puede ser posterior")));
    }

    @Test
    void testResponsableSinUnidadEsRedirigido() throws Exception {
        mockMvc.perform(get("/responsable/pedidos")
                        .with(SecurityMockMvcRequestPostProcessors.user("sin.unidad@epicentro.com")
                                .authorities(new SimpleGrantedAuthority("RESPONSABLE"))))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/responsable/empleados"));
    }

    @Test
    @WithMockUser(username = "cajero@epicentro.com", authorities = "EMPLEADO")
    void testUnEmpleadoNoPuedeVerLaAuditoria() throws Exception {
        mockMvc.perform(get("/responsable/pedidos"))
                .andExpect(status().isForbidden());
    }
}
