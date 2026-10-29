package edu.unla.epicentro_gourmet.services;

import edu.unla.epicentro_gourmet.dtos.LiquidacionHaberesDTO;
import edu.unla.epicentro_gourmet.entities.Empleado;
import edu.unla.epicentro_gourmet.entities.UnidadVenta;

import java.util.List;
import java.util.Optional;

public interface ILiquidacionService {

    // Vacío si la unidad no tiene festival o el festival no tiene costos configurados.
    Optional<List<LiquidacionHaberesDTO>> liquidarUnidad(UnidadVenta unidad);

    // Vacío si el empleado no tiene unidad o su festival no tiene costos configurados.
    Optional<LiquidacionHaberesDTO> liquidar(Empleado empleado);
}
