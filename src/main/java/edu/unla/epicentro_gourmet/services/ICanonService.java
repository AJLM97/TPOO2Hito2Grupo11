package edu.unla.epicentro_gourmet.services;

import edu.unla.epicentro_gourmet.dtos.DetalleCanonDTO;
import edu.unla.epicentro_gourmet.entities.UnidadVenta;

import java.util.Optional;

public interface ICanonService {

    // Vacío si el festival de la unidad no tiene costos configurados.
    Optional<DetalleCanonDTO> calcular(UnidadVenta unidad);
}
