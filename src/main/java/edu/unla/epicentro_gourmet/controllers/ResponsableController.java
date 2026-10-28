package edu.unla.epicentro_gourmet.controllers;

import edu.unla.epicentro_gourmet.dtos.EmpleadoDTO;
import edu.unla.epicentro_gourmet.dtos.PlatoDTO;
import edu.unla.epicentro_gourmet.entities.*;
import edu.unla.epicentro_gourmet.helpers.ViewRouteHelper;
import edu.unla.epicentro_gourmet.services.EnumResultadoAltaEmpleado;
import edu.unla.epicentro_gourmet.services.EnumResultadoGuardadoPlato;
import edu.unla.epicentro_gourmet.services.IEmpleadoService;
import edu.unla.epicentro_gourmet.services.IPlatoService;
import edu.unla.epicentro_gourmet.services.IUnidadVentaService;
import jakarta.validation.Valid;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.view.RedirectView;
import org.modelmapper.ModelMapper;

import java.util.Optional;

@Controller
@PreAuthorize("hasAuthority('RESPONSABLE')")
@RequestMapping("/responsable")
public class ResponsableController {

    private final IEmpleadoService empleadoService;
    private final IPlatoService platoService;
    private final IUnidadVentaService unidadVentaService;
    private final ModelMapper modelMapper;

    public ResponsableController(IEmpleadoService empleadoService,
                                 IPlatoService platoService, IUnidadVentaService unidadVentaService, ModelMapper modelMapper) {
        this.empleadoService = empleadoService;
        this.platoService = platoService;
        this.unidadVentaService = unidadVentaService;
        this.modelMapper = modelMapper;
    }

    @GetMapping("")
    public RedirectView index() {
        return new RedirectView("/responsable/empleados");
    }

    @GetMapping("/empleados")
    public ModelAndView showAllEmpleado(Authentication authentication) {
        ModelAndView mAV = new ModelAndView(ViewRouteHelper.RESPONSABLE_EMPLEADOS);
        UnidadVenta unidadVenta = unidadVentaService.getUnidadVentaFromResponsableEmail(authentication.getName());
        mAV.addObject("empleados", empleadoService.findByUnidadVenta(unidadVenta));
        return mAV;
    }

    @GetMapping("/empleados/nuevo")
    public ModelAndView showFormEmpleado() {
        ModelAndView mAV = new ModelAndView(ViewRouteHelper.RESPONSABLE_EMPLEADO_FORM);
        mAV.addObject("empleado", new EmpleadoDTO());
        return mAV;
    }

    @PostMapping("/empleados/guardar")
    public String saveEmpleado(@Valid @ModelAttribute("empleado") EmpleadoDTO empleadoDTO, BindingResult result, Authentication authentication) {
        if (result.hasErrors()) {
            return ViewRouteHelper.RESPONSABLE_EMPLEADO_FORM;
        }

        UnidadVenta unidadVenta = unidadVentaService.getUnidadVentaFromResponsableEmail(authentication.getName());
        String emailPersonal = empleadoDTO.getEmailPersonal();

        EnumResultadoAltaEmpleado estado = null;

        if ("CAJERO".equalsIgnoreCase(empleadoDTO.getTipoEmpleado())) {
            Cajero cajero = modelMapper.map(empleadoDTO, Cajero.class);
            cajero.setTurnoTrabajo(EnumTurnoCajero.valueOf(empleadoDTO.getTurnoTrabajo()));
            cajero.setUnidad(unidadVenta);

            estado = empleadoService.altaOReactivar(cajero, emailPersonal);

        } else if ("COCINERO".equalsIgnoreCase(empleadoDTO.getTipoEmpleado())) {
            Cocinero cocinero = modelMapper.map(empleadoDTO, Cocinero.class);
            cocinero.setCategoria(EnumCategoriaCocinero.valueOf(empleadoDTO.getCategoriaCocinero()));
            cocinero.setUnidad(unidadVenta);

            estado = empleadoService.altaOReactivar(cocinero, emailPersonal);
        }

        if (estado == EnumResultadoAltaEmpleado.DNI_YA_ACTIVO)
            result.rejectValue("dni", "duplicado", "Ese DNI ya está registrado");
        else if (estado == EnumResultadoAltaEmpleado.TIPO_DISTINTO)
            result.rejectValue("tipoEmpleado", "tipo", "El empleado dado de baja era de otro tipo");
        else
            return "redirect:/responsable/empleados";

        return ViewRouteHelper.RESPONSABLE_EMPLEADO_FORM;
    }

    @GetMapping("/empleados/editar/{id}")
    public ModelAndView get(@PathVariable("id") int id, Authentication authentication) {
        UnidadVenta unidadVenta = unidadVentaService.getUnidadVentaFromResponsableEmail(authentication.getName());
        Optional<Empleado> empleadoOptional = empleadoService.findByIdAndUnidadVenta(id, unidadVenta);
        if (empleadoOptional.isEmpty()) {
            return new ModelAndView(new RedirectView("/responsable/empleados"));
        }
        
        Empleado empleado = empleadoOptional.get();
        EmpleadoDTO empleadoDTO = modelMapper.map(empleado, EmpleadoDTO.class);

        if (empleado instanceof Cajero cajeroExistente) {
            empleadoDTO.setTipoEmpleado("CAJERO");
            empleadoDTO.setTurnoTrabajo(cajeroExistente.getTurnoTrabajo().name());
        } else if (empleado instanceof Cocinero cocineroExistente) {
            empleadoDTO.setTipoEmpleado("COCINERO");
        empleadoDTO.setCategoriaCocinero(cocineroExistente.getCategoria().name());
        }

        ModelAndView mAV = new ModelAndView("responsable/empleados-edit");
        mAV.addObject("empleado", empleadoDTO);
        return mAV;
    }

    @PostMapping("/empleados/actualizar")
    public String update(@Valid @ModelAttribute("empleado") EmpleadoDTO empleadoDTO, BindingResult result) {
        if (result.hasErrors()) {
            return "Responsable/empleados-edit";
        }

        empleadoService.findById(empleadoDTO.getId()).ifPresent(empleadoEncontrado -> {
            empleadoEncontrado.setNombre(empleadoDTO.getNombre());
            empleadoEncontrado.setApellido(empleadoDTO.getApellido());
            empleadoEncontrado.setSueldoBase(empleadoDTO.getSueldoBase());
            if (empleadoEncontrado instanceof Cajero cajeroExistente) {
                cajeroExistente.setTurnoTrabajo(EnumTurnoCajero.valueOf(empleadoDTO.getTurnoTrabajo()));
            } else if (empleadoEncontrado instanceof Cocinero cocineroExistente) {
                cocineroExistente.setCategoria(EnumCategoriaCocinero.valueOf(empleadoDTO.getCategoriaCocinero()));
            }
            empleadoService.save(empleadoEncontrado);
        });
        return "redirect:/responsable/empleados";
    }

    @PostMapping("/empleados/baja/{id}")
    public String deleteEmpleado(@PathVariable("id") int id, Authentication authentication) {
        UnidadVenta unidadVenta = unidadVentaService.getUnidadVentaFromResponsableEmail(authentication.getName());
        empleadoService.logicalDelete(unidadVenta,id);
        return "redirect:/responsable/empleados";
    }

    @GetMapping("/platos")
    public ModelAndView showAllPlato(Authentication authentication) {
        ModelAndView mAV = new ModelAndView(ViewRouteHelper.RESPONSABLE_PLATOS);
        UnidadVenta unidadVenta = unidadVentaService.getUnidadVentaFromResponsableEmail(authentication.getName());
        mAV.addObject("platos", platoService.findByUnidadVenta(unidadVenta));
        return mAV;
    }

    @GetMapping("/platos/nuevo")
    public ModelAndView showFormPlato() {
        ModelAndView mAV = new ModelAndView(ViewRouteHelper.RESPONSABLE_PLATO_FORM);
        mAV.addObject("plato", new PlatoDTO());
        return mAV;
    }

    @PostMapping("/platos/guardar")
    public String savePlato(@Valid @ModelAttribute("plato") PlatoDTO platoDTO, BindingResult result, Authentication authentication) {
        if (result.hasErrors()) {
            return ViewRouteHelper.RESPONSABLE_PLATO_FORM;
        }

        UnidadVenta unidadVenta = unidadVentaService.getUnidadVentaFromResponsableEmail(authentication.getName());
        Plato plato = new Plato(platoDTO.getNombre(), platoDTO.getPrecioVenta(), platoDTO.getCostoProduccion(), unidadVenta);

        if (platoService.altaOReactivar(plato) == EnumResultadoGuardadoPlato.NOMBRE_DUPLICADO) {
            result.rejectValue("nombre", "duplicado", "Ya existe un plato con ese nombre");
            return ViewRouteHelper.RESPONSABLE_PLATO_FORM;
        }
        return "redirect:/responsable/platos";
    }

    @GetMapping("/platos/editar/{id}")
    public ModelAndView showFormEditPlato(@PathVariable("id") long id, Authentication authentication) {
        UnidadVenta unidadVenta = unidadVentaService.getUnidadVentaFromResponsableEmail(authentication.getName());
        Optional<Plato> platoOptional = platoService.findByIdAndUnidadVenta(id, unidadVenta);
        if (platoOptional.isEmpty()) {
            return new ModelAndView(new RedirectView("/responsable/platos"));
        }

        Plato plato = platoOptional.get();
        ModelAndView mAV = new ModelAndView(ViewRouteHelper.RESPONSABLE_PLATO_EDIT);
        mAV.addObject("plato", new PlatoDTO(plato.getId(), plato.getNombre(), plato.getPrecioVenta(), plato.getCostoProduccion()));
        return mAV;
    }

    @PostMapping("/platos/actualizar")
    public String updatePlato(@Valid @ModelAttribute("plato") PlatoDTO platoDTO, BindingResult result, Authentication authentication) {
        if (result.hasErrors()) {
            return ViewRouteHelper.RESPONSABLE_PLATO_EDIT;
        }

        UnidadVenta unidadVenta = unidadVentaService.getUnidadVentaFromResponsableEmail(authentication.getName());
        Plato datos = new Plato(platoDTO.getNombre(), platoDTO.getPrecioVenta(), platoDTO.getCostoProduccion(), unidadVenta);
        datos.setId(platoDTO.getId());

        EnumResultadoGuardadoPlato estado = platoService.actualizar(unidadVenta, datos);
        if (estado == EnumResultadoGuardadoPlato.NOMBRE_DUPLICADO) {
            result.rejectValue("nombre", "duplicado", "Ya existe un plato con ese nombre");
            return ViewRouteHelper.RESPONSABLE_PLATO_EDIT;
        }
        return "redirect:/responsable/platos";
    }

    @PostMapping("/platos/baja/{id}")
    public String deletePlato(@PathVariable("id") long id, Authentication authentication) {
        UnidadVenta unidadVenta = unidadVentaService.getUnidadVentaFromResponsableEmail(authentication.getName());
        platoService.logicalDelete(unidadVenta, id);
        return "redirect:/responsable/platos";
    }
}