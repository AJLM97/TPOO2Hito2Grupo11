package edu.unla.epicentro_gourmet.controllers;

import edu.unla.epicentro_gourmet.dtos.ItemPedidoDTO;
import edu.unla.epicentro_gourmet.entities.Cajero;
import edu.unla.epicentro_gourmet.entities.Empleado;
import edu.unla.epicentro_gourmet.entities.ItemPedido;
import edu.unla.epicentro_gourmet.entities.Pedido;
import edu.unla.epicentro_gourmet.entities.UnidadVenta;
import edu.unla.epicentro_gourmet.helpers.ViewRouteHelper;
import edu.unla.epicentro_gourmet.services.EnumResultadoPedido;
import edu.unla.epicentro_gourmet.services.IEmpleadoService;
import edu.unla.epicentro_gourmet.services.IPedidoService;
import edu.unla.epicentro_gourmet.services.IPlatoService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import org.springframework.web.servlet.view.RedirectView;

import java.util.Comparator;
import java.util.List;
import java.util.Optional;

@Controller
@PreAuthorize("hasAuthority('EMPLEADO')")
@RequestMapping("/empleado")
public class EmpleadoController {

    private static final String REDIRECT_PEDIDOS = "redirect:" + ViewRouteHelper.REDIRECT_CAJERO_PEDIDOS;

    private final IEmpleadoService empleadoService;
    private final IPedidoService pedidoService;
    private final IPlatoService platoService;

    public EmpleadoController(IEmpleadoService empleadoService, IPedidoService pedidoService,
                              IPlatoService platoService) {
        this.empleadoService = empleadoService;
        this.pedidoService = pedidoService;
        this.platoService = platoService;
    }

    @GetMapping("")
    public RedirectView index() {
        return new RedirectView(ViewRouteHelper.REDIRECT_CAJERO_PEDIDOS);
    }

    @GetMapping("/pedidos")
    public String showAllPedido(Model model, Authentication authentication) {
        UnidadVenta unidad = unidadDelCajero(authentication);
        model.addAttribute("pedidos", pedidoService.findByUnidadVenta(unidad));
        return ViewRouteHelper.CAJERO_PEDIDOS;
    }

    @PostMapping("/pedidos/abrir")
    public String openPedido(Authentication authentication) {
        Pedido pedido = pedidoService.abrir(unidadDelCajero(authentication));
        return "redirect:/empleado/pedidos/" + pedido.getId();
    }

    @GetMapping("/pedidos/{id}")
    public String showPedido(@PathVariable("id") long id, Model model, Authentication authentication,
                             RedirectAttributes redirectAttributes) {
        UnidadVenta unidad = unidadDelCajero(authentication);
        if (!model.containsAttribute("itemPedido")) {
            model.addAttribute("itemPedido", new ItemPedidoDTO());
        }
        return mostrarPedido(id, unidad, model, redirectAttributes);
    }

    @PostMapping("/pedidos/{id}/items")
    public String addPlato(@PathVariable("id") long id,
                           @Valid @ModelAttribute("itemPedido") ItemPedidoDTO itemPedido, BindingResult result,
                           Model model, Authentication authentication, RedirectAttributes redirectAttributes) {
        UnidadVenta unidad = unidadDelCajero(authentication);
        if (result.hasErrors()) {
            return mostrarPedido(id, unidad, model, redirectAttributes);
        }

        EnumResultadoPedido resultado = pedidoService.agregarPlato(unidad, id, itemPedido.getIdPlato(), itemPedido.getCantidad());
        if (resultado == EnumResultadoPedido.PLATO_NO_DISPONIBLE) {
            result.rejectValue("idPlato", "noDisponible", mensajeError(resultado));
            return mostrarPedido(id, unidad, model, redirectAttributes);
        }
        if (resultado == EnumResultadoPedido.CANTIDAD_INVALIDA) {
            result.rejectValue("cantidad", "invalida", mensajeError(resultado));
            return mostrarPedido(id, unidad, model, redirectAttributes);
        }
        return redirigir(resultado, id, null, redirectAttributes);
    }

    @PostMapping("/pedidos/{id}/items/{idItem}/cantidad")
    public String updateCantidad(@PathVariable("id") long id, @PathVariable("idItem") long idItem,
                                 @RequestParam(value = "cantidad", defaultValue = "0") long cantidad,
                                 Authentication authentication, RedirectAttributes redirectAttributes) {
        EnumResultadoPedido resultado = pedidoService.modificarCantidad(unidadDelCajero(authentication), id, idItem, cantidad);
        return redirigir(resultado, id, null, redirectAttributes);
    }

    @PostMapping("/pedidos/{id}/items/{idItem}/quitar")
    public String removePlato(@PathVariable("id") long id, @PathVariable("idItem") long idItem,
                              Authentication authentication, RedirectAttributes redirectAttributes) {
        EnumResultadoPedido resultado = pedidoService.quitarPlato(unidadDelCajero(authentication), id, idItem);
        return redirigir(resultado, id, null, redirectAttributes);
    }

    @PostMapping("/pedidos/{id}/cerrar")
    public String closePedido(@PathVariable("id") long id, Authentication authentication,
                              RedirectAttributes redirectAttributes) {
        EnumResultadoPedido resultado = pedidoService.cerrar(unidadDelCajero(authentication), id);
        return redirigir(resultado, id, "El pedido se cerró correctamente", redirectAttributes);
    }

    @PostMapping("/pedidos/{id}/cancelar")
    public String cancelPedido(@PathVariable("id") long id, Authentication authentication,
                               RedirectAttributes redirectAttributes) {
        EnumResultadoPedido resultado = pedidoService.cancelar(unidadDelCajero(authentication), id);
        if (resultado == EnumResultadoPedido.OK) {
            redirectAttributes.addFlashAttribute("mensaje", "El pedido se canceló");
            return REDIRECT_PEDIDOS;
        }
        return redirigir(resultado, id, null, redirectAttributes);
    }

    private String mostrarPedido(long id, UnidadVenta unidad, Model model, RedirectAttributes redirectAttributes) {
        Optional<Pedido> pedido = pedidoService.findByIdAndUnidadVenta(id, unidad);
        if (pedido.isEmpty()) {
            redirectAttributes.addFlashAttribute("error", mensajeError(EnumResultadoPedido.NO_ENCONTRADO));
            return REDIRECT_PEDIDOS;
        }
        List<ItemPedido> items = pedido.get().getItems().stream()
                .sorted(Comparator.comparing(item -> item.getPlato().getNombre(), String.CASE_INSENSITIVE_ORDER))
                .toList();
        model.addAttribute("pedido", pedido.get());
        model.addAttribute("items", items);
        model.addAttribute("platos", platoService.findByUnidadVenta(unidad));
        return ViewRouteHelper.CAJERO_PEDIDO_FORM;
    }

    private String redirigir(EnumResultadoPedido resultado, long id, String mensajeOk, RedirectAttributes redirectAttributes) {
        if (resultado == EnumResultadoPedido.NO_ENCONTRADO) {
            redirectAttributes.addFlashAttribute("error", mensajeError(resultado));
            return REDIRECT_PEDIDOS;
        }
        if (resultado != EnumResultadoPedido.OK) {
            redirectAttributes.addFlashAttribute("error", mensajeError(resultado));
        } else if (mensajeOk != null) {
            redirectAttributes.addFlashAttribute("mensaje", mensajeOk);
        }
        return "redirect:/empleado/pedidos/" + id;
    }

    private String mensajeError(EnumResultadoPedido resultado) {
        return switch (resultado) {
            case NO_ENCONTRADO -> "El pedido no existe";
            case PEDIDO_CERRADO -> "El pedido ya está cerrado y no puede modificarse";
            case PLATO_NO_DISPONIBLE -> "El plato no está disponible en esta unidad";
            case CANTIDAD_INVALIDA -> "La cantidad debe estar entre 1 y " + IPedidoService.CANTIDAD_MAXIMA_POR_PLATO;
            case ITEM_NO_ENCONTRADO -> "El plato no forma parte del pedido";
            case SIN_PLATOS -> "No se puede cerrar un pedido sin platos";
            case OK -> "";
        };
    }

    // Solo un cajero activo con unidad asignada puede operar pedidos.
    private UnidadVenta unidadDelCajero(Authentication authentication) {
        Empleado empleado = empleadoService.findActivoByEmail(authentication.getName())
                .filter(e -> e instanceof Cajero)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.FORBIDDEN,
                        "Solo los cajeros activos pueden gestionar pedidos"));
        if (empleado.getUnidad() == null) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "El cajero no tiene una unidad de venta asignada");
        }
        return empleado.getUnidad();
    }
}
