package edu.unla.epicentro_gourmet.dtos;

import edu.unla.epicentro_gourmet.entities.EnumTemporadaFestival;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDate;

@Getter
@Builder
public class InformacionEmpleadoDTO {
    private final String nombre;
    private final String apellido;
    private final long dni;
    private final LocalDate fechaNacimiento;
    private final LocalDate fechaIngreso;
    private final long antiguedadAnios;
    private final String rol;          // Cajero o Cocinero
    private final String detalleRol;   // turno o categoría

    private final String festivalNombre;
    private final EnumTemporadaFestival temporada;
    private final LocalDate festivalInicio;
    private final LocalDate festivalFin;
    private final String estadoFestival; // Próximo, En curso o Finalizado

    private final String unidadNombre;
    private final String unidadCodigo;
    private final String tipoUnidad;
    private final double superficie;
}
