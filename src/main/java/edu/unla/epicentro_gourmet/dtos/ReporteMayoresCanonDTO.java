package edu.unla.epicentro_gourmet.dtos;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class ReporteMayoresCanonDTO {
    private final String nombreComercial;
    private final String codigo;
    private final String tipoUnidad;
    private final double canon;
    private final DetalleCanonDTO detalleCanon;
}
