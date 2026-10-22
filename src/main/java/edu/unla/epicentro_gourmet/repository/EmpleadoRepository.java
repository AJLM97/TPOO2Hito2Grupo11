package edu.unla.epicentro_gourmet.repository;

import edu.unla.epicentro_gourmet.entities.Empleado;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface EmpleadoRepository extends JpaRepository<Empleado, Long> {

    @Query("SELECT DISTINCT e FROM Empleado e " +
            "LEFT JOIN e.usuario u " +
            "JOIN e.unidadesVenta uv " +
            "WHERE (u IS NULL OR u.bajaLogica = false) " +
            "AND (:idUnidad IS NULL OR uv.idUnidadVenta = :idUnidad) " +
            "AND (:nombre IS NULL OR LOWER(e.nombre) LIKE LOWER(CONCAT('%', :nombre, '%'))) " +
            "AND (:apellido IS NULL OR LOWER(e.apellido) LIKE LOWER(CONCAT('%', :apellido, '%'))) " +
            "AND (:dni IS NULL OR e.dni = :dni)")
    List<Empleado> filtrarEmpleadosActivos(
            @Param("idUnidad") Long idUnidad,
            @Param("nombre") String nombre,
            @Param("apellido") String apellido,
            @Param("dni") Long dni
    );
}
