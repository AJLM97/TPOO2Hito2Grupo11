package edu.unla.epicentro_gourmet.repositories;

import edu.unla.epicentro_gourmet.dtos.RankingConcurrenciaDTO;
import edu.unla.epicentro_gourmet.entities.EnumTemporadaFestival;
import edu.unla.epicentro_gourmet.entities.Festival;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.io.Serializable;
import java.util.List;

@Repository("festivalRepository")
public interface IFestivalRepository extends JpaRepository<Festival, Serializable> {

    @Query("SELECT f FROM Festival f WHERE f.fechaFin > CURRENT_DATE ORDER BY f.fechaInicio ASC")
    List<Festival> findFestivalesNoConcluidos();

    @Query("""
            SELECT new edu.unla.epicentro_gourmet.dtos.RankingConcurrenciaDTO(
                f.nombre, f.temporada, f.fechaInicio, COUNT(DISTINCT pedido.id))
            FROM Festival f
            LEFT JOIN f.unidades uv
            LEFT JOIN uv.pedidos pedido ON pedido.cerrado = true
            WHERE f.temporada = :temporada
            GROUP BY f.id, f.nombre, f.temporada, f.fechaInicio
            ORDER BY COUNT(DISTINCT pedido.id) DESC, f.fechaInicio DESC, f.nombre ASC
            """)
    List<RankingConcurrenciaDTO> findRankingConcurrenciaPorTemporada(
            @Param("temporada") EnumTemporadaFestival temporada);
}
