package edu.unla.epicentro_gourmet.repositories;

import edu.unla.epicentro_gourmet.entities.Festival;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository("festivalRepositorio")
public interface IFestivalRepositorio extends JpaRepository<Festival, Long> {
}