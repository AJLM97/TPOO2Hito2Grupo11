package edu.unla.epicentro_gourmet.services;

import edu.unla.epicentro_gourmet.dtos.PlatoEstrellaDTO;
import edu.unla.epicentro_gourmet.entities.UnidadVenta;

import java.util.Optional;

public interface IPlatoEstrellaService {

    // Vacío si la unidad todavía no tiene pedidos cerrados.
    Optional<PlatoEstrellaDTO> platoEstrella(UnidadVenta unidad);
}
