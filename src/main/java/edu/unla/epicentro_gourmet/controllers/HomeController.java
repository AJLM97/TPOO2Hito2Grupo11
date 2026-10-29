package edu.unla.epicentro_gourmet.controllers;

import java.time.LocalDate;
import java.util.Optional;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.validation.BindingResult;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.ModelAndView;

import edu.unla.epicentro_gourmet.dtos.UnidadVentaDTO;
import edu.unla.epicentro_gourmet.entities.Festival;
import edu.unla.epicentro_gourmet.entities.FoodTruck;
import edu.unla.epicentro_gourmet.entities.PuestoDesarmable;
import edu.unla.epicentro_gourmet.entities.UnidadVenta;
import edu.unla.epicentro_gourmet.services.IFestivalService;
import edu.unla.epicentro_gourmet.services.IUnidadVentaService;
import jakarta.validation.Valid;

@Controller
@RequestMapping("/")
public class HomeController {

    private final IFestivalService festivalService;
    private final IUnidadVentaService unidadVentaService;

    public HomeController(IFestivalService festivalService,
                           IUnidadVentaService unidadVentaService) {
        this.festivalService = festivalService;
        this.unidadVentaService = unidadVentaService;
    }

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

        return "redirect:/home";
    }

    @GetMapping("/home")
    public ModelAndView showAllFestival() {
        ModelAndView mAV = new ModelAndView("home/festivales");
        mAV.addObject("festivales", festivalService.listarFestivalesNoConcluidos());
        return mAV;
    }

    @GetMapping("/festival/{id}")
    public ModelAndView showAllUnidadDeVenta(@PathVariable ("id") long idFestival) {
        Festival festival = festivalService.findById(idFestival);
        if (festival == null || !festival.getFechaFin().isAfter(LocalDate.now())) {
            return new ModelAndView("redirect:/home");
        }

        ModelAndView mAV = new ModelAndView("home/unidades");
        mAV.addObject("unidades", unidadVentaService.getUnidadesDeVentaFromFestival(idFestival));
        mAV.addObject("idFestival", idFestival);
        mAV.addObject("nombreFestival", festival.getNombre());
        return mAV;
    }

    @GetMapping("/festival/{id}/registrar")
    public ModelAndView showFormUnidadDeVenta(@PathVariable("id") long idFestival) {
        Festival festival = festivalService.findById(idFestival);
        if (festival == null || !festival.getFechaFin().isAfter(LocalDate.now())) {
            return new ModelAndView("redirect:/home");
        }

        ModelAndView mAV = new ModelAndView("home/unidades-form");
        mAV.addObject("unidad", new UnidadVentaDTO());
        mAV.addObject("idFestival", idFestival);
        return mAV;
    }

    @PostMapping("/festival/{id}/guardar")
    public String saveUnidadDeVenta(@PathVariable("id") long idFestival,
                                    @Valid @ModelAttribute("unidad") UnidadVentaDTO unidadDTO,
                                    BindingResult result,
                                    Model model) {
        model.addAttribute("idFestival", idFestival);
        if (result.hasErrors()) {
            return "home/unidades-form";
        }

        String emailResponsable = unidadDTO.getEmailResponsable();
        Optional<UnidadVenta> unidadVentaOptional = unidadVentaService.getUnidadVentaFromResponsableEmail(emailResponsable);

        if (unidadVentaOptional.isPresent()) {
            result.rejectValue("emailResponsable", "duplicado", "Ese responsable ya tiene una unidad de venta asignada");
            return "home/unidades-form";
        }

        Festival festival = festivalService.findById(idFestival);

        if ("PUESTO_DESARMABLE".equalsIgnoreCase(unidadDTO.getTipoUnidadVenta())) {
            PuestoDesarmable puesto = new PuestoDesarmable(
                unidadDTO.getNombreComercial(),
                unidadDTO.getCodigo(),
                unidadDTO.getSuperficie(),
                festival,
                unidadDTO.getCantidadDeCarpas(),
                unidadDTO.getTiempoMontajeMinutos(),
                null
            );

            unidadVentaService.altaConResponsable(puesto, emailResponsable, unidadDTO.getPasswordResponsable());

        } else if ("FOOD_TRUCK".equalsIgnoreCase(unidadDTO.getTipoUnidadVenta())) {
            FoodTruck foodTruck = new FoodTruck(
                unidadDTO.getNombreComercial(),
                unidadDTO.getCodigo(),
                unidadDTO.getSuperficie(),
                festival,
                unidadDTO.getPatente(),
                unidadDTO.isConexionElectrica(),
                null
            );

            unidadVentaService.altaConResponsable(foodTruck, emailResponsable, unidadDTO.getPasswordResponsable());
        }

        return "redirect:/festival/" + idFestival;
    }
}
