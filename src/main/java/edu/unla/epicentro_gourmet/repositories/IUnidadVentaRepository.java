package edu.unla.epicentro_gourmet.repositories;

import edu.unla.epicentro_gourmet.entities.UnidadVenta;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.io.Serializable;
import java.util.List;
import java.util.Optional;

@Repository("unidadVentaRepository")
public interface IUnidadVentaRepository extends JpaRepository<UnidadVenta, Serializable> {

    @Query("SELECT uv FROM UnidadVenta uv JOIN uv.responsable u WHERE u.email = :email AND u.activo = true")
    public abstract Optional<UnidadVenta> findUnidadVentaFromResponsableEmail(@Param("email") String email);

    List<UnidadVenta> findByFestival_Id(Long festivalId);

    @Query("SELECT uv FROM UnidadVenta uv JOIN uv.festival f JOIN uv.responsable r WHERE f.id = :idFestival AND r.activo = true")
    List<UnidadVenta> findUnidadesDeVentaFromFestival(@Param("idFestival") long idFestival);

    Optional<UnidadVenta> findById(Long id);

    @Query("SELECT uv FROM UnidadVenta uv JOIN FETCH uv.responsable u WHERE uv.id = :id AND u.activo = true")
    Optional<UnidadVenta> findByIdWithResponsable(@Param("id") Long id);
}
