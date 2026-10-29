package edu.unla.epicentro_gourmet.services;

import edu.unla.epicentro_gourmet.dtos.ReporteVentaDTO;
import edu.unla.epicentro_gourmet.entities.UnidadVenta;

import java.util.Optional;

public interface IReporteVentaService {

    ReporteVentaDTO reporteDeUnidad(UnidadVenta unidad);

    // Vacío si el festival no existe.
    Optional<ReporteVentaDTO> reporteDeFestival(long idFestival);
}
