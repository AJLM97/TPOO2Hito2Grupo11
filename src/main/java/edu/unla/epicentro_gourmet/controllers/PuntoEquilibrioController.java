package edu.unla.epicentro_gourmet.controllers;

import edu.unla.epicentro_gourmet.entities.UnidadVenta;
import edu.unla.epicentro_gourmet.helpers.ViewRouteHelper;
import edu.unla.epicentro_gourmet.services.IPuntoEquilibrioService;
import edu.unla.epicentro_gourmet.services.IUnidadVentaService;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.view.RedirectView;

import java.time.LocalDate;
import java.util.Optional;

@Controller
@PreAuthorize("hasAuthority('RESPONSABLE')")
@RequestMapping("/responsable/reportes/punto-equilibrio")
public class PuntoEquilibrioController {

    private final IPuntoEquilibrioService puntoEquilibrioService;
    private final IUnidadVentaService unidadVentaService;

    public PuntoEquilibrioController(IPuntoEquilibrioService puntoEquilibrioService, IUnidadVentaService unidadVentaService) {
        this.puntoEquilibrioService = puntoEquilibrioService;
        this.unidadVentaService = unidadVentaService;
    }

    @GetMapping("")
    public ModelAndView showPuntoEquilibrio(Authentication authentication) {
        Optional<UnidadVenta> unidadVentaOptional = unidadVentaService.getUnidadVentaFromResponsableEmail(authentication.getName());
        if (unidadVentaOptional.isEmpty()) {
            return new ModelAndView(new RedirectView("/responsable/empleados"));
        }
        ModelAndView mAV = new ModelAndView(ViewRouteHelper.RESPONSABLE_PUNTO_EQUILIBRIO);
        mAV.addObject("equilibrio", puntoEquilibrioService.calcular(unidadVentaOptional.get(), LocalDate.now()).orElse(null));
        return mAV;
    }
}
