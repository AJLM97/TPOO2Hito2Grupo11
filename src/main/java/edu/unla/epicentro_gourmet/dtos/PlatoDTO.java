package edu.unla.epicentro_gourmet.dtos;

import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.PositiveOrZero;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class PlatoDTO {
    private long id;

    @NotBlank(message = "El nombre del plato es obligatorio")
    @Size(min = 3, max = 50, message = "El nombre debe tener entre 3 y 50 caracteres")
    private String nombre;

    @NotNull(message = "El precio de venta es obligatorio")
    @Positive(message = "El precio de venta debe ser mayor que cero")
    @Digits(integer = 9, fraction = 2, message = "El precio de venta debe tener como máximo 9 cifras enteras y 2 decimales")
    private Double precioVenta;

    @NotNull(message = "El costo de producción es obligatorio")
    @PositiveOrZero(message = "El costo de producción no puede ser negativo")
    @Digits(integer = 9, fraction = 2, message = "El costo de producción debe tener como máximo 9 cifras enteras y 2 decimales")
    private Double costoProduccion;

    public PlatoDTO(long id, String nombre, Double precioVenta, Double costoProduccion) {
        this.id = id;
        this.nombre = nombre;
        this.precioVenta = precioVenta;
        this.costoProduccion = costoProduccion;
    }
}
