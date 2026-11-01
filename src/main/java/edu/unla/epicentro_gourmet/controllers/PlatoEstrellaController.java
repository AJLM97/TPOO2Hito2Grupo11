package edu.unla.epicentro_gourmet.controllers;

import edu.unla.epicentro_gourmet.entities.UnidadVenta;
import edu.unla.epicentro_gourmet.helpers.ViewRouteHelper;
import edu.unla.epicentro_gourmet.services.IPlatoEstrellaService;
import edu.unla.epicentro_gourmet.services.IUnidadVentaService;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.view.RedirectView;

import java.util.Optional;

@Controller
@PreAuthorize("hasAuthority('RESPONSABLE')")
@RequestMapping("/responsable/reportes/plato-estrella")
public class PlatoEstrellaController {

    private final IPlatoEstrellaService platoEstrellaService;
    private final IUnidadVentaService unidadVentaService;

    public PlatoEstrellaController(IPlatoEstrellaService platoEstrellaService, IUnidadVentaService unidadVentaService) {
        this.platoEstrellaService = platoEstrellaService;
        this.unidadVentaService = unidadVentaService;
    }

    @GetMapping("")
    public ModelAndView showPlatoEstrella(Authentication authentication) {
        Optional<UnidadVenta> unidadVentaOptional = unidadVentaService.getUnidadVentaFromResponsableEmail(authentication.getName());
        if (unidadVentaOptional.isEmpty()) {
            return new ModelAndView(new RedirectView("/responsable/empleados"));
        }
        UnidadVenta unidadVenta = unidadVentaOptional.get();
        ModelAndView mAV = new ModelAndView(ViewRouteHelper.RESPONSABLE_REPORTE_PLATO_ESTRELLA);
        mAV.addObject("unidad", unidadVenta);
        mAV.addObject("estrella", platoEstrellaService.platoEstrella(unidadVenta).orElse(null));
        return mAV;
    }
}
