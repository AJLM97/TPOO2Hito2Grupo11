package edu.unla.epicentro_gourmet.services;

import edu.unla.epicentro_gourmet.dtos.ReporteVentaDTO;
import edu.unla.epicentro_gourmet.dtos.RankingUnidadVentaDTO;
import edu.unla.epicentro_gourmet.entities.UnidadVenta;

import java.util.List;
import java.util.Optional;

public interface IReporteVentaService {

    ReporteVentaDTO reporteDeUnidad(UnidadVenta unidad);

    // Vacío si el festival no existe.
    Optional<ReporteVentaDTO> reporteDeFestival(long idFestival);

    List<RankingUnidadVentaDTO> rankingUnidadesPorRecaudacion(long idFestival);
}
