package edu.unla.epicentro_gourmet.repositories;

import edu.unla.epicentro_gourmet.entities.Pedido;
import edu.unla.epicentro_gourmet.entities.UnidadVenta;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.io.Serializable;
import java.util.List;
import java.util.Optional;

@Repository("pedidoRepository")
public interface IPedidoRepository extends JpaRepository<Pedido, Serializable> {

    @EntityGraph(attributePaths = {"items", "items.plato"})
    List<Pedido> findByUnidadVentaOrderByFechaTransaccionDesc(UnidadVenta unidadVenta);

    @EntityGraph(attributePaths = {"items", "items.plato"})
    Optional<Pedido> findByIdAndUnidadVenta(Long id, UnidadVenta unidadVenta);
}
