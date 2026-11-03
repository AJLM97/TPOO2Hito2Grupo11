package edu.unla.epicentro_gourmet.dtos;

import edu.unla.epicentro_gourmet.entities.EnumCategoriaCocinero;
import edu.unla.epicentro_gourmet.entities.EnumTurnoCajero;
import jakarta.validation.constraints.AssertTrue;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.format.annotation.DateTimeFormat;

import java.time.LocalDate;

@Getter
@Setter
@NoArgsConstructor
public class FiltroEmpleadoDTO {
    private String nombre;
    private String apellido;
    private Long dni;
    private String tipoEmpleado;                    // "CAJERO", "COCINERO" o vacío
    private EnumCategoriaCocinero categoriaCocinero;
    private EnumTurnoCajero turnoTrabajo;

    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
    private LocalDate ingresoDesde;
    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
    private LocalDate ingresoHasta;

    @AssertTrue(message = "La fecha 'desde' no puede ser posterior a la fecha 'hasta'")
    public boolean isRangoFechasValido() {
        return ingresoDesde == null || ingresoHasta == null || !ingresoDesde.isAfter(ingresoHasta);
    }
}
