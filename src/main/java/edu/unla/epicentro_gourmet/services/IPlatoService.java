package edu.unla.epicentro_gourmet.services;

import edu.unla.epicentro_gourmet.entities.Plato;
import java.util.List;

public interface IPlatoService {

    public List<Plato> getAll();

    public Plato save(Plato plato);

    public Plato findById(long id);

    public void delete(long id);
}
