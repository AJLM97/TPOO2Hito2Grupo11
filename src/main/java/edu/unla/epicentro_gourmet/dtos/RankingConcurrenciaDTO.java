package edu.unla.epicentro_gourmet.dtos;

import edu.unla.epicentro_gourmet.entities.EnumTemporadaFestival;
import lombok.Getter;

import java.time.LocalDate;

@Getter
public class RankingConcurrenciaDTO {
    private final String nombreFestival;
    private final EnumTemporadaFestival temporada;
    private final int anio;
    private final long concurrenciaEstimada;

    public RankingConcurrenciaDTO(String nombreFestival, EnumTemporadaFestival temporada, LocalDate fechaInicio,
                                  Long concurrenciaEstimada) {
        this.nombreFestival = nombreFestival;
        this.temporada = temporada;
        this.anio = fechaInicio.getYear();
        this.concurrenciaEstimada = concurrenciaEstimada;
    }
}
