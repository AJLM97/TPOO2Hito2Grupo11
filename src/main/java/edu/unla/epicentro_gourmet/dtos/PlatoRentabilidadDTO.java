package edu.unla.epicentro_gourmet.dtos;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class PlatoRentabilidadDTO {
    private final String nombre;
    private final long unidadesVendidas;
    private final double precioVenta;
    private final double costoProduccion;
    private final double rentabilidad;
}
