package edu.unla.epicentro_gourmet.dtos;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.time.LocalDateTime;

// No se persiste: resumen de un pedido para la auditoría del responsable.
@Getter
@AllArgsConstructor
public class PedidoAuditoriaDTO {
    private final long id;
    private final LocalDateTime fechaTransaccion;
    private final boolean cerrado;
    private final int cantidadItems;
    private final long cantidadPlatos;
    private final double total;
}
