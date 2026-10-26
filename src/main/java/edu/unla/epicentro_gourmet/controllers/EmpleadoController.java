package edu.unla.epicentro_gourmet.controllers;

import edu.unla.epicentro_gourmet.entities.Pedido;
import edu.unla.epicentro_gourmet.helpers.ViewRouteHelper;
import edu.unla.epicentro_gourmet.services.IEmpleadoService;
import edu.unla.epicentro_gourmet.services.IPedidoService;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.view.RedirectView;

@Controller
@PreAuthorize("hasAuthority('EMPLEADO')")
@RequestMapping("/empleado")
public class EmpleadoController {

    private final IEmpleadoService empleadoService;
    private final IPedidoService pedidoService;

    public EmpleadoController(IEmpleadoService empleadoService, IPedidoService pedidoService) {
        this.empleadoService = empleadoService;
        this.pedidoService = pedidoService;

    }

    @GetMapping("")
    public RedirectView index() {
        return new RedirectView(ViewRouteHelper.REDIRECT_CAJERO_PEDIDOS);
    }

    @GetMapping("/pedidos")
    public String showAllPedido(Model model) {
        return ViewRouteHelper.CAJERO_PEDIDOS;
    }

    @GetMapping("/pedidos/nuevo")
    public String showFormPedido(Model model) {
        return ViewRouteHelper.CAJERO_PEDIDO_FORM;
    }

    @PostMapping("/pedidos/guardar")
    public RedirectView savePedido(@ModelAttribute("pedido") Pedido pedido) {
        return new RedirectView(ViewRouteHelper.REDIRECT_CAJERO_PEDIDOS);
    }
}