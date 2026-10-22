package edu.unla.epicentro_gourmet.services;

import edu.unla.epicentro_gourmet.entities.Empleado;
import edu.unla.epicentro_gourmet.entities.EnumRolUsuario;
import edu.unla.epicentro_gourmet.entities.Usuario;
import edu.unla.epicentro_gourmet.repositories.IEmpleadoRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.Period;
import java.util.List;
import java.util.UUID;

@Service("empleadoService")
public class EmpleadoService {

    @Autowired
    @Qualifier("empleadoRepository")
    private IEmpleadoRepository empleadoRepository;

    @Autowired
    private JavaMailSender mailSender;

    public Empleado registrarEmpleado(Empleado nuevoEmpleado, String emailPersonalDelEmpleado) {
        int edad = Period.between(nuevoEmpleado.getFechaNacimiento(), LocalDate.now()).getYears();
        if (edad < 18) {
            throw new IllegalArgumentException("Error: El empleado debe ser mayor de edad (18 años).");
        }

        var empleadoExistente = empleadoRepository.encontrarPorDni(nuevoEmpleado.getDni());
        if (empleadoExistente.isPresent()) {
            Empleado e = empleadoExistente.get();
            if (!e.isActivo()) {
                e.setActivo(true);
                e.setUnidad(nuevoEmpleado.getUnidad());
                return empleadoRepository.save(e);
            } else {
                throw new IllegalArgumentException("Error: Ya existe un empleado activo con ese DNI.");
            }
        }

        String passwordAleatoria = UUID.randomUUID().toString().substring(0, 8);
        Usuario nuevoUsuario = new Usuario(
                nuevoEmpleado.getApellido().toLowerCase() + nuevoEmpleado.getDni() + "@epicentro.com",
                passwordAleatoria,
                EnumRolUsuario.EMPLEADO
        );
        nuevoEmpleado.setUsuario(nuevoUsuario);

        Empleado empleadoGuardado = empleadoRepository.save(nuevoEmpleado);

        enviarCorreoConCredenciales(emailPersonalDelEmpleado, nuevoUsuario.getEmail(), passwordAleatoria, nuevoEmpleado.getNombre());

        return empleadoGuardado;
    }

    public void eliminarEmpleadoLogico(long idEmpleado) {
        Empleado e = empleadoRepository.findById(idEmpleado)
                .orElseThrow(() -> new IllegalArgumentException("Error: No existe el empleado con ID: " + idEmpleado));
        e.setActivo(false);
        empleadoRepository.save(e);
    }

    public List<Empleado> listarEmpleadosActivosDeUnidad(long idUnidad) {
        return empleadoRepository.encontrarActivosPorUnidad(idUnidad);
    }

    private void enviarCorreoConCredenciales(String emailPersonal, String emailUsuario, String password, String nombre) {
        SimpleMailMessage mensaje = new SimpleMailMessage();
        mensaje.setTo(emailPersonal);
        mensaje.setSubject("Bienvenido a Epicentro Gourmet - Tus Credenciales");
        mensaje.setText("Hola " + nombre + ",\n\nTu cuenta de personal ha sido creada con éxito.\n" +
                "Correo de acceso: " + emailUsuario + "\n" +
                "Contraseña temporal: " + password + "\n\nPor seguridad, recordá cambiarla en tu primer ingreso.");
        mailSender.send(mensaje);
    }
}
