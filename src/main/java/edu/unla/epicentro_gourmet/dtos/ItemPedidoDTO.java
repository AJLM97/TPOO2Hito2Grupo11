package edu.unla.epicentro_gourmet.dtos;

import edu.unla.epicentro_gourmet.services.IPedidoService;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class ItemPedidoDTO {
    @NotNull(message = "Debe seleccionar un plato")
    private Long idPlato;

    @NotNull(message = "La cantidad es obligatoria")
    @Min(value = 1, message = "La cantidad debe ser al menos 1")
    @Max(value = IPedidoService.CANTIDAD_MAXIMA_POR_PLATO, message = "La cantidad no puede superar " + IPedidoService.CANTIDAD_MAXIMA_POR_PLATO + " unidades por plato")
    private Long cantidad = 1L;
}
