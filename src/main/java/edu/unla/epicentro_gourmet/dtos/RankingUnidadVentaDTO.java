package edu.unla.epicentro_gourmet.dtos;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class RankingUnidadVentaDTO {
    private final String nombreUnidad;
    private final String codigoUnidad;
    private final double recaudacion;
}
