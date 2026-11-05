package edu.unla.epicentro_gourmet.controllers;

import edu.unla.epicentro_gourmet.entities.UnidadVenta;
import edu.unla.epicentro_gourmet.helpers.ViewRouteHelper;
import edu.unla.epicentro_gourmet.services.IPosicionRankingService;
import edu.unla.epicentro_gourmet.services.IUnidadVentaService;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.view.RedirectView;

import java.util.Optional;

@Controller
@PreAuthorize("hasAuthority('RESPONSABLE')")
@RequestMapping("/responsable/reportes/posicion-ranking")
public class PosicionRankingController {

    private final IPosicionRankingService posicionRankingService;
    private final IUnidadVentaService unidadVentaService;

    public PosicionRankingController(IPosicionRankingService posicionRankingService,
                                     IUnidadVentaService unidadVentaService) {
        this.posicionRankingService = posicionRankingService;
        this.unidadVentaService = unidadVentaService;
    }

    @GetMapping("")
    public ModelAndView showPosicionRanking(Authentication authentication) {
        Optional<UnidadVenta> unidadVentaOptional =
                unidadVentaService.getUnidadVentaFromResponsableEmail(authentication.getName());
        if (unidadVentaOptional.isEmpty()) {
            return new ModelAndView(new RedirectView("/responsable/empleados"));
        }
        ModelAndView mAV = new ModelAndView(ViewRouteHelper.RESPONSABLE_POSICION_RANKING);
        mAV.addObject("posicion", posicionRankingService.posicionDeUnidad(unidadVentaOptional.get())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND,
                        "Tu unidad no figura en el ranking de su festival")));
        return mAV;
    }
}
