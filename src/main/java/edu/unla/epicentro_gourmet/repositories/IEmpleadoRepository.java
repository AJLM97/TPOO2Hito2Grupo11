package edu.unla.epicentro_gourmet.repositories;

import edu.unla.epicentro_gourmet.entities.Empleado;
import edu.unla.epicentro_gourmet.entities.UnidadVenta;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.io.Serializable;
import java.util.List;
import java.util.Optional;

@Repository("empleadoRepository")
public interface IEmpleadoRepository extends JpaRepository<Empleado, Serializable> {

    public abstract List<Empleado> findByUnidadAndActivoTrue(UnidadVenta unidadVenta);

    @Query("SELECT e FROM Empleado e JOIN FETCH e.usuario WHERE e.unidad = :unidadVenta AND e.id = :id")
    public abstract Optional<Empleado> findByUnidadAndIdWithUsuario(@Param("unidadVenta") UnidadVenta unidadVenta, @Param("id") long id);

    public abstract Optional<Empleado> findByDni(long dni);

    public abstract Optional<Empleado> findByIdAndUnidad(long id, UnidadVenta unidadVenta);

    public abstract Optional<Empleado> findById(int id);
}
