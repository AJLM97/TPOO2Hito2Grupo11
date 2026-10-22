package edu.unla.epicentro_gourmet.dtos;

import edu.unla.epicentro_gourmet.entities.EnumTemporadaFestival;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDate;

@Getter
@Setter
@NoArgsConstructor
public class SolicitudFestival {
    private Long id;
    private String nombre;
    private EnumTemporadaFestival temporada;
    private LocalDate fechaInicio;
    private LocalDate fechaFin;
}