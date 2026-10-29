package edu.unla.epicentro_gourmet.controllers;

import edu.unla.epicentro_gourmet.dtos.EmpleadoDTO;
import edu.unla.epicentro_gourmet.dtos.SolicitudFestival;
import edu.unla.epicentro_gourmet.dtos.UnidadVentaDTO;
import edu.unla.epicentro_gourmet.entities.Cajero;
import edu.unla.epicentro_gourmet.entities.Cocinero;
import edu.unla.epicentro_gourmet.entities.Empleado;
import edu.unla.epicentro_gourmet.entities.EnumCategoriaCocinero;
import edu.unla.epicentro_gourmet.entities.EnumTurnoCajero;
import edu.unla.epicentro_gourmet.entities.PuestoDesarmable;
import edu.unla.epicentro_gourmet.entities.UnidadVenta;
import edu.unla.epicentro_gourmet.entities.Usuario;
import edu.unla.epicentro_gourmet.entities.Festival;
import edu.unla.epicentro_gourmet.entities.FoodTruck;
import edu.unla.epicentro_gourmet.helpers.ViewRouteHelper;
import edu.unla.epicentro_gourmet.services.EnumResultadoAltaEmpleado;
import edu.unla.epicentro_gourmet.services.IEmpleadoService;
import edu.unla.epicentro_gourmet.services.IFestivalService;
import edu.unla.epicentro_gourmet.services.IReporteVentaService;
import edu.unla.epicentro_gourmet.services.IReporteMayoresCanonService;
import edu.unla.epicentro_gourmet.services.IUnidadVentaService;
import jakarta.validation.Valid;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.modelmapper.ModelMapper;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import org.springframework.web.servlet.view.RedirectView;

@Controller
@PreAuthorize("hasAuthority('ADMINISTRADOR')")
@RequestMapping("/admin")
public class AdminController {

    private final IFestivalService festivalService;
    private final IUnidadVentaService unidadVentaService;
    private final IEmpleadoService empleadoService;
    private final IReporteVentaService reporteVentaService;
    private final IReporteMayoresCanonService reporteMayoresCanonService;
    private final ModelMapper modelMapper;

    public AdminController(IFestivalService festivalService,
                           IUnidadVentaService unidadVentaService,
                           IEmpleadoService empleadoService,
                           IReporteVentaService reporteVentaService,
                           IReporteMayoresCanonService reporteMayoresCanonService,
                           ModelMapper modelMapper) {
        this.festivalService = festivalService;
        this.unidadVentaService = unidadVentaService;
        this.empleadoService = empleadoService;
        this.reporteVentaService = reporteVentaService;
        this.reporteMayoresCanonService = reporteMayoresCanonService;
        this.modelMapper = modelMapper;
    }

    @GetMapping("")
    public RedirectView index() {
        return new RedirectView(ViewRouteHelper.REDIRECT_ADMIN_FESTIVALES);
    }

    @GetMapping("/festivales")
    public ModelAndView showAllFestival() {
        ModelAndView mAV = new ModelAndView(ViewRouteHelper.ADMIN_FESTIVALES);
        mAV.addObject("festivales", festivalService.listar());
        return mAV;
    }

    @GetMapping("/ranking-recaudacion")
    public ModelAndView showRankingRecaudacion(@RequestParam(required = false) Long festivalId) {
        ModelAndView mAV = new ModelAndView(ViewRouteHelper.ADMIN_RANKING_RECAUDACION);
        mAV.addObject("festivales", festivalService.getAll());
        if (festivalId != null) {
            Festival festival = festivalService.findById(festivalId);
            if (festival == null) {
                return new ModelAndView("redirect:/admin/festivales");
            }
            mAV.addObject("festivalSeleccionado", festival);
            mAV.addObject("ranking", reporteVentaService.rankingUnidadesPorRecaudacion(festivalId));
        }
        return mAV;
    }

    @GetMapping("/mayores-canon")
    public ModelAndView showMayoresCanon(@RequestParam(required = false) Long festivalId,
                                         @RequestParam(defaultValue = "3") int x) {
        ModelAndView mAV = new ModelAndView(ViewRouteHelper.ADMIN_MAYORES_CANON);
        mAV.addObject("festivales", festivalService.getAll());
        mAV.addObject("cantidad", x);
        if (festivalId != null) {
            Festival festival = festivalService.findById(festivalId);
            if (festival == null) {
                return new ModelAndView("redirect:/admin/festivales");
            }
            mAV.addObject("festivalSeleccionado", festival);
            if (x < 1) {
                mAV.addObject("error", "La cantidad de unidades debe ser mayor que cero.");
            } else {
                mAV.addObject("unidades", reporteMayoresCanonService.mayoresCanon(festivalId, x));
            }
        }
        return mAV;
    }

    @GetMapping("/festivales/nuevo")
    public ModelAndView showFormFestival() {
        ModelAndView mAV = new ModelAndView(ViewRouteHelper.ADMIN_REGISTRO_FESTIVAL);
        mAV.addObject("festival", new SolicitudFestival());
        return mAV;
    }

    @PostMapping("/festivales/guardar")
    public String saveFestival(@Valid @ModelAttribute("festival") SolicitudFestival festival, BindingResult result) {
        if (result.hasErrors()) {
            return ViewRouteHelper.ADMIN_REGISTRO_FESTIVAL;
        }
        festivalService.crear(festival);
        return "redirect:" + ViewRouteHelper.REDIRECT_ADMIN_FESTIVALES;
    }

    @GetMapping("/festivales/editar/{id}")
    public ModelAndView showFormEditFestival(@PathVariable("id") long id) {
        SolicitudFestival festival = modelMapper.map(festivalService.buscar(id), SolicitudFestival.class);
        ModelAndView mAV = new ModelAndView(ViewRouteHelper.ADMIN_EDITAR_FESTIVAL);
        mAV.addObject("festival", festival);
        return mAV;
    }

    @PostMapping("/festivales/actualizar")
    public String updateFestival(@Valid @ModelAttribute("festival") SolicitudFestival festival, BindingResult result) {
        if (result.hasErrors()) {
            return ViewRouteHelper.ADMIN_EDITAR_FESTIVAL;
        }
        festivalService.actualizar(festival.getId(), festival);
        return "redirect:" + ViewRouteHelper.REDIRECT_ADMIN_FESTIVALES;
    }

    @PostMapping("/festivales/eliminar/{id}")
    public String deleteFestival(@PathVariable("id") long id, RedirectAttributes redirectAttributes) {
        try {
            festivalService.eliminar(id);
        } catch (ResponseStatusException e) {
            redirectAttributes.addFlashAttribute("error", e.getReason());
        }
        return "redirect:" + ViewRouteHelper.REDIRECT_ADMIN_FESTIVALES;
    }

    @GetMapping("/festivales/{id}/unidades")
    public ModelAndView showAllUnidadDeVenta(@PathVariable ("id") long idFestival) {
        ModelAndView mAV = new ModelAndView(ViewRouteHelper.ADMIN_UNIDADES);
        mAV.addObject("unidades", unidadVentaService.getUnidadesDeVentaFromFestival(idFestival));
        mAV.addObject("idFestival", idFestival);
        return mAV;
    }

    @GetMapping("/festivales/{id}/unidades/nuevo")
    public ModelAndView showFormUnidadDeVenta(@PathVariable("id") long idFestival) {
        ModelAndView mAV = new ModelAndView(ViewRouteHelper.ADMIN_UNIDADES_FORM);
        UnidadVentaDTO unidadDTO = new UnidadVentaDTO();

        mAV.addObject("unidad", unidadDTO);
        mAV.addObject("idFestival", idFestival);
        return mAV;
    }

    @PostMapping("/festivales/{id}/unidades/guardar")
    public String saveUnidadDeVenta(@PathVariable("id") long idFestival, @Valid @ModelAttribute("unidad") UnidadVentaDTO unidadDTO, BindingResult result) {
        if (result.hasErrors()) {
            return ViewRouteHelper.ADMIN_UNIDADES_FORM;
        }

        String emailResponsable = unidadDTO.getEmailResponsable();
        String passwordResponsable = UUID.randomUUID().toString().substring(0, 8);
        Optional<UnidadVenta> unidadVentaOptional = unidadVentaService.getUnidadVentaFromResponsableEmail(emailResponsable);

        if (unidadVentaOptional.isPresent()) {
            result.rejectValue("emailResponsable", "duplicado", "Ese responsable ya tiene una unidad de venta asignada");
            return ViewRouteHelper.ADMIN_UNIDADES_FORM;
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

            unidadVentaService.altaConResponsable(puesto, emailResponsable, passwordResponsable);

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

            unidadVentaService.altaConResponsable(foodTruck, emailResponsable, passwordResponsable);
        }

        return "redirect:/admin/festivales/" + idFestival + "/unidades";
    }

    @GetMapping("/festivales/{idFestival}/unidades/editar/{idUnidadVenta}")
    public ModelAndView getUnidadDeVenta(@PathVariable("idFestival") long idFestival, @PathVariable("idUnidadVenta") long idUnidadVenta) {
        Optional<UnidadVenta> unidadVentaOptional = unidadVentaService.findById(idUnidadVenta);
        if (unidadVentaOptional.isEmpty()) {
            return new ModelAndView(new RedirectView("/admin/festivales/" + idFestival + "/unidades"));
        }

        UnidadVenta unidadVenta = unidadVentaOptional.get();
        UnidadVentaDTO unidadVentaDTO = modelMapper.map(unidadVenta, UnidadVentaDTO.class);
        
        if (unidadVenta instanceof PuestoDesarmable puestoExistente) {
            unidadVentaDTO.setTipoUnidadVenta("PUESTO_DESARMABLE");
            unidadVentaDTO.setCantidadDeCarpas(puestoExistente.getCantidadDeCarpas());
            unidadVentaDTO.setTiempoMontajeMinutos(puestoExistente.getTiempoMontajeMinutos());
        } else if (unidadVenta instanceof FoodTruck foodTruckExistente) {
            unidadVentaDTO.setTipoUnidadVenta("FOOD_TRUCK");
            unidadVentaDTO.setConexionElectrica(foodTruckExistente.isConexionElectrica());
            unidadVentaDTO.setPatente(foodTruckExistente.getPatente());
        }

        ModelAndView mAV = new ModelAndView("admin/unidades-edit");
        mAV.addObject("unidad", unidadVentaDTO);
        mAV.addObject("idFestival", idFestival);
        return mAV;
    }

    @PostMapping("/festivales/{idFestival}/unidades/actualizar")
    public String updateUnidadDeVenta(@Valid @ModelAttribute("unidad") UnidadVentaDTO unidadVentaDTO, BindingResult result, @PathVariable("idFestival") long idFestival) {
        if (result.hasErrors()) {
            return "admin/unidades-edit";
        }

        unidadVentaService.findByIdWithResponsable(unidadVentaDTO.getId()).ifPresent(unidadVentaEncontrada -> {
            unidadVentaEncontrada.setNombreComercial(unidadVentaDTO.getNombreComercial());
            unidadVentaEncontrada.setSuperficie(unidadVentaDTO.getSuperficie());
            if (unidadVentaEncontrada instanceof PuestoDesarmable puestoExistente) {
                puestoExistente.setCantidadDeCarpas(unidadVentaDTO.getCantidadDeCarpas());
                puestoExistente.setTiempoMontajeMinutos(unidadVentaDTO.getTiempoMontajeMinutos());
            } else if (unidadVentaEncontrada instanceof FoodTruck foodTruckExistente) {
                foodTruckExistente.setConexionElectrica(unidadVentaDTO.isConexionElectrica());
                foodTruckExistente.setPatente(unidadVentaDTO.getPatente());
            }

            if(unidadVentaEncontrada.getResponsable().getEmail().equals(unidadVentaDTO.getEmailResponsable())) {
                unidadVentaService.save(unidadVentaEncontrada);
            } else {
                unidadVentaService.actualizarConResponsable(unidadVentaEncontrada, unidadVentaDTO.getEmailResponsable());
            }
        });
        return "redirect:/admin/festivales/" + idFestival + "/unidades";
    }

    @PostMapping("/festivales/{idFestival}/unidades/baja/{idUnidadVenta}")
    public String deleteUnidadDeVenta(@PathVariable("idUnidadVenta") int idUnidadVenta, @PathVariable("idFestival") long idFestival) {
        
        Optional<UnidadVenta> unidadVentaOptional = unidadVentaService.findByIdWithResponsable(idUnidadVenta);
        if (unidadVentaOptional.isPresent()) {
            UnidadVenta unidadVenta = unidadVentaOptional.get();
            List<Empleado> empleados = empleadoService.findByUnidadVenta(unidadVenta);
            for (Empleado empleado : empleados) {
                empleadoService.logicalDelete(unidadVenta, empleado.getId());
            }
            unidadVentaService.logicalDelete(unidadVenta);
        }
        return "redirect:/admin/festivales/" + idFestival + "/unidades";
    }
}
