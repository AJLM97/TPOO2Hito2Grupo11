package edu.unla.epicentro_gourmet.dtos;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class EmpleadoDTO {
    private Long idUnidadVenta;
    private String nombre;
    private String apellido;
    private Long dni;
    private String tipoEmpleado;
}
