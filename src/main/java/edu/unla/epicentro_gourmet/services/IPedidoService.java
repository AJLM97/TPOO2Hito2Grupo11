package edu.unla.epicentro_gourmet.services;

import edu.unla.epicentro_gourmet.entities.Pedido;
import java.util.List;

public interface IPedidoService {

    public Pedido save(Pedido pedido);

    public List<Pedido> getAll();

    public Pedido findById(long id);

    public void delete(long id);

    public List<Pedido> findPedidosDelDia();
}
