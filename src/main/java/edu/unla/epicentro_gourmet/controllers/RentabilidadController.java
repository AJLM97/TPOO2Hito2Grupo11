package edu.unla.epicentro_gourmet.controllers;

import edu.unla.epicentro_gourmet.entities.UnidadVenta;
import edu.unla.epicentro_gourmet.helpers.ViewRouteHelper;
import edu.unla.epicentro_gourmet.services.EnumResultadoRangoFechas;
import edu.unla.epicentro_gourmet.services.IRentabilidadService;
import edu.unla.epicentro_gourmet.services.IUnidadVentaService;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.view.RedirectView;

import java.time.LocalDate;
import java.util.Optional;

@Controller
@PreAuthorize("hasAuthority('RESPONSABLE')")
@RequestMapping("/responsable/reportes/rentabilidad")
public class RentabilidadController {

    private final IRentabilidadService rentabilidadService;
    private final IUnidadVentaService unidadVentaService;

    public RentabilidadController(IRentabilidadService rentabilidadService, IUnidadVentaService unidadVentaService) {
        this.rentabilidadService = rentabilidadService;
        this.unidadVentaService = unidadVentaService;
    }

    @GetMapping("")
    public ModelAndView showRentabilidad(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate desde,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate hasta,
            Authentication authentication) {
        Optional<UnidadVenta> unidadVentaOptional = unidadVentaService.getUnidadVentaFromResponsableEmail(authentication.getName());
        if (unidadVentaOptional.isEmpty()) {
            return new ModelAndView(new RedirectView("/responsable/empleados"));
        }
        UnidadVenta unidadVenta = unidadVentaOptional.get();

        ModelAndView mAV = new ModelAndView(ViewRouteHelper.RESPONSABLE_REPORTE_RENTABILIDAD);
        mAV.addObject("desde", desde);
        mAV.addObject("hasta", hasta);

        boolean sinFechas = desde == null && hasta == null;
        EnumResultadoRangoFechas rango = sinFechas
                ? EnumResultadoRangoFechas.OK : rentabilidadService.validarRango(unidadVenta, desde, hasta);
        if (rango != EnumResultadoRangoFechas.OK) {
            mAV.addObject("error", mensajeDe(rango));
            return mAV;
        }

        mAV.addObject("reporte", (sinFechas
                ? rentabilidadService.rentabilidad(unidadVenta)
                : rentabilidadService.rentabilidad(unidadVenta, desde, hasta)).orElse(null));
        mAV.addObject("calculado", true);
        return mAV;
    }

    private String mensajeDe(EnumResultadoRangoFechas rango) {
        return switch (rango) {
            case FALTA_FECHA -> "Indicá las dos fechas del rango.";
            case DESDE_POSTERIOR_A_HASTA -> "La fecha desde no puede ser posterior a la fecha hasta.";
            case FUERA_DEL_FESTIVAL -> "El rango debe estar dentro de las fechas del festival.";
            case OK -> "";
        };
    }
}
