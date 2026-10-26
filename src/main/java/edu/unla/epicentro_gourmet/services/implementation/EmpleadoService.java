package edu.unla.epicentro_gourmet.services.implementation;

import edu.unla.epicentro_gourmet.entities.Empleado;
import edu.unla.epicentro_gourmet.repositories.IEmpleadoRepository;
import edu.unla.epicentro_gourmet.services.IEmpleadoService;
import org.springframework.stereotype.Service;

import java.util.List;

@Service("empleadoService")
public class EmpleadoService implements IEmpleadoService {

    private final IEmpleadoRepository empleadoRepository;

    public EmpleadoService(IEmpleadoRepository empleadoRepository) {
        this.empleadoRepository = empleadoRepository;
    }

    @Override
    public List<Empleado> getAll() {
        return empleadoRepository.findAll();
    }

    @Override
    public Empleado save(Empleado empleado) {
        return empleadoRepository.save(empleado);
    }

    @Override
    public Empleado findById(long id) {
        return empleadoRepository.findById(id).orElse(null);
    }

    @Override
    public void logicalDelete(long id) {
        Empleado empleado = findById(id);
        if (empleado != null) {
            empleado.setActivo(false);
            empleadoRepository.save(empleado);
        }
    }
}
