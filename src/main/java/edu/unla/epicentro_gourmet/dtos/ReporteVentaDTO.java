package edu.unla.epicentro_gourmet.dtos;

import lombok.AllArgsConstructor;
import lombok.Getter;

// No se persiste. En el reporte de un festival completo, nombreUnidad y codigoUnidad quedan en null.
@Getter
@AllArgsConstructor
public class ReporteVentaDTO {
    private final String nombreFestival;
    private final String nombreUnidad;
    private final String codigoUnidad;
    private final long cantidadPedidos;
    private final long cantidadPlatos;
    private final double recaudacion;
}
