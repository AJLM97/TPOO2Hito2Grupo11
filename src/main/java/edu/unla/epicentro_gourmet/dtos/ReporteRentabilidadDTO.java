package edu.unla.epicentro_gourmet.dtos;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.time.LocalDate;

// No se persiste. Si desde y hasta son null, el reporte abarca todos los pedidos cerrados de la unidad.
@Getter
@AllArgsConstructor
public class ReporteRentabilidadDTO {
    private final String nombreFestival;
    private final String nombreUnidad;
    private final String codigoUnidad;
    private final LocalDate desde;
    private final LocalDate hasta;
    private final double ingresos;
    private final double costoPlatos;
    private final double sueldos;
    private final double canon;
    private final double rentabilidadNeta;

    public double getGananciaBruta() {
        return ingresos - costoPlatos;
    }
}
