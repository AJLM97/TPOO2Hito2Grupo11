package edu.unla.epicentro_gourmet.controllers;

import edu.unla.epicentro_gourmet.dtos.LiquidacionHaberesDTO;
import edu.unla.epicentro_gourmet.entities.UnidadVenta;
import edu.unla.epicentro_gourmet.helpers.ViewRouteHelper;
import edu.unla.epicentro_gourmet.services.ILiquidacionService;
import edu.unla.epicentro_gourmet.services.IUnidadVentaService;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.view.RedirectView;

import java.util.List;
import java.util.Optional;

@Controller
@PreAuthorize("hasAuthority('RESPONSABLE')")
@RequestMapping("/responsable/liquidacion")
public class LiquidacionController {

    private final ILiquidacionService liquidacionService;
    private final IUnidadVentaService unidadVentaService;

    public LiquidacionController(ILiquidacionService liquidacionService, IUnidadVentaService unidadVentaService) {
        this.liquidacionService = liquidacionService;
        this.unidadVentaService = unidadVentaService;
    }

    @GetMapping("")
    public ModelAndView showLiquidacion(Authentication authentication) {
        Optional<UnidadVenta> unidadVentaOptional = unidadVentaService.getUnidadVentaFromResponsableEmail(authentication.getName());
        if (unidadVentaOptional.isEmpty()) {
            return new ModelAndView(new RedirectView("/responsable/empleados"));
        }
        UnidadVenta unidadVenta = unidadVentaOptional.get();
        Optional<List<LiquidacionHaberesDTO>> liquidaciones = liquidacionService.liquidarUnidad(unidadVenta);

        ModelAndView mAV = new ModelAndView(ViewRouteHelper.RESPONSABLE_LIQUIDACION);
        mAV.addObject("festival", unidadVenta.getFestival());
        mAV.addObject("liquidaciones", liquidaciones.orElse(null));
        mAV.addObject("totalPersonal", liquidaciones.orElse(List.of()).stream()
                .mapToDouble(LiquidacionHaberesDTO::getTotal).sum());
        return mAV;
    }
}
