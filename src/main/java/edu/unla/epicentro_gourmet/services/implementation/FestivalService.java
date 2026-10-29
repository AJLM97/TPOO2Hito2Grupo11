package edu.unla.epicentro_gourmet.services.implementation;

import edu.unla.epicentro_gourmet.dtos.RespuestaCostos;
import edu.unla.epicentro_gourmet.dtos.RespuestaFestival;
import edu.unla.epicentro_gourmet.dtos.RespuestaUnidadVenta;
import edu.unla.epicentro_gourmet.dtos.SolicitudCostos;
import edu.unla.epicentro_gourmet.dtos.SolicitudFestival;
import edu.unla.epicentro_gourmet.entities.Costos;
import edu.unla.epicentro_gourmet.entities.EnumAplicacionCosto;
import edu.unla.epicentro_gourmet.entities.Festival;
import edu.unla.epicentro_gourmet.entities.FoodTruck;
import edu.unla.epicentro_gourmet.entities.PuestoDesarmable;
import edu.unla.epicentro_gourmet.entities.UnidadVenta;
import edu.unla.epicentro_gourmet.repositories.IFestivalRepository;
import edu.unla.epicentro_gourmet.repositories.IUnidadVentaRepository;
import edu.unla.epicentro_gourmet.services.IFestivalService;
import org.modelmapper.ModelMapper;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.Comparator;
import java.util.List;

@Service("festivalService")
public class FestivalService implements IFestivalService {

    private final IFestivalRepository festivalRepository;
    private final IUnidadVentaRepository unidadVentaRepository;
    private final ModelMapper modelMapper;

    public FestivalService(IFestivalRepository festivalRepository,
                           IUnidadVentaRepository unidadVentaRepository,
                           ModelMapper modelMapper) {
        this.festivalRepository = festivalRepository;
        this.unidadVentaRepository = unidadVentaRepository;
        this.modelMapper = modelMapper;
    }

    @Override
    public List<Festival> getAll() {
        return festivalRepository.findAll();
    }

    @Override
    public Festival findById(long id) {
        return festivalRepository.findById(id).orElse(null);
    }

    @Override
    public void delete(long id) {
        festivalRepository.deleteById(id);
    }

    @Override
    @Transactional(readOnly = true)
    public List<RespuestaFestival> listar() {
        return festivalRepository.findAll(Sort.by(Sort.Direction.ASC, "fechaInicio"))
                .stream()
                .map(this::mapearRespuesta)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public RespuestaFestival buscar(Long id) {
        return mapearRespuesta(buscarPorId(id));
    }

    @Override
    @Transactional
    public RespuestaFestival crear(SolicitudFestival solicitud) {
        validar(solicitud);
        Festival festival = modelMapper.map(solicitud, Festival.class);
        festival.setId(null);
        festival.setCosto(mapearCostos(solicitud.getCostos()));
        return mapearRespuesta(festivalRepository.save(festival));
    }

    @Override
    @Transactional
    public RespuestaFestival actualizar(Long id, SolicitudFestival solicitud) {
        validar(solicitud);
        Festival festival = buscarPorId(id);
        modelMapper.map(solicitud, festival);
        actualizarCostos(festival, solicitud.getCostos());
        return mapearRespuesta(festivalRepository.save(festival));
    }

    @Override
    @Transactional
    public void eliminar(Long id) {
        Festival festival = buscarPorId(id);
        if (!festival.getUnidades().isEmpty()) {
            throw new ResponseStatusException(HttpStatus.CONFLICT,
                    "No se puede eliminar un festival con unidades de venta asociadas");
        }
        festivalRepository.delete(festival);
    }

    @Override
    @Transactional(readOnly = true)
    public List<RespuestaUnidadVenta> listarUnidades(Long festivalId) {
        Festival festival = buscarPorId(festivalId);
        return unidadVentaRepository.findByFestival_Id(festival.getId()).stream()
                .sorted(Comparator.comparing(UnidadVenta::getCodigo, String.CASE_INSENSITIVE_ORDER))
                .map(this::mapearUnidadVenta)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public List<Festival> listarFestivalesNoConcluidos() {
        return festivalRepository.findFestivalesNoConcluidos();
    }

    private Festival buscarPorId(Long id) {
        return festivalRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Festival no encontrado"));
    }

    private void validar(SolicitudFestival solicitud) {
        if (solicitud == null || solicitud.getNombre() == null || solicitud.getNombre().isBlank()
                || solicitud.getTemporada() == null || solicitud.getFechaInicio() == null
                || solicitud.getFechaFin() == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "Todos los campos del festival son obligatorios");
        }
        if (solicitud.getFechaFin().isBefore(solicitud.getFechaInicio())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "La fecha de fin no puede ser anterior a la fecha de inicio");
        }
        if (!costosValidos(solicitud.getCostos())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "Todos los costos del festival son obligatorios y deben ser mayores o iguales a cero");
        }
    }

    private boolean costosValidos(SolicitudCostos costos) {
        return costos != null
                && costoValido(costos.getCostoSuperficie())
                && costoValido(costos.getCostoMontaje())
                && costoValido(costos.getPlusElectricidad())
                && costoValido(costos.getPlusLavaplatos())
                && costoValido(costos.getPlusAyudante())
                && costoValido(costos.getPlusCocinero())
                && costoValido(costos.getPlusAntiguedad())
                && costoValido(costos.getSueldoBase())
                && costos.getAplicacionCostoSuperficie() != null
                && costos.getAplicacionCostoMontaje() != null
                && (costos.getAplicacionPlusElectricidad() == EnumAplicacionCosto.TODAS
                    || costos.getAplicacionPlusElectricidad() == EnumAplicacionCosto.FOOD_TRUCK);
    }

    private boolean costoValido(Float valor) {
        return valor != null && Float.isFinite(valor) && valor >= 0;
    }

    private Costos mapearCostos(SolicitudCostos solicitud) {
        Costos costos = new Costos(solicitud.getCostoSuperficie(), solicitud.getCostoMontaje(),
                solicitud.getPlusElectricidad(), solicitud.getPlusLavaplatos(),
                solicitud.getPlusAyudante(), solicitud.getPlusCocinero(),
                solicitud.getPlusAntiguedad(), solicitud.getSueldoBase());
        costos.setAplicacionCostoSuperficie(solicitud.getAplicacionCostoSuperficie());
        costos.setAplicacionCostoMontaje(solicitud.getAplicacionCostoMontaje());
        costos.setAplicacionPlusElectricidad(solicitud.getAplicacionPlusElectricidad());
        return costos;
    }

    private void actualizarCostos(Festival festival, SolicitudCostos solicitud) {
        Costos costos = festival.getCosto();
        if (costos == null) {
            festival.setCosto(mapearCostos(solicitud));
            return;
        }
        costos.setCostoSuperficie(solicitud.getCostoSuperficie());
        costos.setCostoMontaje(solicitud.getCostoMontaje());
        costos.setPlusElectricidad(solicitud.getPlusElectricidad());
        costos.setPlusLavaplatos(solicitud.getPlusLavaplatos());
        costos.setPlusAyudante(solicitud.getPlusAyudante());
        costos.setPlusCocinero(solicitud.getPlusCocinero());
        costos.setPlusAntiguedad(solicitud.getPlusAntiguedad());
        costos.setSueldoBase(solicitud.getSueldoBase());
        costos.setAplicacionCostoSuperficie(solicitud.getAplicacionCostoSuperficie());
        costos.setAplicacionCostoMontaje(solicitud.getAplicacionCostoMontaje());
        costos.setAplicacionPlusElectricidad(solicitud.getAplicacionPlusElectricidad());
    }

    private RespuestaFestival mapearRespuesta(Festival festival) {
        RespuestaFestival respuesta = modelMapper.map(festival, RespuestaFestival.class);
        respuesta.setCostos(modelMapper.map(festival.getCosto(), RespuestaCostos.class));
        return respuesta;
    }

    private RespuestaUnidadVenta mapearUnidadVenta(UnidadVenta unidad) {
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
