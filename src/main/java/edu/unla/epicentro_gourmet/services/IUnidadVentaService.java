package edu.unla.epicentro_gourmet.services;

import edu.unla.epicentro_gourmet.entities.UnidadVenta;
import java.util.List;

public interface IUnidadVentaService {

    public List<UnidadVenta> getAll();

    public UnidadVenta findById(long id);

    public UnidadVenta save(UnidadVenta unidadVenta);

    public void delete(long id);

    public UnidadVenta getUnidadVentaFromResponsableEmail(String email);

}
