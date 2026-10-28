package edu.unla.epicentro_gourmet.controllers;

import edu.unla.epicentro_gourmet.dtos.SolicitudFestival;
import edu.unla.epicentro_gourmet.entities.UnidadVenta;
import edu.unla.epicentro_gourmet.helpers.ViewRouteHelper;
import edu.unla.epicentro_gourmet.services.IFestivalService;
import edu.unla.epicentro_gourmet.services.IUnidadVentaService;
import jakarta.validation.Valid;
import org.modelmapper.ModelMapper;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import org.springframework.web.servlet.view.RedirectView;

@Controller
@PreAuthorize("hasAuthority('ADMINISTRADOR')")
@RequestMapping("/admin")
public class AdminController {

    private final IFestivalService festivalService;
    private final IUnidadVentaService unidadVentaService;
    private final ModelMapper modelMapper;

    public AdminController(IFestivalService festivalService,
                           IUnidadVentaService unidadVentaService,
                           ModelMapper modelMapper) {
        this.festivalService = festivalService;
        this.unidadVentaService = unidadVentaService;
        this.modelMapper = modelMapper;
    }

    @GetMapping("")
    public RedirectView index() {
        return new RedirectView(ViewRouteHelper.REDIRECT_ADMIN_FESTIVALES);
    }

    @GetMapping("/festivales")
    public ModelAndView showAllFestival() {
        ModelAndView mAV = new ModelAndView(ViewRouteHelper.ADMIN_FESTIVALES);
        mAV.addObject("festivales", festivalService.listar());
        return mAV;
    }

    @GetMapping("/festivales/nuevo")
    public ModelAndView showFormFestival() {
        ModelAndView mAV = new ModelAndView(ViewRouteHelper.ADMIN_REGISTRO_FESTIVAL);
        mAV.addObject("festival", new SolicitudFestival());
        return mAV;
    }

    @PostMapping("/festivales/guardar")
    public String saveFestival(@Valid @ModelAttribute("festival") SolicitudFestival festival, BindingResult result) {
        if (result.hasErrors()) {
            return ViewRouteHelper.ADMIN_REGISTRO_FESTIVAL;
        }
        festivalService.crear(festival);
        return "redirect:" + ViewRouteHelper.REDIRECT_ADMIN_FESTIVALES;
    }

    @GetMapping("/festivales/editar/{id}")
    public ModelAndView showFormEditFestival(@PathVariable("id") long id) {
        SolicitudFestival festival = modelMapper.map(festivalService.buscar(id), SolicitudFestival.class);
        ModelAndView mAV = new ModelAndView(ViewRouteHelper.ADMIN_EDITAR_FESTIVAL);
        mAV.addObject("festival", festival);
        return mAV;
    }

    @PostMapping("/festivales/actualizar")
    public String updateFestival(@Valid @ModelAttribute("festival") SolicitudFestival festival, BindingResult result) {
        if (result.hasErrors()) {
            return ViewRouteHelper.ADMIN_EDITAR_FESTIVAL;
        }
        festivalService.actualizar(festival.getId(), festival);
        return "redirect:" + ViewRouteHelper.REDIRECT_ADMIN_FESTIVALES;
    }

    @PostMapping("/festivales/eliminar/{id}")
    public String deleteFestival(@PathVariable("id") long id, RedirectAttributes redirectAttributes) {
        try {
            festivalService.eliminar(id);
        } catch (ResponseStatusException e) {
            redirectAttributes.addFlashAttribute("error", e.getReason());
        }
        return "redirect:" + ViewRouteHelper.REDIRECT_ADMIN_FESTIVALES;
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
