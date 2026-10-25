package edu.unla.epicentro_gourmet.controller;

import edu.unla.epicentro_gourmet.dto.EmpleadoDto;
import edu.unla.epicentro_gourmet.entities.Empleado;
import edu.unla.epicentro_gourmet.service.EmpleadoService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestMapping;

import java.util.List;

@Controller
@RequestMapping("/empleados")
public class EmpleadoController {

    @Autowired
    private EmpleadoService empleadoService;

    @GetMapping("/filtrar")
    public String buscarEmpleados(@ModelAttribute("filtro") EmpleadoDto filtro, Model model) {
        List<Empleado> empleados = empleadoService.buscarEmpleadosConFiltro(filtro);
        model.addAttribute("empleados", empleados);
        model.addAttribute("filtro", filtro);
        return "empleados/lista-empleados";
    }
}
