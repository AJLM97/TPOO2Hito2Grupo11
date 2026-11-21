package edu.unla.epicentro_gourmet.controllers.api.v1;

import edu.unla.epicentro_gourmet.services.IUnidadVentaService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/unidad-venta")
public class UnidadVentaRestController {

    private final IUnidadVentaService unidadVentaService;

    @Autowired
    public UnidadVentaRestController(IUnidadVentaService unidadVentaService) {
        this.unidadVentaService = unidadVentaService;
    }
}
