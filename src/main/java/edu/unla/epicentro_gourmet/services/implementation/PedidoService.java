package edu.unla.epicentro_gourmet.services.implementation;

import edu.unla.epicentro_gourmet.entities.ItemPedido;
import edu.unla.epicentro_gourmet.entities.Pedido;
import edu.unla.epicentro_gourmet.entities.Plato;
import edu.unla.epicentro_gourmet.entities.UnidadVenta;
import edu.unla.epicentro_gourmet.repositories.IPedidoRepository;
import edu.unla.epicentro_gourmet.services.EnumResultadoPedido;
import edu.unla.epicentro_gourmet.services.IPedidoService;
import edu.unla.epicentro_gourmet.services.IPlatoService;
import edu.unla.epicentro_gourmet.services.IUnidadVentaService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service("pedidoService")
public class PedidoService implements IPedidoService {

    private final IPedidoRepository pedidoRepository;
    private final IPlatoService platoService;
    private final IUnidadVentaService unidadVentaService;

    public PedidoService(IPedidoRepository pedidoRepository, IPlatoService platoService,
                         IUnidadVentaService unidadVentaService) {
        this.pedidoRepository = pedidoRepository;
        this.platoService = platoService;
        this.unidadVentaService = unidadVentaService;
    }

    @Override
    public Pedido save(Pedido pedido) {
        return pedidoRepository.save(pedido);
    }

    @Override
    public List<Pedido> getAll() {
        return pedidoRepository.findAll();
    }

    @Override
    public Pedido findById(long id) {
        return pedidoRepository.findById(id).orElse(null);
    }

    @Override
    public void delete(long id) {
        pedidoRepository.deleteById(id);
    }

    @Override
    public List<Pedido> findPedidosDelDia() {
        return pedidoRepository.findAll();
    }

    @Override
    @Transactional(readOnly = true)
    public List<Pedido> findByUnidadVenta(UnidadVenta unidadVenta) {
        return pedidoRepository.findByUnidadVentaOrderByFechaTransaccionDesc(unidadVenta);
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<Pedido> findByIdAndUnidadVenta(long id, UnidadVenta unidadVenta) {
        return pedidoRepository.findByIdAndUnidadVenta(id, unidadVenta);
    }

    // La fecha la asigna el sistema al abrir el pedido: nunca se carga a mano.
    @Override
    @Transactional
    public Pedido abrir(UnidadVenta unidadVenta) {
        UnidadVenta unidad = unidadVentaService.findById(unidadVenta.getId())
                .orElseThrow(() -> new IllegalArgumentException("La unidad de venta no existe"));
        Pedido pedido = new Pedido(LocalDateTime.now(), unidad);
        pedido.setFestival(unidad.getFestival());
        return pedidoRepository.save(pedido);
    }

    @Override
    @Transactional
    public EnumResultadoPedido agregarPlato(UnidadVenta unidadVenta, long idPedido, long idPlato, long cantidad) {
        Optional<Pedido> encontrado = pedidoRepository.findByIdAndUnidadVenta(idPedido, unidadVenta);
        if (encontrado.isEmpty()) {
            return EnumResultadoPedido.NO_ENCONTRADO;
        }
        Pedido pedido = encontrado.get();
        if (pedido.isCerrado()) {
            return EnumResultadoPedido.PEDIDO_CERRADO;
        }
        if (!cantidadValida(cantidad)) {
            return EnumResultadoPedido.CANTIDAD_INVALIDA;
        }
        Optional<Plato> plato = platoService.findByIdAndUnidadVenta(idPlato, unidadVenta);
        if (plato.isEmpty()) {
            return EnumResultadoPedido.PLATO_NO_DISPONIBLE;
        }

        Optional<ItemPedido> existente = pedido.getItems().stream()
                .filter(item -> item.getPlato().getId().equals(idPlato))
                .findFirst();
        if (existente.isPresent()) {
            long nuevaCantidad = existente.get().getCantidad() + cantidad;
            if (!cantidadValida(nuevaCantidad)) {
                return EnumResultadoPedido.CANTIDAD_INVALIDA;
            }
            existente.get().setCantidad(nuevaCantidad);
        } else {
            pedido.getItems().add(new ItemPedido(plato.get(), pedido, cantidad));
        }
        pedidoRepository.save(pedido);
        return EnumResultadoPedido.OK;
    }

    @Override
    @Transactional
    public EnumResultadoPedido modificarCantidad(UnidadVenta unidadVenta, long idPedido, long idItem, long cantidad) {
        Optional<Pedido> encontrado = pedidoRepository.findByIdAndUnidadVenta(idPedido, unidadVenta);
        if (encontrado.isEmpty()) {
            return EnumResultadoPedido.NO_ENCONTRADO;
        }
        Pedido pedido = encontrado.get();
        if (pedido.isCerrado()) {
            return EnumResultadoPedido.PEDIDO_CERRADO;
        }
        Optional<ItemPedido> item = pedido.getItems().stream()
                .filter(i -> i.getIdItemPedido() == idItem)
                .findFirst();
        if (item.isEmpty()) {
            return EnumResultadoPedido.ITEM_NO_ENCONTRADO;
        }
        if (!cantidadValida(cantidad)) {
            return EnumResultadoPedido.CANTIDAD_INVALIDA;
        }
        item.get().setCantidad(cantidad);
        pedidoRepository.save(pedido);
        return EnumResultadoPedido.OK;
    }

    @Override
    @Transactional
    public EnumResultadoPedido quitarPlato(UnidadVenta unidadVenta, long idPedido, long idItem) {
        Optional<Pedido> encontrado = pedidoRepository.findByIdAndUnidadVenta(idPedido, unidadVenta);
        if (encontrado.isEmpty()) {
            return EnumResultadoPedido.NO_ENCONTRADO;
        }
        Pedido pedido = encontrado.get();
        if (pedido.isCerrado()) {
            return EnumResultadoPedido.PEDIDO_CERRADO;
        }
        boolean quitado = pedido.getItems().removeIf(item -> item.getIdItemPedido() == idItem);
        if (!quitado) {
            return EnumResultadoPedido.ITEM_NO_ENCONTRADO;
        }
        pedidoRepository.save(pedido);
        return EnumResultadoPedido.OK;
    }

    @Override
    @Transactional
    public EnumResultadoPedido cerrar(UnidadVenta unidadVenta, long idPedido) {
        Optional<Pedido> encontrado = pedidoRepository.findByIdAndUnidadVenta(idPedido, unidadVenta);
        if (encontrado.isEmpty()) {
            return EnumResultadoPedido.NO_ENCONTRADO;
        }
        Pedido pedido = encontrado.get();
        if (pedido.isCerrado()) {
            return EnumResultadoPedido.PEDIDO_CERRADO;
        }
        if (pedido.getItems().isEmpty()) {
            return EnumResultadoPedido.SIN_PLATOS;
        }
        pedido.setCerrado(true);
        pedidoRepository.save(pedido);
        return EnumResultadoPedido.OK;
    }

    @Override
    @Transactional
    public EnumResultadoPedido cancelar(UnidadVenta unidadVenta, long idPedido) {
        Optional<Pedido> encontrado = pedidoRepository.findByIdAndUnidadVenta(idPedido, unidadVenta);
        if (encontrado.isEmpty()) {
            return EnumResultadoPedido.NO_ENCONTRADO;
        }
        if (encontrado.get().isCerrado()) {
            return EnumResultadoPedido.PEDIDO_CERRADO;
        }
        pedidoRepository.delete(encontrado.get());
        return EnumResultadoPedido.OK;
    }

    private boolean cantidadValida(long cantidad) {
        return cantidad >= 1 && cantidad <= CANTIDAD_MAXIMA_POR_PLATO;
    }
}
