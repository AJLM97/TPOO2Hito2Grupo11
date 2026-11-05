package edu.unla.epicentro_gourmet.repositories;

import edu.unla.epicentro_gourmet.entities.Plato;
import edu.unla.epicentro_gourmet.entities.UnidadVenta;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.io.Serializable;
import java.util.List;
import java.util.Optional;

@Repository("platoRepository")
public interface IPlatoRepository extends JpaRepository<Plato, Serializable> {

    public abstract List<Plato> findByUnidadAndActivoTrue(UnidadVenta unidadVenta);

    public abstract Optional<Plato> findByIdAndUnidadAndActivoTrue(Long id, UnidadVenta unidadVenta);

    public abstract List<Plato> findByUnidadAndNombreIgnoreCase(UnidadVenta unidadVenta, String nombre);

    @Query("""
        SELECT p FROM Plato p
        WHERE p.unidad = :unidad
          AND p.activo = true
          AND (:precioMin IS NULL OR p.precioVenta >= :precioMin)
          AND (:precioMax IS NULL OR p.precioVenta <= :precioMax)
          AND (:margenMin IS NULL OR (p.precioVenta - p.costoProduccion) >= :margenMin)
          AND (:nombre IS NULL OR LOWER(p.nombre) LIKE LOWER(CONCAT('%', :nombre, '%')))
        ORDER BY p.nombre
        """)
    public abstract List<Plato> filtrar(@Param("unidad") UnidadVenta unidad,
                                        @Param("precioMin") Double precioMin,
                                        @Param("precioMax") Double precioMax,
                                        @Param("margenMin") Double margenMin,
                                        @Param("nombre") String nombre);
}
