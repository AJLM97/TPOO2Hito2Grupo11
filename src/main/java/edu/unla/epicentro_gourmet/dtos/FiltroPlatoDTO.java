package edu.unla.epicentro_gourmet.dtos;

import jakarta.validation.constraints.AssertTrue;
import jakarta.validation.constraints.PositiveOrZero;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class FiltroPlatoDTO {

    @PositiveOrZero(message = "El precio mínimo no puede ser negativo")
    private Double precioMin;

    @PositiveOrZero(message = "El precio máximo no puede ser negativo")
    private Double precioMax;

    private Double margenMin;

    private String nombre;

    @AssertTrue(message = "El precio mínimo no puede ser mayor que el máximo")
    public boolean isRangoPreciosValido() {
        return precioMin == null || precioMax == null || precioMin <= precioMax;
    }
}
