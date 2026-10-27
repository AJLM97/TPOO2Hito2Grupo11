package edu.unla.epicentro_gourmet.repositories;

import edu.unla.epicentro_gourmet.entities.UnidadVenta;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.io.Serializable;

@Repository("unidadVentaRepository")
public interface IUnidadVentaRepository extends JpaRepository<UnidadVenta, Serializable> {

    @Query("SELECT uv FROM UnidadVenta uv JOIN uv.responsable u WHERE u.email = :email")
    public abstract UnidadVenta findUnidadVentaFromResponsableEmail(@Param("email") String email);
}
