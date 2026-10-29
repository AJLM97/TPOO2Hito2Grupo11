package edu.unla.epicentro_gourmet.services.implementation;

import edu.unla.epicentro_gourmet.dtos.PlatoDTO;
import edu.unla.epicentro_gourmet.dtos.PlatoEstrellaDTO;
import edu.unla.epicentro_gourmet.entities.UnidadVenta;
import edu.unla.epicentro_gourmet.repositories.IItemPedidoRepository;
import edu.unla.epicentro_gourmet.services.IPlatoEstrellaService;
import org.hibernate.Hibernate;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Service("platoEstrellaService")
public class PlatoEstrellaService implements IPlatoEstrellaService {

    private final IItemPedidoRepository itemPedidoRepository;

    public PlatoEstrellaService(IItemPedidoRepository itemPedidoRepository) {
        this.itemPedidoRepository = itemPedidoRepository;
    }

    // Cuenta las unidades pedidas en pedidos cerrados; un plato dado de baja sigue contando por lo ya vendido.
    @Override
    @Transactional(readOnly = true)
    public Optional<PlatoEstrellaDTO> platoEstrella(UnidadVenta unidad) {
        UnidadVenta real = (UnidadVenta) Hibernate.unproxy(unidad);
        return itemPedidoRepository.findPlatosMasPedidos(real, PageRequest.of(0, 1)).stream()
                .findFirst()
                .map(plato -> new PlatoEstrellaDTO(
                        new PlatoDTO(plato.getId(), plato.getNombre(), plato.getPrecioVenta(), plato.getCostoProduccion()),
                        plato.getUnidades()));
    }
}
