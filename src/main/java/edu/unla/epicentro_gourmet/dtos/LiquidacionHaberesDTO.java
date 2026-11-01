package edu.unla.epicentro_gourmet.dtos;

import lombok.AllArgsConstructor;
import lombok.Getter;

// No se persiste: es el resultado de liquidar el sueldo de un empleado en un festival.
@Getter
@AllArgsConstructor
public class LiquidacionHaberesDTO {
    private final long idEmpleado;
    private final String nombreCompleto;
    private final String rol;
    private final double sueldoBase;
    private final String conceptoAdicional;
    private final double adicional;
    private final long diasFestival;
    private final double porcentaje;
    private final double total;
}
