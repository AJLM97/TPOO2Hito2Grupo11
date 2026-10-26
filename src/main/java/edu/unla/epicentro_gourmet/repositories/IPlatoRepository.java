package edu.unla.epicentro_gourmet.repositories;

import edu.unla.epicentro_gourmet.entities.Plato;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.io.Serializable;

@Repository("platoRepository")
public interface IPlatoRepository extends JpaRepository<Plato, Serializable> {
}
