package edu.unla.epicentro_gourmet.controller;

import edu.unla.epicentro_gourmet.entities.Cajero;
import edu.unla.epicentro_gourmet.entities.Cocinero;
import edu.unla.epicentro_gourmet.entities.Empleado;
import edu.unla.epicentro_gourmet.services.EmpleadoService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List;

@Controller
@RequestMapping("/empleados")
public class EmpleadoController {

    @Autowired
    @Qualifier("empleadoService")
    private EmpleadoService empleadoService;

    @GetMapping("/lista")
    public String listarEmpleados(@RequestParam(name = "idUnidad", defaultValue = "1") long idUnidad, Model model) {
        List<Empleado> activos = empleadoService.listarEmpleadosActivosDeUnidad(idUnidad);
        model.addAttribute("empleados", activos);
        model.addAttribute("idUnidadActual", idUnidad);
        return "empleados/lista";
    }

    @GetMapping("/nuevo")
    public String mostrarFormularioAlta(@RequestParam(name = "idUnidad") long idUnidad, Model model) {
        model.addAttribute("nuevoCajero", new Cajero());
        model.addAttribute("nuevoCocinero", new Cocinero());
        model.addAttribute("idUnidadActual", idUnidad);
        return "empleados/formulario-alta";
    }

    @PostMapping("/alta/cajero")
    public String registrarCajero(@ModelAttribute("nuevoCajero") Cajero cajero,
                                  @RequestParam("emailPersonal") String emailPersonal,
                                  @RequestParam("idUnidad") long idUnidad,
                                  RedirectAttributes redirectAttributes) {
        try {
            empleadoService.registrarEmpleado(cajero, emailPersonal);
            redirectAttributes.addFlashAttribute("mensajeExito", "¡Cajero registrado con éxito! Credenciales enviadas por correo.");
        } catch (IllegalArgumentException e) {
            redirectAttributes.addFlashAttribute("mensajeError", e.getMessage());
            return "redirect:/empleados/nuevo";
        }
        return "redirect:/empleados/lista?idUnidad=" + idUnidad;
    }

    @PostMapping("/alta/cocinero")
    public String registrarCocinero(@ModelAttribute("nuevoCocinero") Cocinero cocinero,
                                    @RequestParam("emailPersonal") String emailPersonal,
                                    @RequestParam("idUnidad") long idUnidad,
                                    RedirectAttributes redirectAttributes) {
        try {
            empleadoService.registrarEmpleado(cocinero, emailPersonal);
            redirectAttributes.addFlashAttribute("mensajeExito", "¡Cocinero registrado con éxito! Credenciales enviadas por correo.");
        } catch (IllegalArgumentException e) {
            redirectAttributes.addFlashAttribute("mensajeError", e.getMessage());
            return "redirect:/empleados/nuevo";
        }
        return "redirect:/empleados/lista?idUnidad=" + idUnidad;
    }

    @PostMapping("/eliminar/{id}")
    public String eliminarEmpleado(@PathVariable("id") long idEmpleado,
                                   @RequestParam("idUnidad") long idUnidad,
                                   RedirectAttributes redirectAttributes) {
        try {
            empleadoService.eliminarEmpleadoLogico(idEmpleado);
            redirectAttributes.addFlashAttribute("mensajeExito", "Empleado eliminado de forma lógica correctamente.");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("mensajeError", "Error al intentar eliminar el empleado.");
        }
        return "redirect:/empleados/lista?idUnidad=" + idUnidad;
    }
}