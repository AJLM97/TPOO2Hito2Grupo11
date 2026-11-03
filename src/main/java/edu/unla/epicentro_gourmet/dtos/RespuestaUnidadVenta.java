package edu.unla.epicentro_gourmet.dtos;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class RespuestaUnidadVenta {
    private Long id;
    private String codigo;
    private String nombreComercial;
    private String tipo;
    private double superficie;
    private String patente;
    private Boolean conexionElectrica;
    private Long cantidadDeCarpas;
    private Long tiempoMontajeMinutos;
}