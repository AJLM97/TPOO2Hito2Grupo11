package edu.unla.epicentro_gourmet.services.implementation;

import edu.unla.epicentro_gourmet.entities.Pedido;
import edu.unla.epicentro_gourmet.repositories.IPedidoRepository;
import edu.unla.epicentro_gourmet.services.IPedidoService;
import org.springframework.stereotype.Service;

import java.util.List;

@Service("pedidoService")
public class PedidoService implements IPedidoService {

    private final IPedidoRepository pedidoRepository;

    public PedidoService(IPedidoRepository pedidoRepository) {
        this.pedidoRepository = pedidoRepository;
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
}
