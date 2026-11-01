package edu.unla.epicentro_gourmet.services;

import edu.unla.epicentro_gourmet.dtos.PedidoAuditoriaDTO;
import edu.unla.epicentro_gourmet.entities.UnidadVenta;

import java.time.LocalDate;
import java.util.List;

public interface IAuditoriaPedidosService {

    // Desde y hasta son opcionales; si están las dos, desde no puede ser posterior a hasta.
    boolean rangoDeFechasValido(LocalDate desde, LocalDate hasta);

    // Filtros opcionales y combinables: rango de fechas (inclusivo), estado y plato. Más recientes primero.
    List<PedidoAuditoriaDTO> buscar(UnidadVenta unidad, LocalDate desde, LocalDate hasta,
                                    EnumFiltroEstadoPedido estado, Long idPlato);
}
