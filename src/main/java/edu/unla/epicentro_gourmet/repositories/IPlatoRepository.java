package edu.unla.epicentro_gourmet.repositories;

import edu.unla.epicentro_gourmet.entities.Plato;
import edu.unla.epicentro_gourmet.entities.UnidadVenta;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.io.Serializable;
import java.util.List;
import java.util.Optional;

@Repository("platoRepository")
public interface IPlatoRepository extends JpaRepository<Plato, Serializable> {

    public abstract List<Plato> findByUnidadAndActivoTrue(UnidadVenta unidadVenta);

    public abstract Optional<Plato> findByIdAndUnidadAndActivoTrue(Long id, UnidadVenta unidadVenta);

    public abstract List<Plato> findByUnidadAndNombreIgnoreCase(UnidadVenta unidadVenta, String nombre);
}
