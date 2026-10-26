package edu.unla.epicentro_gourmet.repositories;

import edu.unla.epicentro_gourmet.entities.UnidadVenta;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.io.Serializable;

@Repository("unidadVentaRepository")
public interface IUnidadVentaRepository extends JpaRepository<UnidadVenta, Serializable> {
}
