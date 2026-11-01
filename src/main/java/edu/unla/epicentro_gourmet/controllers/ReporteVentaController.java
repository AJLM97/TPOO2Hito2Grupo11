package edu.unla.epicentro_gourmet.controllers;

import edu.unla.epicentro_gourmet.entities.UnidadVenta;
import edu.unla.epicentro_gourmet.helpers.ViewRouteHelper;
import edu.unla.epicentro_gourmet.services.IReporteVentaService;
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
@RequestMapping("/responsable/reportes/recaudacion")
public class ReporteVentaController {

    private final IReporteVentaService reporteVentaService;
    private final IUnidadVentaService unidadVentaService;

    public ReporteVentaController(IReporteVentaService reporteVentaService, IUnidadVentaService unidadVentaService) {
        this.reporteVentaService = reporteVentaService;
        this.unidadVentaService = unidadVentaService;
    }

    @GetMapping("")
    public ModelAndView showReporteRecaudacion(Authentication authentication) {
        Optional<UnidadVenta> unidadVentaOptional = unidadVentaService.getUnidadVentaFromResponsableEmail(authentication.getName());
        if (unidadVentaOptional.isEmpty()) {
            return new ModelAndView(new RedirectView("/responsable/empleados"));
        }
        ModelAndView mAV = new ModelAndView(ViewRouteHelper.RESPONSABLE_REPORTE_RECAUDACION);
        mAV.addObject("reporte", reporteVentaService.reporteDeUnidad(unidadVentaOptional.get()));
        return mAV;
    }
}
