package edu.unla.epicentro_gourmet.dtos;

import lombok.AllArgsConstructor;
import lombok.Getter;

// No se persiste: es el resultado del cálculo del canon con su desglose.
@Getter
@AllArgsConstructor
public class DetalleCanonDTO {
    private final String nombreFestival;
    private final String nombreComercial;
    private final String codigo;
    private final String tipoUnidad;
    private final double superficie;
    private final double costoSuperficie;
    private final double importeSuperficie;
    private final double plusElectricidad;
    private final double tiempoMontajeMinutos;
    private final double costoMontaje;
    private final double descuentoMontaje;
    private final double canon;
}
