package edu.unla.epicentro_gourmet.services;

import edu.unla.epicentro_gourmet.entities.Empleado;
import edu.unla.epicentro_gourmet.entities.UnidadVenta;

import java.util.List;
import java.util.Optional;

public interface IEmpleadoService {

    public List<Empleado> getAll();

    public Empleado save(Empleado empleado);

    public Optional<Empleado> findById(long id);

    public void logicalDelete(UnidadVenta unidadVenta, long id);

    public Empleado newEmpleado(Empleado empleado, String emailPersonal);

    public List<Empleado> findByUnidadVenta(UnidadVenta unidadVenta);

    public Optional<Empleado> findByDni(long dni);

    public Optional<Empleado> findActivoByEmail(String email);

    public Optional<Empleado> findByIdAndUnidadVenta(long id, UnidadVenta unidadVenta);

    EnumResultadoAltaEmpleado altaOReactivar(Empleado candidato, String emailPersonal);
}
