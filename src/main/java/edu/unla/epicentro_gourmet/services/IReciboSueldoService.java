package edu.unla.epicentro_gourmet.services;

import java.util.Optional;

public interface IReciboSueldoService {
    // Vacío si el empleado no existe, no tiene unidad o su festival no tiene costos configurados
    Optional<byte[]> generarRecibo(String emailEmpleado);
}
