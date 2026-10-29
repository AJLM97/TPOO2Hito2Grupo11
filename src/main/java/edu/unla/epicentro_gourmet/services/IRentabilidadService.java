package edu.unla.epicentro_gourmet.services;

import edu.unla.epicentro_gourmet.dtos.ReporteRentabilidadDTO;
import edu.unla.epicentro_gourmet.entities.UnidadVenta;

import java.time.LocalDate;
import java.util.Optional;

public interface IRentabilidadService {

    // Vacío si el festival de la unidad no tiene costos: sin ellos no se pueden calcular sueldos ni canon.
    Optional<ReporteRentabilidadDTO> rentabilidad(UnidadVenta unidad);

    // Considera solo los pedidos del rango; sueldos y canon se restan completos.
    Optional<ReporteRentabilidadDTO> rentabilidad(UnidadVenta unidad, LocalDate desde, LocalDate hasta);

    EnumResultadoRangoFechas validarRango(UnidadVenta unidad, LocalDate desde, LocalDate hasta);
}
