package edu.unla.epicentro_gourmet.controllers.api.v1;

import edu.unla.epicentro_gourmet.services.IEmpleadoService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/empleado")
public class EmpleadoRestController {

    private final IEmpleadoService empleadoService;

    @Autowired
    public EmpleadoRestController(IEmpleadoService empleadoService) {
        this.empleadoService = empleadoService;
    }

}
