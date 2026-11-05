package edu.unla.epicentro_gourmet.dtos;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class PosicionRankingDTO {
    private final String nombreFestival;
    private final String nombreUnidad;
    private final String codigoUnidad;
    private final int puesto;
    private final int totalUnidades;
    private final double recaudacion;
    private final double diferenciaConLider;
    private final double diferenciaConSuperior;
    private final boolean lider;
    private final boolean empatada;
}
