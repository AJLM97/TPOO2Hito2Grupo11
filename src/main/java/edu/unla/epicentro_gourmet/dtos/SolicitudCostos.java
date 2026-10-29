package edu.unla.epicentro_gourmet.dtos;

import edu.unla.epicentro_gourmet.entities.EnumAplicacionCosto;
import jakarta.validation.constraints.AssertTrue;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class SolicitudCostos {
    @NotNull(message = "El costo por superficie es obligatorio")
    @PositiveOrZero(message = "El costo por superficie no puede ser negativo")
    private Float costoSuperficie;

    @NotNull(message = "El costo por montaje es obligatorio")
    @PositiveOrZero(message = "El costo por montaje no puede ser negativo")
    private Float costoMontaje;

    @NotNull(message = "El plus por electricidad es obligatorio")
    @PositiveOrZero(message = "El plus por electricidad no puede ser negativo")
    private Float plusElectricidad;

    @NotNull(message = "El plus de lavaplatos es obligatorio")
    @PositiveOrZero(message = "El plus de lavaplatos no puede ser negativo")
    private Float plusLavaplatos;

    @NotNull(message = "El plus de cocinero ayudante es obligatorio")
    @PositiveOrZero(message = "El plus de cocinero ayudante no puede ser negativo")
    private Float plusAyudante;

    @NotNull(message = "El plus de cocinero es obligatorio")
    @PositiveOrZero(message = "El plus de cocinero no puede ser negativo")
    private Float plusCocinero;

    @NotNull(message = "El plus por antigüedad es obligatorio")
    @PositiveOrZero(message = "El plus por antigüedad no puede ser negativo")
    private Float plusAntiguedad;

    @NotNull(message = "El sueldo base es obligatorio")
    @PositiveOrZero(message = "El sueldo base no puede ser negativo")
    private Float sueldoBase;

    @NotNull(message = "Debe indicar a qué unidades aplica el costo por superficie")
    private EnumAplicacionCosto aplicacionCostoSuperficie = EnumAplicacionCosto.TODAS;

    @NotNull(message = "Debe indicar a qué unidades aplica el costo por montaje")
    private EnumAplicacionCosto aplicacionCostoMontaje = EnumAplicacionCosto.PUESTO_DESARMABLE;

    @NotNull(message = "Debe indicar a qué unidades aplica el plus por electricidad")
    private EnumAplicacionCosto aplicacionPlusElectricidad = EnumAplicacionCosto.FOOD_TRUCK;

    @AssertTrue(message = "El plus por electricidad solo puede aplicarse a todas las unidades o a Food Trucks")
    public boolean isAplicacionElectricidadValida() {
        return aplicacionPlusElectricidad == null
                || aplicacionPlusElectricidad != EnumAplicacionCosto.PUESTO_DESARMABLE;
    }
}