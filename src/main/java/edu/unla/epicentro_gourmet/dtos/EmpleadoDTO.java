package edu.unla.epicentro_gourmet.dtos;

import jakarta.validation.constraints.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.format.annotation.DateTimeFormat;

import java.time.LocalDate;

@Getter
@Setter
@NoArgsConstructor
public class EmpleadoDTO {
    private long id;
    @Size(min = 3, max = 25, message = "El nombre debe tener entre 3 a 25 caracteres")
    private String nombre;
    @Size(min = 3, max = 25, message = "El apellido debe tener entre 3 a 25 caracteres")
    private String apellido;
    @Min(value = 1_000_000, message = "El DNI debe tener al menos 7 cifras")
    @Max(value = 99_999_999, message = "El DNI no puede tener más de 8 cifras")
    private long dni;

    @NotNull(message = "La fecha de nacimiento es obligatoria")
    @DateTimeFormat(pattern = "yyyy-MM-dd")
    private LocalDate fechaNacimiento;

    private LocalDate fechaIngreso = LocalDate.now();

    private String emailPersonal;
    private String tipoEmpleado;
    private String turnoTrabajo;
    private String categoriaCocinero;

    public EmpleadoDTO(long id, String nombre, String apellido, long dni, LocalDate fechaNacimiento, double sueldoBase, String emailPersonal, String tipoEmpleado, String turnoTrabajo, String categoriaCocinero) {
        this.setId(id);
        this.nombre = nombre;
        this.apellido = apellido;
        this.dni = dni;
        this.fechaNacimiento = fechaNacimiento;
        this.emailPersonal = emailPersonal;
        this.tipoEmpleado = tipoEmpleado;
        this.turnoTrabajo = turnoTrabajo;
        this.categoriaCocinero = categoriaCocinero;
    }

    @AssertTrue(message = "El empleado debe tener al menos 18 años")
    public boolean isEdadMinimaValida() {
        return fechaNacimiento == null
                || !fechaNacimiento.isAfter(LocalDate.now().minusYears(18));
    }
}
