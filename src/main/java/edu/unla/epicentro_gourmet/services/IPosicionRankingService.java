package edu.unla.epicentro_gourmet.services;

import edu.unla.epicentro_gourmet.dtos.PosicionRankingDTO;
import edu.unla.epicentro_gourmet.entities.UnidadVenta;

import java.util.Optional;

public interface IPosicionRankingService {
    Optional<PosicionRankingDTO> posicionDeUnidad(UnidadVenta unidadVenta);
}
