package edu.unla.epicentro_gourmet.controllers;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import edu.unla.epicentro_gourmet.helpers.ViewRouteHelper;
import edu.unla.epicentro_gourmet.services.implementation.UsuarioService;

@Controller
public class UserController {

    private final UsuarioService usuarioService;
    private final PasswordEncoder passwordEncoder;

    public UserController(UsuarioService usuarioService, PasswordEncoder passwordEncoder){
        this.usuarioService = usuarioService;
        this.passwordEncoder = passwordEncoder;
    }

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
                    return "redirect:" + ViewRouteHelper.REDIRECT_EMPLEADO_INFORMACION;
            }
        }

        return "redirect:/login?error";
    }

    @GetMapping("/usuario/cambiar-password")
    public String mostrarCambioPassword(){
        return "user/cambiar-password";
    }

    @PostMapping("/usuario/cambiar-password")
    public String cambiarPassword(Authentication authentication,
                                  @RequestParam(required = false) String contraseniaActual,
                                  @RequestParam(required = false) String contraseniaNueva,
                                  @RequestParam(required = false) String repetirContraseniaNueva,
                                  Model model,
                                  RedirectAttributes redirectAttributes){
        if(contraseniaActual == null || contraseniaActual.isBlank()
        || contraseniaNueva == null || contraseniaNueva.isBlank()
        || repetirContraseniaNueva == null || repetirContraseniaNueva.isBlank()){
            model.addAttribute("error", "Completá todos los campos.");
            return "user/cambiar-password";
        }

        var usuario = usuarioService.findByEmail(authentication.getName());
        if(usuario == null){
            model.addAttribute("error", "No se encontró el usuario autenticado.");
            return "user/cambiar-password";
        }

        if(!passwordEncoder.matches(contraseniaActual, usuario.getPassword())){
            model.addAttribute("error", "La contraseña actual es incorrecta.");
            return "user/cambiar-password";
        }

        if(contraseniaActual.equals(contraseniaNueva)){
            model.addAttribute("error", "La contraseña nueva no puede ser igual a la actual.");
            return "user/cambiar-password";
        }

        if(!contraseniaNueva.equals(repetirContraseniaNueva)){
            model.addAttribute("error", "La contraseña nueva y su repetición no coinciden.");
            return "user/cambiar-password";
        }

        usuario.setPassword(passwordEncoder.encode(contraseniaNueva));
        usuarioService.save(usuario);

        redirectAttributes.addFlashAttribute("exito", "Contraseña cambiada con exito.");
        return "redirect:/usuario/cambiar-password";
    }
}