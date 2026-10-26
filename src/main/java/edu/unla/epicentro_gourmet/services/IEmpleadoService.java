package edu.unla.epicentro_gourmet.services;

import edu.unla.epicentro_gourmet.entities.Empleado;
import java.util.List;

public interface IEmpleadoService {

    public List<Empleado> getAll();

    public Empleado save(Empleado empleado);

    public Empleado findById(long id);

    public void logicalDelete(long id);
}
