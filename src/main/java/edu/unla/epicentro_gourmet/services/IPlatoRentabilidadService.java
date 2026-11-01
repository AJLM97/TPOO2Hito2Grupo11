package edu.unla.epicentro_gourmet.services;

import edu.unla.epicentro_gourmet.dtos.PlatoRentabilidadDTO;
import edu.unla.epicentro_gourmet.entities.UnidadVenta;

import java.util.List;

public interface IPlatoRentabilidadService {
    List<PlatoRentabilidadDTO> platosPorRentabilidad(UnidadVenta unidad, int cantidad);
}
