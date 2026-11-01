package edu.unla.epicentro_gourmet.services;

import edu.unla.epicentro_gourmet.entities.UnidadVenta;
import java.util.List;
import java.util.Optional;

public interface IUnidadVentaService {

    public List<UnidadVenta> getAll();

    public Optional<UnidadVenta> findById(long id);

    public UnidadVenta save(UnidadVenta unidadVenta);

    public void delete(long id);

    public Optional<UnidadVenta> getUnidadVentaFromResponsableEmail(String email);

    public List<UnidadVenta> getUnidadesDeVentaFromFestival(long idFestival);

    public void altaConResponsable(UnidadVenta unidadVenta, String emailResponsable, String passwordResponsable);

    public Optional<UnidadVenta> findByIdWithResponsable(long id);

    public void actualizarConResponsable(UnidadVenta unidadVenta, String emailResponsable);

    public void logicalDelete(UnidadVenta unidadVenta);

}
