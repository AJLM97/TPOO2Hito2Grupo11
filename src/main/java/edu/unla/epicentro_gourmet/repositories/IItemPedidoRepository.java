package edu.unla.epicentro_gourmet.repositories;

import edu.unla.epicentro_gourmet.entities.ItemPedido;
import edu.unla.epicentro_gourmet.entities.UnidadVenta;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository("itemPedidoRepository")
public interface IItemPedidoRepository extends JpaRepository<ItemPedido, Long> {

    interface PlatoPedido {
        Long getId();

        String getNombre();

        Double getPrecioVenta();

        Double getCostoProduccion();

        Long getUnidades();
    }

    // Más unidades primero; en un empate gana el plato cargado antes (menor id).
    @Query("SELECT p.id AS id, p.nombre AS nombre, p.precioVenta AS precioVenta, "
            + "p.costoProduccion AS costoProduccion, SUM(i.cantidad) AS unidades "
            + "FROM ItemPedido i JOIN i.plato p JOIN i.pedido pe "
            + "WHERE pe.unidadVenta = :unidad AND pe.cerrado = true "
            + "GROUP BY p.id, p.nombre, p.precioVenta, p.costoProduccion "
            + "ORDER BY SUM(i.cantidad) DESC, p.id ASC")
    List<PlatoPedido> findPlatosMasPedidos(@Param("unidad") UnidadVenta unidad, Pageable pageable);
}
