package edu.unla.epicentro_gourmet.controllers;

import edu.unla.epicentro_gourmet.entities.Festival;
import edu.unla.epicentro_gourmet.entities.UnidadVenta;
import edu.unla.epicentro_gourmet.helpers.ViewRouteHelper;
import edu.unla.epicentro_gourmet.services.IFestivalService;
import edu.unla.epicentro_gourmet.services.IUnidadVentaService;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.view.RedirectView;

@Controller
@PreAuthorize("hasAuthority('ADMINISTRADOR')")
@RequestMapping("/admin")
public class AdminController {

    private final IFestivalService festivalService;
    private final IUnidadVentaService unidadVentaService;

    public AdminController(IFestivalService festivalService,
                           IUnidadVentaService unidadVentaService) {
        this.festivalService = festivalService;
        this.unidadVentaService = unidadVentaService;
    }

    @GetMapping("")
    public RedirectView index() {
        return new RedirectView(ViewRouteHelper.REDIRECT_ADMIN_FESTIVALES);
    }

    @GetMapping("/festivales")
    public String showAllFestival(Model model) {
        return ViewRouteHelper.ADMIN_FESTIVALES;
    }

    @GetMapping("/festivales/nuevo")
    public String showFormFestival(Model model) {
        return ViewRouteHelper.ADMIN_REGISTRO_FESTIVAL;
    }

    @PostMapping("/festivales/guardar")
    public RedirectView saveFestival(@ModelAttribute("unidad") Festival festival) {
        return new RedirectView(ViewRouteHelper.REDIRECT_ADMIN_FESTIVALES);
    }

    @GetMapping("/festivales/{id}/unidades")
    public String showAllUnidadDeVenta(@PathVariable("id") long idFestival, Model model) {
        return ViewRouteHelper.ADMIN_UNIDADES;
    }

    @GetMapping("/festivales/{id}/unidades/nuevo")
    public String showFormUnidadDeVenta(@PathVariable("id") long idFestival, Model model) {
        return ViewRouteHelper.ADMIN_REGISTRO_RESPONSABLE;
    }

    @PostMapping("/festivales/{id}/unidades/guardar")
    public RedirectView saveUnidadDeVenta(@PathVariable("id") long idFestival, @ModelAttribute("unidad") UnidadVenta unidad) {
        return new RedirectView(ViewRouteHelper.REDIRECT_ADMIN_FESTIVALES);
    }
}
