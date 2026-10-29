package edu.unla.epicentro_gourmet.services.implementation;

import edu.unla.epicentro_gourmet.dtos.PedidoAuditoriaDTO;
import edu.unla.epicentro_gourmet.entities.ItemPedido;
import edu.unla.epicentro_gourmet.entities.Pedido;
import edu.unla.epicentro_gourmet.entities.UnidadVenta;
import edu.unla.epicentro_gourmet.repositories.IPedidoRepository;
import edu.unla.epicentro_gourmet.services.EnumFiltroEstadoPedido;
import edu.unla.epicentro_gourmet.services.IAuditoriaPedidosService;
import org.hibernate.Hibernate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;

@Service("auditoriaPedidosService")
public class AuditoriaPedidosService implements IAuditoriaPedidosService {

    private final IPedidoRepository pedidoRepository;

    public AuditoriaPedidosService(IPedidoRepository pedidoRepository) {
        this.pedidoRepository = pedidoRepository;
    }

    @Override
    public boolean rangoDeFechasValido(LocalDate desde, LocalDate hasta) {
        return desde == null || hasta == null || !desde.isAfter(hasta);
    }

    @Override
    @Transactional(readOnly = true)
    public List<PedidoAuditoriaDTO> buscar(UnidadVenta unidad, LocalDate desde, LocalDate hasta,
                                           EnumFiltroEstadoPedido estado, Long idPlato) {
        if (!rangoDeFechasValido(desde, hasta)) {
            throw new IllegalArgumentException("La fecha desde no puede ser posterior a la fecha hasta");
        }
        UnidadVenta real = (UnidadVenta) Hibernate.unproxy(unidad);
        LocalDateTime inicio = desde == null ? null : desde.atStartOfDay();
        LocalDateTime fin = hasta == null ? null : hasta.atTime(LocalTime.MAX);
        EnumFiltroEstadoPedido filtroEstado = estado == null ? EnumFiltroEstadoPedido.TODOS : estado;

        return pedidoRepository.findByUnidadVentaOrderByFechaTransaccionDesc(real).stream()
                .filter(pedido -> inicio == null || !pedido.getFechaTransaccion().isBefore(inicio))
                .filter(pedido -> fin == null || !pedido.getFechaTransaccion().isAfter(fin))
                .filter(pedido -> coincideEstado(pedido, filtroEstado))
                .filter(pedido -> idPlato == null || contienePlato(pedido, idPlato))
                .map(this::resumir)
                .toList();
    }

    private boolean coincideEstado(Pedido pedido, EnumFiltroEstadoPedido estado) {
        return switch (estado) {
            case TODOS -> true;
            case ABIERTO -> !pedido.isCerrado();
            case CERRADO -> pedido.isCerrado();
        };
    }

    private boolean contienePlato(Pedido pedido, long idPlato) {
        return pedido.getItems().stream().anyMatch(item -> item.getPlato().getId() == idPlato);
    }

    private PedidoAuditoriaDTO resumir(Pedido pedido) {
        long platos = pedido.getItems().stream().mapToLong(ItemPedido::getCantidad).sum();
        return new PedidoAuditoriaDTO(pedido.getId(), pedido.getFechaTransaccion(), pedido.isCerrado(),
                pedido.getItems().size(), platos, pedido.getTotal());
    }
}
