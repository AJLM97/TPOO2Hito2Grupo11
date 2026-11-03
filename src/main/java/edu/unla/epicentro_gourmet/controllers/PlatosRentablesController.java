package edu.unla.epicentro_gourmet.controllers;

import edu.unla.epicentro_gourmet.entities.UnidadVenta;
import edu.unla.epicentro_gourmet.helpers.ViewRouteHelper;
import edu.unla.epicentro_gourmet.services.IPlatoRentabilidadService;
import edu.unla.epicentro_gourmet.services.IUnidadVentaService;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.view.RedirectView;

import java.util.Optional;

@Controller
@PreAuthorize("hasAuthority('RESPONSABLE')")
@RequestMapping("/responsable/reportes/platos-rentables")
public class PlatosRentablesController {
    private static final int MINIMO_PLATOS = 3;

    private final IPlatoRentabilidadService platoRentabilidadService;
    private final IUnidadVentaService unidadVentaService;

    public PlatosRentablesController(IPlatoRentabilidadService platoRentabilidadService,
                                     IUnidadVentaService unidadVentaService) {
        this.platoRentabilidadService = platoRentabilidadService;
        this.unidadVentaService = unidadVentaService;
    }

    @GetMapping("")
    public ModelAndView mostrarPlatosRentables(
            @RequestParam(defaultValue = "3") int cantidad,
            Authentication authentication) {
        Optional<UnidadVenta> unidadVentaOptional =
                unidadVentaService.getUnidadVentaFromResponsableEmail(authentication.getName());
        if (unidadVentaOptional.isEmpty()) {
            return new ModelAndView(new RedirectView("/responsable/empleados"));
        }

        ModelAndView mAV = new ModelAndView(ViewRouteHelper.RESPONSABLE_PLATOS_RENTABLES);
        mAV.addObject("unidad", unidadVentaOptional.get());
        mAV.addObject("cantidad", cantidad);
        if (cantidad < MINIMO_PLATOS) {
            mAV.addObject("error", "La cantidad de platos debe ser al menos 3.");
        } else {
            mAV.addObject("platos", platoRentabilidadService.platosPorRentabilidad(
                    unidadVentaOptional.get(), cantidad));
        }
        return mAV;
    }
}
