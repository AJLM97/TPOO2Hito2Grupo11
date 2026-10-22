package edu.unla.epicentro_gourmet.repositories;

import edu.unla.epicentro_gourmet.entities.Empleado;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.io.Serializable;
import java.util.List;
import java.util.Optional;

@Repository("empleadoRepository")
public interface IEmpleadoRepository extends JpaRepository<Empleado, Serializable> {

    @Query("SELECT e FROM Empleado e WHERE e.unidad.id = :idUnidad AND e.activo = true")
    public abstract List<Empleado> encontrarActivosPorUnidad(@Param("idUnidad") long idUnidad);

    @Query("SELECT e FROM Empleado e WHERE e.id = :idEmpleado")
    public abstract Optional<Empleado> encontrarPorId(@Param("idEmpleado") long idEmpleado);

    @Query("SELECT e FROM Empleado e WHERE e.dni = :dni")
    public abstract Optional<Empleado> encontrarPorDni(@Param("dni") long dni);

}
