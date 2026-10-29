package edu.unla.epicentro_gourmet.dtos;

import lombok.AllArgsConstructor;
import lombok.Getter;

// No se persiste: el PlatoDTO más pedido junto con las unidades que se pidieron.
@Getter
@AllArgsConstructor
public class PlatoEstrellaDTO {
    private final PlatoDTO plato;
    private final long unidadesPedidas;
}
