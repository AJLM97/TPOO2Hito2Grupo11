package edu.unla.epicentro_gourmet.services;

import edu.unla.epicentro_gourmet.dtos.PuntoEquilibrioDTO;
import edu.unla.epicentro_gourmet.entities.UnidadVenta;

import java.time.LocalDate;
import java.util.Optional;

public interface IPuntoEquilibrioService {

    // Vacío si el festival de la unidad no tiene costos. hoy se recibe para poder informar el estado del festival.
    Optional<PuntoEquilibrioDTO> calcular(UnidadVenta unidad, LocalDate hoy);
}
