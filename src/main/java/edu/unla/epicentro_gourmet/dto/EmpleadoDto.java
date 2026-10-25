package edu.unla.epicentro_gourmet.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class EmpleadoDto {
    private Long idUnidadVenta;  // Para filtrar empleados de una unidad específica
    private String nombre;       // Búsqueda parcial por nombre
    private String apellido;     // Búsqueda parcial por apellido
    private Long dni;            // Búsqueda exacta por DNI
    private String tipoEmpleado; // "CAJERO", "COCINERO" o "TODOS"
}
