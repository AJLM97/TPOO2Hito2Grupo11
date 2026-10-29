package edu.unla.epicentro_gourmet.controllers;

import edu.unla.epicentro_gourmet.dtos.PedidoAuditoriaDTO;
import edu.unla.epicentro_gourmet.entities.UnidadVenta;
import edu.unla.epicentro_gourmet.helpers.ViewRouteHelper;
import edu.unla.epicentro_gourmet.services.EnumFiltroEstadoPedido;
import edu.unla.epicentro_gourmet.services.IAuditoriaPedidosService;
import edu.unla.epicentro_gourmet.services.IPlatoService;
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
import java.util.List;
import java.util.Optional;

@Controller
@PreAuthorize("hasAuthority('RESPONSABLE')")
@RequestMapping("/responsable/pedidos")
public class AuditoriaPedidosController {

    private final IAuditoriaPedidosService auditoriaPedidosService;
    private final IPlatoService platoService;
    private final IUnidadVentaService unidadVentaService;

    public AuditoriaPedidosController(IAuditoriaPedidosService auditoriaPedidosService, IPlatoService platoService,
                                      IUnidadVentaService unidadVentaService) {
        this.auditoriaPedidosService = auditoriaPedidosService;
        this.platoService = platoService;
        this.unidadVentaService = unidadVentaService;
    }

    @GetMapping("")
    public ModelAndView showAuditoria(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate desde,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate hasta,
            @RequestParam(required = false) EnumFiltroEstadoPedido estado,
            @RequestParam(required = false) Long plato,
            Authentication authentication) {
        Optional<UnidadVenta> unidadVentaOptional = unidadVentaService.getUnidadVentaFromResponsableEmail(authentication.getName());
        if (unidadVentaOptional.isEmpty()) {
            return new ModelAndView(new RedirectView("/responsable/empleados"));
        }
        UnidadVenta unidadVenta = unidadVentaOptional.get();

        ModelAndView mAV = new ModelAndView(ViewRouteHelper.RESPONSABLE_AUDITORIA_PEDIDOS);
        mAV.addObject("platos", platoService.findByUnidadVenta(unidadVenta));
        mAV.addObject("desde", desde);
        mAV.addObject("hasta", hasta);
        mAV.addObject("estado", estado == null ? EnumFiltroEstadoPedido.TODOS : estado);
        mAV.addObject("platoSeleccionado", plato);

        List<PedidoAuditoriaDTO> pedidos = List.of();
        if (auditoriaPedidosService.rangoDeFechasValido(desde, hasta)) {
            pedidos = auditoriaPedidosService.buscar(unidadVenta, desde, hasta, estado, plato);
        } else {
            mAV.addObject("error", "La fecha desde no puede ser posterior a la fecha hasta.");
        }
        mAV.addObject("pedidos", pedidos);
        return mAV;
    }
}
