package edu.unla.epicentro_gourmet.services;

import edu.unla.epicentro_gourmet.entities.Festival;
import java.util.List;

public interface IFestivalService {

    public List<Festival> getAll();

    public Festival findById(long id);

    public void delete(long id);
}
