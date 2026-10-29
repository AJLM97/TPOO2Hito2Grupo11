package edu.unla.epicentro_gourmet;

import edu.unla.epicentro_gourmet.entities.Cajero;
import edu.unla.epicentro_gourmet.entities.Cocinero;
import edu.unla.epicentro_gourmet.entities.EnumCategoriaCocinero;
import edu.unla.epicentro_gourmet.entities.EnumRolUsuario;
import edu.unla.epicentro_gourmet.entities.EnumTemporadaFestival;
import edu.unla.epicentro_gourmet.entities.EnumTurnoCajero;
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
import jakarta.persistence.EntityManager;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.support.TransactionTemplate;

import java.time.LocalDate;
import java.time.LocalDateTime;
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
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.flash;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.model;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.redirectedUrl;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.view;

@SpringBootTest
@AutoConfigureMockMvc
@WithMockUser(username = PruebasGestionPedidosVistasTests.EMAIL_CAJERO, authorities = "EMPLEADO")
class PruebasGestionPedidosVistasTests {
    static final String EMAIL_CAJERO = "cajero.pedidos@epicentro.com";
    private static final String EMAIL_COCINERO = "cocinero.pedidos@epicentro.com";

    @Autowired
    private MockMvc mockMvc;
    @Autowired
    private IPedidoRepository pedidoRepository;
    @Autowired
    private IPlatoRepository platoRepository;
    @Autowired
    private IEmpleadoRepository empleadoRepository;
    @Autowired
    private IUnidadVentaRepository unidadVentaRepository;
    @Autowired
    private IFestivalRepository festivalRepository;
    @Autowired
    private IUsuarioRepository usuarioRepository;
    @Autowired
    private TransactionTemplate transaction;

    private UnidadVenta unidad;
    private UnidadVenta unidadAjena;
    private Plato empanada;
    private Plato limonada;
    private Plato platoAjeno;

    @BeforeEach
    void prepararDatos() {
        pedidoRepository.deleteAll();
        empleadoRepository.deleteAll();
        platoRepository.deleteAll();
        unidadVentaRepository.deleteAll();
        festivalRepository.deleteAll();
        usuarioRepository.deleteAll();

        Festival festival = festivalRepository.save(new Festival("Festival de prueba",
                EnumTemporadaFestival.PRIMAVERA, LocalDate.of(2026, 11, 1), LocalDate.of(2026, 11, 3), null));
        unidad = unidadVentaRepository.save(new PuestoDesarmable("Puesto propio", "PD-0001", 12.0, festival, 2, 45, null));
        unidadAjena = unidadVentaRepository.save(new PuestoDesarmable("Puesto ajeno", "PD-0002", 10.0, festival, 1, 30, null));

        Usuario usuarioCajero = new Usuario(EMAIL_CAJERO, "clave", EnumRolUsuario.EMPLEADO, true);
        empleadoRepository.save(new Cajero("Mateo", "Gomez", 28987654L, LocalDate.of(1993, 7, 20),
                LocalDate.of(2019, 6, 15), unidad, EnumTurnoCajero.MANANA, usuarioCajero));

        empanada = platoRepository.save(new Plato("Empanada", 3500, 1200, unidad));
        limonada = platoRepository.save(new Plato("Limonada", 2800, 700, unidad));
        platoAjeno = platoRepository.save(new Plato("Plato ajeno", 900, 300, unidadAjena));
    }

    private Pedido pedidoAbierto() {
        return abrirPedido(unidad, false);
    }

    private Pedido abrirPedido(UnidadVenta destino, boolean cerrado) {
        Pedido pedido = new Pedido(LocalDateTime.now(), destino);
        pedido.setFestival(destino.getFestival());
        pedido.setCerrado(cerrado);
        return pedidoRepository.save(pedido);
    }

    private Pedido conItem(Pedido pedido, Plato plato, long cantidad) {
        pedido.getItems().add(new ItemPedido(plato, pedido, cantidad));
        return pedidoRepository.save(pedido);
    }

    private Pedido recargar(Long id) {
        return transaction.execute(status -> {
            Pedido pedido = pedidoRepository.findById(id).orElseThrow();
            pedido.getItems().forEach(item -> item.getPlato().getNombre());
            return pedido;
        });
    }

    @Test
    void testListadoMuestraSoloLosPedidosDeLaUnidadDelCajero() throws Exception {
        Pedido propio = conItem(abrirPedido(unidad, true), empanada, 2);
        conItem(abrirPedido(unidadAjena, true), platoAjeno, 1);

        mockMvc.perform(get("/empleado/pedidos"))
                .andExpect(status().isOk())
                .andExpect(view().name("empleado/pedidos"))
                .andExpect(model().attribute("pedidos", hasSize(1)))
                .andExpect(content().string(containsString("Cerrado")))
                .andExpect(content().string(containsString("7000.00")))
                .andExpect(content().string(containsString("/empleado/pedidos/" + propio.getId())));
    }

    @Test
    void testAbrirUnPedidoLoCreaAbiertoConFechaDeHoyYElFestivalDeLaUnidad() throws Exception {
        mockMvc.perform(post("/empleado/pedidos/abrir"))
                .andExpect(status().is3xxRedirection());

        List<Pedido> pedidos = pedidoRepository.findAll();
        assertEquals(1, pedidos.size());
        Pedido pedido = recargar(pedidos.get(0).getId());
        assertFalse(pedido.isCerrado());
        assertEquals(LocalDate.now(), pedido.getFechaTransaccion().toLocalDate());
        assertEquals(unidad.getId(), pedido.getUnidadVenta().getId());
        assertEquals(unidad.getFestival().getId(), pedido.getFestival().getId());
        assertTrue(pedido.getItems().isEmpty());
    }

    @Test
    void testPantallaDelPedidoMuestraLosPlatosDisponiblesDeLaUnidad() throws Exception {
        Plato baja = new Plato("Plato dado de baja", 100, 50, unidad);
        baja.setActivo(false);
        platoRepository.save(baja);
        Pedido pedido = conItem(pedidoAbierto(), empanada, 2);

        mockMvc.perform(get("/empleado/pedidos/{id}", pedido.getId()))
                .andExpect(status().isOk())
                .andExpect(view().name("empleado/pedidos-form"))
                .andExpect(model().attribute("platos", hasSize(2)))
                .andExpect(content().string(containsString("Empanada")))
                .andExpect(content().string(containsString("Limonada")))
                .andExpect(content().string(not(containsString("Plato dado de baja"))))
                .andExpect(content().string(not(containsString("Plato ajeno"))))
                .andExpect(content().string(containsString("Cerrar pedido")));
    }

    @Test
    void testNoSePuedeVerUnPedidoDeOtraUnidad() throws Exception {
        Pedido ajeno = abrirPedido(unidadAjena, false);

        mockMvc.perform(get("/empleado/pedidos/{id}", ajeno.getId()))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/empleado/pedidos"))
                .andExpect(flash().attribute("error", "El pedido no existe"));
    }

    @Test
    void testAgregarUnPlatoAlPedido() throws Exception {
        Pedido pedido = pedidoAbierto();

        mockMvc.perform(post("/empleado/pedidos/{id}/items", pedido.getId())
                        .param("idPlato", empanada.getId().toString())
                        .param("cantidad", "3"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/empleado/pedidos/" + pedido.getId()));

        Pedido actualizado = recargar(pedido.getId());
        assertEquals(1, actualizado.getItems().size());
        assertEquals(3, actualizado.getItems().iterator().next().getCantidad());
        assertEquals(10500.0, actualizado.getTotal());
    }

    @Test
    void testAgregarUnPlatoYaCargadoSumaLaCantidad() throws Exception {
        Pedido pedido = conItem(pedidoAbierto(), empanada, 2);

        mockMvc.perform(post("/empleado/pedidos/{id}/items", pedido.getId())
                        .param("idPlato", empanada.getId().toString())
                        .param("cantidad", "3"))
                .andExpect(status().is3xxRedirection());

        Pedido actualizado = recargar(pedido.getId());
        assertEquals(1, actualizado.getItems().size());
        assertEquals(5, actualizado.getItems().iterator().next().getCantidad());
    }

    @Test
    void testCantidadFueraDeRangoSeRechaza() throws Exception {
        Pedido pedido = pedidoAbierto();

        for (String cantidad : List.of("0", "-2", "100")) {
            mockMvc.perform(post("/empleado/pedidos/{id}/items", pedido.getId())
                            .param("idPlato", empanada.getId().toString())
                            .param("cantidad", cantidad))
                    .andExpect(status().isOk())
                    .andExpect(view().name("empleado/pedidos-form"))
                    .andExpect(model().attributeHasFieldErrors("itemPedido", "cantidad"));
        }
        assertTrue(recargar(pedido.getId()).getItems().isEmpty());
    }

    @Test
    void testSumarCantidadQueSuperaElMaximoSeRechaza() throws Exception {
        Pedido pedido = conItem(pedidoAbierto(), empanada, 95);

        mockMvc.perform(post("/empleado/pedidos/{id}/items", pedido.getId())
                        .param("idPlato", empanada.getId().toString())
                        .param("cantidad", "10"))
                .andExpect(status().isOk())
                .andExpect(model().attributeHasFieldErrors("itemPedido", "cantidad"));

        assertEquals(95, recargar(pedido.getId()).getItems().iterator().next().getCantidad());
    }

    @Test
    void testNoSePuedeAgregarUnPlatoDeOtraUnidadNiUnoDadoDeBaja() throws Exception {
        Plato baja = new Plato("Plato dado de baja", 100, 50, unidad);
        baja.setActivo(false);
        baja = platoRepository.save(baja);
        Pedido pedido = pedidoAbierto();

        for (Plato noDisponible : List.of(platoAjeno, baja)) {
            mockMvc.perform(post("/empleado/pedidos/{id}/items", pedido.getId())
                            .param("idPlato", noDisponible.getId().toString())
                            .param("cantidad", "1"))
                    .andExpect(status().isOk())
                    .andExpect(model().attributeHasFieldErrors("itemPedido", "idPlato"))
                    .andExpect(content().string(containsString("El plato no est")));
        }
        assertTrue(recargar(pedido.getId()).getItems().isEmpty());
    }

    @Test
    void testSinSeleccionarPlatoSeMuestraElError() throws Exception {
        Pedido pedido = pedidoAbierto();

        mockMvc.perform(post("/empleado/pedidos/{id}/items", pedido.getId())
                        .param("cantidad", "1"))
                .andExpect(status().isOk())
                .andExpect(model().attributeHasFieldErrors("itemPedido", "idPlato"));
    }

    @Test
    void testModificarLaCantidadDeUnPlato() throws Exception {
        Pedido pedido = conItem(pedidoAbierto(), empanada, 2);
        ItemPedido item = recargar(pedido.getId()).getItems().iterator().next();

        mockMvc.perform(post("/empleado/pedidos/{id}/items/{idItem}/cantidad", pedido.getId(), item.getIdItemPedido())
                        .param("cantidad", "7"))
                .andExpect(status().is3xxRedirection());

        assertEquals(7, recargar(pedido.getId()).getItems().iterator().next().getCantidad());
    }

    @Test
    void testModificarConCantidadInvalidaMuestraElErrorYNoCambia() throws Exception {
        Pedido pedido = conItem(pedidoAbierto(), empanada, 2);
        ItemPedido item = recargar(pedido.getId()).getItems().iterator().next();

        mockMvc.perform(post("/empleado/pedidos/{id}/items/{idItem}/cantidad", pedido.getId(), item.getIdItemPedido())
                        .param("cantidad", "0"))
                .andExpect(status().is3xxRedirection())
                .andExpect(flash().attributeExists("error"));

        assertEquals(2, recargar(pedido.getId()).getItems().iterator().next().getCantidad());
    }

    @Test
    void testQuitarUnPlatoDelPedido() throws Exception {
        Pedido pedido = conItem(conItem(pedidoAbierto(), empanada, 2), limonada, 1);
        ItemPedido item = recargar(pedido.getId()).getItems().stream()
                .filter(i -> i.getPlato().getId().equals(empanada.getId())).findFirst().orElseThrow();

        mockMvc.perform(post("/empleado/pedidos/{id}/items/{idItem}/quitar", pedido.getId(), item.getIdItemPedido()))
                .andExpect(status().is3xxRedirection());

        Pedido actualizado = recargar(pedido.getId());
        assertEquals(1, actualizado.getItems().size());
        assertEquals("Limonada", actualizado.getItems().iterator().next().getPlato().getNombre());
    }

    @Test
    void testCerrarUnPedidoConPlatos() throws Exception {
        Pedido pedido = conItem(pedidoAbierto(), empanada, 1);

        mockMvc.perform(post("/empleado/pedidos/{id}/cerrar", pedido.getId()))
                .andExpect(status().is3xxRedirection())
                .andExpect(flash().attribute("mensaje", "El pedido se cerró correctamente"));

        assertTrue(recargar(pedido.getId()).isCerrado());
    }

    @Test
    void testNoSePuedeCerrarUnPedidoSinPlatos() throws Exception {
        Pedido pedido = pedidoAbierto();

        mockMvc.perform(post("/empleado/pedidos/{id}/cerrar", pedido.getId()))
                .andExpect(status().is3xxRedirection())
                .andExpect(flash().attribute("error", "No se puede cerrar un pedido sin platos"));

        assertFalse(recargar(pedido.getId()).isCerrado());
    }

    @Test
    void testUnPedidoCerradoNoPuedeModificarseNiCancelarse() throws Exception {
        Pedido pedido = conItem(abrirPedido(unidad, true), empanada, 2);
        ItemPedido item = recargar(pedido.getId()).getItems().iterator().next();
        String cerrado = "El pedido ya está cerrado y no puede modificarse";

        mockMvc.perform(post("/empleado/pedidos/{id}/items", pedido.getId())
                        .param("idPlato", limonada.getId().toString()).param("cantidad", "1"))
                .andExpect(flash().attribute("error", cerrado));
        mockMvc.perform(post("/empleado/pedidos/{id}/items/{idItem}/cantidad", pedido.getId(), item.getIdItemPedido())
                        .param("cantidad", "9"))
                .andExpect(flash().attribute("error", cerrado));
        mockMvc.perform(post("/empleado/pedidos/{id}/items/{idItem}/quitar", pedido.getId(), item.getIdItemPedido()))
                .andExpect(flash().attribute("error", cerrado));
        mockMvc.perform(post("/empleado/pedidos/{id}/cerrar", pedido.getId()))
                .andExpect(flash().attribute("error", cerrado));
        mockMvc.perform(post("/empleado/pedidos/{id}/cancelar", pedido.getId()))
                .andExpect(flash().attribute("error", cerrado));

        Pedido sinCambios = recargar(pedido.getId());
        assertTrue(sinCambios.isCerrado());
        assertEquals(1, sinCambios.getItems().size());
        assertEquals(2, sinCambios.getItems().iterator().next().getCantidad());
    }

    @Test
    void testLaPantallaDeUnPedidoCerradoEsSoloLectura() throws Exception {
        Pedido pedido = conItem(abrirPedido(unidad, true), empanada, 2);

        mockMvc.perform(get("/empleado/pedidos/{id}", pedido.getId()))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("Cerrado")))
                .andExpect(content().string(not(containsString("Cerrar pedido"))))
                .andExpect(content().string(not(containsString("Agregar plato"))));
    }

    @Test
    void testCancelarUnPedidoAbiertoLoEliminaConSusPlatos() throws Exception {
        Pedido pedido = conItem(pedidoAbierto(), empanada, 2);

        mockMvc.perform(post("/empleado/pedidos/{id}/cancelar", pedido.getId()))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/empleado/pedidos"));

        assertTrue(pedidoRepository.findById(pedido.getId()).isEmpty());
        assertTrue(platoRepository.findById(empanada.getId()).isPresent());
    }

    @Test
    void testNoSePuedeOperarUnPedidoDeOtraUnidad() throws Exception {
        Pedido ajeno = conItem(abrirPedido(unidadAjena, false), platoAjeno, 1);

        mockMvc.perform(post("/empleado/pedidos/{id}/cerrar", ajeno.getId()))
                .andExpect(flash().attribute("error", "El pedido no existe"));
        mockMvc.perform(post("/empleado/pedidos/{id}/cancelar", ajeno.getId()))
                .andExpect(flash().attribute("error", "El pedido no existe"));

        Pedido sinCambios = recargar(ajeno.getId());
        assertFalse(sinCambios.isCerrado());
        assertEquals(1, sinCambios.getItems().size());
    }

    @Test
    @WithMockUser(username = EMAIL_COCINERO, authorities = "EMPLEADO")
    void testUnCocineroNoPuedeGestionarPedidos() throws Exception {
        Usuario usuario = new Usuario(EMAIL_COCINERO, "clave", EnumRolUsuario.EMPLEADO, true);
        empleadoRepository.save(new Cocinero("Lucia", "Fernandez", 30123456L, LocalDate.of(1995, 3, 12),
                LocalDate.of(2020, 2, 1), unidad, EnumCategoriaCocinero.COCINERO, usuario));

        mockMvc.perform(get("/empleado/pedidos")).andExpect(status().isForbidden());
        mockMvc.perform(post("/empleado/pedidos/abrir")).andExpect(status().isForbidden());
        assertEquals(0, pedidoRepository.count());
    }

    @Test
    @WithMockUser(username = "sin.ficha@epicentro.com", authorities = "EMPLEADO")
    void testUnEmpleadoQueNoEsCajeroActivoRecibe403() throws Exception {
        mockMvc.perform(get("/empleado/pedidos")).andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(authorities = "RESPONSABLE")
    void testUnResponsableNoPuedeAccederALosPedidosDelCajero() throws Exception {
        mockMvc.perform(get("/empleado/pedidos")).andExpect(status().isForbidden());
    }
}
