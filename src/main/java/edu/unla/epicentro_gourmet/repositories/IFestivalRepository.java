package edu.unla.epicentro_gourmet.repositories;

import edu.unla.epicentro_gourmet.entities.Festival;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.io.Serializable;
import java.util.List;

@Repository("festivalRepository")
public interface IFestivalRepository extends JpaRepository<Festival, Serializable> {

    @Query("SELECT f FROM Festival f WHERE f.fechaFin > CURRENT_DATE ORDER BY f.fechaInicio ASC")
    List<Festival> findFestivalesNoConcluidos();
}
