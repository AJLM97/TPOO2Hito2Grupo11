package edu.unla.epicentro_gourmet.services;

import edu.unla.epicentro_gourmet.entities.Plato;
import edu.unla.epicentro_gourmet.entities.UnidadVenta;

import java.util.List;
import java.util.Optional;

public interface IPlatoService {

    public List<Plato> getAll();

    public Plato save(Plato plato);

    public Plato findById(long id);

    public void delete(long id);

    public List<Plato> findByUnidadVenta(UnidadVenta unidadVenta);

    public Optional<Plato> findByIdAndUnidadVenta(long id, UnidadVenta unidadVenta);

    EnumResultadoGuardadoPlato altaOReactivar(Plato candidato);

    EnumResultadoGuardadoPlato actualizar(UnidadVenta unidadVenta, Plato datos);

    public void logicalDelete(UnidadVenta unidadVenta, long id);
}
