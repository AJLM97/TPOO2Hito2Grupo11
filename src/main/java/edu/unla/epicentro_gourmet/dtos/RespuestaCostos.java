package edu.unla.epicentro_gourmet.dtos;

import edu.unla.epicentro_gourmet.entities.EnumAplicacionCosto;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class RespuestaCostos {
    private float costoSuperficie;
    private float costoMontaje;
    private float plusElectricidad;
    private float plusLavaplatos;
    private float plusAyudante;
    private float plusCocinero;
    private float plusAntiguedad;
    private float sueldoBase;
    private EnumAplicacionCosto aplicacionCostoSuperficie;
    private EnumAplicacionCosto aplicacionCostoMontaje;
    private EnumAplicacionCosto aplicacionPlusElectricidad;
}