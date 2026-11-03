package edu.unla.epicentro_gourmet.services;

import edu.unla.epicentro_gourmet.entities.Pedido;
import edu.unla.epicentro_gourmet.entities.UnidadVenta;

import java.util.List;
import java.util.Optional;

public interface IPedidoService {

    int CANTIDAD_MAXIMA_POR_PLATO = 99;

    public Pedido save(Pedido pedido);

    public List<Pedido> getAll();

    public Pedido findById(long id);

    public void delete(long id);

    public List<Pedido> findPedidosDelDia();

    public List<Pedido> findByUnidadVenta(UnidadVenta unidadVenta);

    public Optional<Pedido> findByIdAndUnidadVenta(long id, UnidadVenta unidadVenta);

    public Pedido abrir(UnidadVenta unidadVenta);

    EnumResultadoPedido agregarPlato(UnidadVenta unidadVenta, long idPedido, long idPlato, long cantidad);

    EnumResultadoPedido modificarCantidad(UnidadVenta unidadVenta, long idPedido, long idItem, long cantidad);

    EnumResultadoPedido quitarPlato(UnidadVenta unidadVenta, long idPedido, long idItem);

    EnumResultadoPedido cerrar(UnidadVenta unidadVenta, long idPedido);

    EnumResultadoPedido cancelar(UnidadVenta unidadVenta, long idPedido);
}
