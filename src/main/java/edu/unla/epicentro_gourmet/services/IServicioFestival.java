package edu.unla.epicentro_gourmet.services;

import edu.unla.epicentro_gourmet.dtos.RespuestaFestival;
import edu.unla.epicentro_gourmet.dtos.SolicitudFestival;

public interface IServicioFestival {
    RespuestaFestival crear(SolicitudFestival solicitud);

    RespuestaFestival actualizar(Long id, SolicitudFestival solicitud);

    void eliminar(Long id);
}