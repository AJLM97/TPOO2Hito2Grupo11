package edu.unla.epicentro_gourmet.controllers;

import edu.unla.epicentro_gourmet.dtos.EmpleadoDTO;
import edu.unla.epicentro_gourmet.entity.Empleado;
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
    public String buscarEmpleados(@ModelAttribute("filtro") EmpleadoFiltroDTO filtro, Model model) {
        List<Empleado> empleados = empleadoService.buscarEmpleadosConFiltro(filtro);
        model.addAttribute("empleados", empleados);
        model.addAttribute("filtro", filtro);
        return "empleados/listaEmpleados";
    }
}
