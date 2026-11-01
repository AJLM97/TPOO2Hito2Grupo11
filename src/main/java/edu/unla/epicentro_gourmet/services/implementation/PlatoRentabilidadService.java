package edu.unla.epicentro_gourmet.services.implementation;

import edu.unla.epicentro_gourmet.dtos.PlatoRentabilidadDTO;
import edu.unla.epicentro_gourmet.entities.UnidadVenta;
import edu.unla.epicentro_gourmet.repositories.IItemPedidoRepository;
import edu.unla.epicentro_gourmet.services.IPlatoRentabilidadService;
import org.hibernate.Hibernate;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service("platoRentabilidadService")
public class PlatoRentabilidadService implements IPlatoRentabilidadService {
    private static final int MINIMO_PLATOS = 3;

    private final IItemPedidoRepository itemPedidoRepository;

    public PlatoRentabilidadService(IItemPedidoRepository itemPedidoRepository) {
        this.itemPedidoRepository = itemPedidoRepository;
    }

    @Override
    @Transactional(readOnly = true)
    public List<PlatoRentabilidadDTO> platosPorRentabilidad(UnidadVenta unidad, int cantidad) {
        if (cantidad < MINIMO_PLATOS) {
            throw new IllegalArgumentException("La cantidad de platos debe ser al menos 3.");
        }

        UnidadVenta real = (UnidadVenta) Hibernate.unproxy(unidad);
        return itemPedidoRepository.findPlatosPorRentabilidad(real, PageRequest.of(0, cantidad)).stream()
                .map(plato -> new PlatoRentabilidadDTO(
                        plato.getNombre(),
                        plato.getUnidadesVendidas(),
                        plato.getPrecioVenta(),
                        plato.getCostoProduccion(),
                        plato.getRentabilidad()))
                .toList();
    }
}
