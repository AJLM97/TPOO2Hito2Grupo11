package edu.unla.epicentro_gourmet.service;

import edu.unla.epicentro_gourmet.dtos.EmpleadoDTO;
import edu.unla.epicentro_gourmet.entities.Cajero;
import edu.unla.epicentro_gourmet.entities.Cocinero;
import edu.unla.epicentro_gourmet.entities.Empleado;
import edu.unla.epicentro_gourmet.repository.EmpleadoRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class EmpleadoService {

    @Autowired
    private EmpleadoRepository empleadoRepository;

    public List<Empleado> buscarEmpleadosConFiltro(EmpleadoFiltroDTO filtro) {
        String nombre = (filtro.getNombre() != null && !filtro.getNombre().isBlank()) ? filtro.getNombre() : null;
        String apellido = (filtro.getApellido() != null && !filtro.getApellido().isBlank()) ? filtro.getApellido() : null;

        // Traer empleados activos desde BD
        List<Empleado> resultado = empleadoRepository.filtrarEmpleadosActivos(
                filtro.getIdUnidadVenta(),
                nombre,
                apellido,
                filtro.getDni()
        );

        if (filtro.getTipoEmpleado() != null && !filtro.getTipoEmpleado().isBlank()) {
            if ("CAJERO".equalsIgnoreCase(filtro.getTipoEmpleado())) {
                resultado = resultado.stream()
                        .filter(e -> e instanceof Cajero)
                        .collect(Collectors.toList());
            } else if ("COCINERO".equalsIgnoreCase(filtro.getTipoEmpleado())) {
                resultado = resultado.stream()
                        .filter(e -> e instanceof Cocinero)
                        .collect(Collectors.toList());
            }
        }

        return resultado;
    }
}
