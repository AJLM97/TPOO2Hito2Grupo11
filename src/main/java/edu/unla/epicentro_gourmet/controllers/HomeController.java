package edu.unla.epicentro_gourmet.controllers;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("/")
public class HomeController {

    @GetMapping({"/", "/index"})
    public String index() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();

        if (auth != null && auth.isAuthenticated()) {
            boolean esEmpleado = auth.getAuthorities().stream().anyMatch(a -> a.getAuthority().equals("EMPLEADO"));
            boolean esResponsable = auth.getAuthorities().stream().anyMatch(a -> a.getAuthority().equals("RESPONSABLE"));
            boolean esAdmin = auth.getAuthorities().stream().anyMatch(a -> a.getAuthority().equals("ADMINISTRADOR"));

            if (esEmpleado) {
                return "redirect:/empleado/pedidos";
            } else if (esResponsable) {
                return "redirect:/responsable/empleados";
            } else if (esAdmin) {
                return "redirect:/admin/festivales";
            }
        }

        return "redirect:/login";
    }
}
