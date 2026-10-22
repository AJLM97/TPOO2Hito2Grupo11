package edu.unla.epicentro_gourmet.services.implementation;

import edu.unla.epicentro_gourmet.dtos.RespuestaFestival;
import edu.unla.epicentro_gourmet.dtos.RespuestaUnidadVenta;
import edu.unla.epicentro_gourmet.dtos.SolicitudFestival;
import edu.unla.epicentro_gourmet.entities.Festival;
import edu.unla.epicentro_gourmet.entities.FoodTruck;
import edu.unla.epicentro_gourmet.entities.PuestoDesarmable;
import edu.unla.epicentro_gourmet.entities.UnidadVenta;
import edu.unla.epicentro_gourmet.repositories.IFestivalRepositorio;
import edu.unla.epicentro_gourmet.services.IServicioFestival;
import org.modelmapper.ModelMapper;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

@Service("servicioFestival")
@Transactional
public class ServicioFestival implements IServicioFestival {
    private final IFestivalRepositorio festivalRepositorio;
    private final ModelMapper modelMapper;

    public ServicioFestival(IFestivalRepositorio festivalRepositorio, ModelMapper modelMapper) {
        this.festivalRepositorio = festivalRepositorio;
        this.modelMapper = modelMapper;
    }

    @Override
    @Transactional(readOnly = true)
    public List<RespuestaFestival> listar() {
        return festivalRepositorio.findAll(Sort.by(Sort.Direction.ASC, "fechInicio")).stream()
                .map(festival -> modelMapper.map(festival, RespuestaFestival.class))
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public RespuestaFestival buscar(Long id) {
        return modelMapper.map(buscarEntidad(id), RespuestaFestival.class);
    }

    @Override
    public RespuestaFestival crear(SolicitudFestival solicitud) {
        validar(solicitud);
        Festival festival = modelMapper.map(solicitud, Festival.class);
        return modelMapper.map(festivalRepositorio.save(festival), RespuestaFestival.class);
    }

    @Override
    public RespuestaFestival actualizar(Long id, SolicitudFestival solicitud) {
        validar(solicitud);
        Festival festival = buscarEntidad(id);
        modelMapper.map(solicitud, festival);
        return modelMapper.map(festivalRepositorio.save(festival), RespuestaFestival.class);
    }

    @Override
    public void eliminar(Long id) {
        Festival festival = buscarEntidad(id);
        if (!festival.getUnidades().isEmpty()) {
            throw new ResponseStatusException(HttpStatus.CONFLICT,
                    "No se puede eliminar un festival que tiene unidades de venta asociadas");
        }
        festivalRepositorio.delete(festival);
    }

    @Override
    @Transactional(readOnly = true)
    public List<RespuestaUnidadVenta> listarUnidades(Long festivalId) {
        Festival festival = buscarEntidad(festivalId);
        return festival.getUnidades().stream()
                .sorted((primera, segunda) -> primera.getCodigo().compareToIgnoreCase(segunda.getCodigo()))
                .map(this::aRespuestaUnidad)
                .toList();
    }

    private Festival buscarEntidad(Long id) {
        return festivalRepositorio.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Festival no encontrado"));
    }

    private void validar(SolicitudFestival solicitud) {
        if (solicitud == null || solicitud.getNombre() == null || solicitud.getNombre().isBlank()
                || solicitud.getTemporada() == null || solicitud.getFechaInicio() == null || solicitud.getFechaFin() == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Todos los campos del festival son obligatorios");
        }
        if (solicitud.getFechaFin().isBefore(solicitud.getFechaInicio())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "La fecha de fin no puede ser anterior a la fecha de inicio");
        }
    }

    private RespuestaUnidadVenta aRespuestaUnidad(UnidadVenta unidad) {
        RespuestaUnidadVenta respuesta = modelMapper.map(unidad, RespuestaUnidadVenta.class);
        if (unidad instanceof FoodTruck foodTruck) {
            respuesta.setTipo("FoodTruck");
            respuesta.setPatente(foodTruck.getPatente());
            respuesta.setConexionElectrica(foodTruck.isConexionElectrica());
        } else if (unidad instanceof PuestoDesarmable puesto) {
            respuesta.setTipo("PuestoDesarmable");
            respuesta.setCantidadDeCarpas(puesto.getCantidadDeCarpas());
            respuesta.setTiempoMontajeMinutos(puesto.getTiempoMontajeMinutos());
        }
        return respuesta;
    }
}