package edu.unla.epicentro_gourmet.repositories;

import edu.unla.epicentro_gourmet.entities.UnidadVenta;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository("unidadVentaRepositorio")
public interface IUnidadVentaRepositorio extends JpaRepository<UnidadVenta, Long> {
	List<UnidadVenta> findByFestival_Id(Long festivalId);
}