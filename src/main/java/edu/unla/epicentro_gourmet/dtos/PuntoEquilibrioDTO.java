package edu.unla.epicentro_gourmet.dtos;

import edu.unla.epicentro_gourmet.services.EnumEstadoFestival;
import lombok.AllArgsConstructor;
import lombok.Getter;

// No se persiste. ventasNecesarias y ventasFaltantes son null si todavía no hay un margen con el que estimarlas.
@Getter
@AllArgsConstructor
public class PuntoEquilibrioDTO {
    private final String nombreFestival;
    private final String nombreUnidad;
    private final String codigoUnidad;
    private final EnumEstadoFestival estadoFestival;
    private final double canon;
    private final double sueldos;
    private final double costosFijos;
    private final double ventas;
    private final double costoPlatos;
    private final double gananciaBruta;
    private final double porcentajeCubierto;
    private final boolean equilibrioAlcanzado;
    private final double gananciaFaltante;
    private final Double ventasNecesarias;
    private final Double ventasFaltantes;
}
