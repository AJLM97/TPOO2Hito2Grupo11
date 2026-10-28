package edu.unla.epicentro_gourmet.dtos;

import edu.unla.epicentro_gourmet.entities.EnumTemporadaFestival;
import jakarta.validation.Valid;
import jakarta.validation.constraints.AssertTrue;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.format.annotation.DateTimeFormat;

import java.time.LocalDate;

@Getter
@Setter
@NoArgsConstructor
public class SolicitudFestival {
    private Long id;

    @NotBlank(message = "El nombre del festival es obligatorio")
    private String nombre;

    @NotNull(message = "La temporada es obligatoria")
    private EnumTemporadaFestival temporada;

    @NotNull(message = "La fecha de inicio es obligatoria")
    @DateTimeFormat(pattern = "yyyy-MM-dd")
    private LocalDate fechaInicio;

    @NotNull(message = "La fecha de fin es obligatoria")
    @DateTimeFormat(pattern = "yyyy-MM-dd")
    private LocalDate fechaFin;

    @Valid
    @NotNull(message = "Los costos del festival son obligatorios")
    private SolicitudCostos costos = new SolicitudCostos();

    @AssertTrue(message = "La fecha de fin no puede ser anterior a la fecha de inicio")
    public boolean isPeriodoValido() {
        return fechaInicio == null || fechaFin == null || !fechaFin.isBefore(fechaInicio);
    }
}