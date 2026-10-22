package edu.unla.epicentro_gourmet.repositories;

import edu.unla.epicentro_gourmet.entities.UnidadVenta;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository("unidadVentaRepositorio")
public interface IUnidadVentaRepositorio extends JpaRepository<UnidadVenta, Long> {
}