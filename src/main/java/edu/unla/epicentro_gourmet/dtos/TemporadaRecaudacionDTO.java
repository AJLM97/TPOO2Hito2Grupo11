package edu.unla.epicentro_gourmet.dtos;

import edu.unla.epicentro_gourmet.entities.EnumTemporadaFestival;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class TemporadaRecaudacionDTO{
    private final EnumTemporadaFestival temporada;
    private final double promedioRecaudado;
    private final String festivalMayorRecaudacion;
}