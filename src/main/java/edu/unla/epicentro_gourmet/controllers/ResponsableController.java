package edu.unla.epicentro_gourmet.controllers;

import edu.unla.epicentro_gourmet.entities.Empleado;
import edu.unla.epicentro_gourmet.entities.Plato;
import edu.unla.epicentro_gourmet.helpers.ViewRouteHelper;
import edu.unla.epicentro_gourmet.services.IEmpleadoService;
import edu.unla.epicentro_gourmet.services.IPlatoService;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.view.RedirectView;

@Controller
@PreAuthorize("hasAuthority('RESPONSABLE')")
@RequestMapping("/responsable")
public class ResponsableController {

    private final IEmpleadoService empleadoService;
    private final IPlatoService platoService;

    public ResponsableController(IEmpleadoService empleadoService,
                                 IPlatoService platoService) {
        this.empleadoService = empleadoService;
        this.platoService = platoService;
    }

    @GetMapping("")
    public RedirectView index() {
        return new RedirectView("/responsable/empleados");
    }
    @GetMapping("/empleados")
    public String showAllEmpleado(Model model) {
        return ViewRouteHelper.RESPONSABLE_EMPLEADOS;
    }

    @GetMapping("/empleados/nuevo")
    public String showFormEmpleado(Model model) {
        return ViewRouteHelper.RESPONSABLE_EMPLEADO_FORM;
    }

    @PostMapping("/empleados/guardar")
    public RedirectView saveEmpleado(@ModelAttribute("empleado") Empleado empleado) {
        return new RedirectView("/responsable/empleados");
    }

    @GetMapping("/platos")
    public String showAllPlato(Model model) {
        return ViewRouteHelper.RESPONSABLE_PLATOS;
    }

    @GetMapping("/platos/nuevo")
    public String showFormPlato(Model model) {
        return ViewRouteHelper.RESPONSABLE_PLATO_FORM;
    }

    @PostMapping("/platos/guardar")
    public RedirectView savePlato(@ModelAttribute("plato") Plato plato) {
        return new RedirectView("/responsable/platos");
    }
}