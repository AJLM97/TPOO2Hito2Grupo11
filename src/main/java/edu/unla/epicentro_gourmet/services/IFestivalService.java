package edu.unla.epicentro_gourmet.services;

import edu.unla.epicentro_gourmet.dtos.RespuestaFestival;
import edu.unla.epicentro_gourmet.dtos.RespuestaUnidadVenta;
import edu.unla.epicentro_gourmet.dtos.SolicitudFestival;
import edu.unla.epicentro_gourmet.entities.Festival;
import java.util.List;

public interface IFestivalService {

    public List<Festival> getAll();

    public Festival findById(long id);

    public void delete(long id);

    List<RespuestaFestival> listar();

    RespuestaFestival buscar(Long id);

    RespuestaFestival crear(SolicitudFestival solicitud);

    RespuestaFestival actualizar(Long id, SolicitudFestival solicitud);

    void eliminar(Long id);

    List<RespuestaUnidadVenta> listarUnidades(Long festivalId);
}
