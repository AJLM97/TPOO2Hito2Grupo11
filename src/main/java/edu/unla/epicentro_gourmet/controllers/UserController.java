package edu.unla.epicentro_gourmet.controllers;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

import edu.unla.epicentro_gourmet.helpers.ViewRouteHelper;


@Controller
public class UserController {

    @GetMapping("/login")
    public String login(Model model,
                        @RequestParam(required=false) String error,
                        @RequestParam(required=false) String logout) {
        model.addAttribute("error", error);
        model.addAttribute("logout", logout);
        return ViewRouteHelper.USER_LOGIN;
    }

    @GetMapping("/logout")
    public String logout(Model model) {
        return ViewRouteHelper.USER_LOGOUT;
    }

    @GetMapping("/loginsuccess")
    public String loginCheck(Authentication authentication) {
        if (authentication == null) {
            return "redirect:/login";
        }

        for (GrantedAuthority authority : authentication.getAuthorities()) {
            String rol = authority.getAuthority();

            switch (rol) {
                case "ADMINISTRADOR":
                    return "redirect:/admin/festivales";
                case "RESPONSABLE":
                    return "redirect:/responsable/empleados";
                case "EMPLEADO":
                    return "redirect:/empleado/pedidos";
            }
        }

        return "redirect:/login?error";
    }
}