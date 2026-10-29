package edu.unla.epicentro_gourmet.services.implementation;

import edu.unla.epicentro_gourmet.dtos.FiltroEmpleadoDTO;
import edu.unla.epicentro_gourmet.entities.*;
import edu.unla.epicentro_gourmet.repositories.IEmpleadoRepository;
import edu.unla.epicentro_gourmet.services.EnumResultadoAltaEmpleado;
import edu.unla.epicentro_gourmet.services.IEmpleadoService;
import jakarta.transaction.Transactional;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service("empleadoService")
public class EmpleadoService implements IEmpleadoService {

    private final IEmpleadoRepository empleadoRepository;
    private final PasswordEncoder passwordEncoder;

    @Autowired
    private JavaMailSender mailSender;

    public EmpleadoService(IEmpleadoRepository empleadoRepository, PasswordEncoder passwordEncoder) {
        this.empleadoRepository = empleadoRepository;
        this.passwordEncoder = passwordEncoder;
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
    public Optional<Empleado> findById(long id) {
        return empleadoRepository.findById(id);
    }

    public Optional<Empleado> findByIdAndUnidadVenta(long id, UnidadVenta unidadVenta) {
        return empleadoRepository.findByIdAndUnidad(id, unidadVenta);
    }

    @Override
    public void logicalDelete(UnidadVenta unidadVenta, long id) {
        Empleado empleado = empleadoRepository.findByUnidadAndIdWithUsuario(unidadVenta, id).orElse(null);;
        if (empleado != null) {
            empleado.setActivo(false);
            empleado.getUsuario().setActivo(false);
            empleadoRepository.save(empleado);
        }
    }

    @Override
    public Empleado newEmpleado(Empleado empleado, String emailPersonal) {
        String passwordAleatoria = UUID.randomUUID().toString().substring(0, 8);
        Usuario nuevoUsuario = new Usuario(
                empleado.getDni() + "@epicentro.com",
                passwordEncoder.encode(passwordAleatoria),
                EnumRolUsuario.EMPLEADO, true
        );
        empleado.setUsuario(nuevoUsuario);

        enviarCorreoConCredenciales(emailPersonal, nuevoUsuario.getEmail(), passwordAleatoria, empleado.getNombre());
        return empleadoRepository.save(empleado);
    }

    @Override
    public List<Empleado> findByUnidadVenta(UnidadVenta unidadVenta) {
        return empleadoRepository.findByUnidadAndActivoTrue(unidadVenta);
    }

    @Override
    public Optional<Empleado> findByDni(long dni) {
        return empleadoRepository.findByDni(dni);
    }

    @Override
    public Optional<Empleado> findActivoByEmail(String email) {
        return empleadoRepository.findByUsuario_EmailAndActivoTrue(email);
    }

    @Override
    @Transactional
    public EnumResultadoAltaEmpleado altaOReactivar(
            Empleado empleado, String emailPersonal) {

        Optional<Empleado> encontrado = empleadoRepository.findByDni(empleado.getDni());

        if (encontrado.isEmpty()) {
            newEmpleado(empleado, emailPersonal);
            return EnumResultadoAltaEmpleado.CREADO;
        }

        Empleado existente = encontrado.get();

        if (existente.isActivo()) {
            return EnumResultadoAltaEmpleado.DNI_YA_ACTIVO;
        }

        return reactivarEmpleado(existente, empleado, emailPersonal);
    }

    private EnumResultadoAltaEmpleado reactivarEmpleado(Empleado empleadoExistente, Empleado empleadoActualizado, String emailPersonal) {
        if (empleadoExistente instanceof Cajero cajeroExistente && empleadoActualizado instanceof Cajero cajeroCandidato) {
            cajeroExistente.setTurnoTrabajo(cajeroCandidato.getTurnoTrabajo());
        } else if (empleadoExistente instanceof Cocinero cocineroExistente  && empleadoActualizado instanceof Cocinero cocineroCandidato) {
            cocineroExistente.setCategoria(cocineroCandidato.getCategoria());
        } else {
            return EnumResultadoAltaEmpleado.TIPO_DISTINTO;
        }

        empleadoExistente.setNombre(empleadoActualizado.getNombre());
        empleadoExistente.setApellido(empleadoActualizado.getApellido());
        empleadoExistente.setFechaNacimiento(empleadoActualizado.getFechaNacimiento());
        empleadoExistente.setFechaIngreso(LocalDate.now());
        empleadoExistente.setSueldoBase(empleadoActualizado.getSueldoBase());
        empleadoExistente.setUnidad(empleadoActualizado.getUnidad());
        empleadoExistente.setActivo(true);

        if (empleadoExistente.getUsuario() != null) {
            empleadoExistente.getUsuario().setActivo(true);
        }

        String passwordAleatoria = UUID.randomUUID().toString().substring(0, 8);
        Usuario usuario = empleadoExistente.getUsuario();
        usuario.setActivo(true);
        usuario.setPassword(passwordEncoder.encode(passwordAleatoria));

        enviarCorreoConCredencialesReactivadas(emailPersonal, usuario.getEmail(), passwordAleatoria, empleadoExistente.getNombre());
        empleadoRepository.save(empleadoExistente);

        return EnumResultadoAltaEmpleado.REACTIVADO;
    }

    private void enviarCorreoConCredencialesReactivadas(String emailPersonal, String emailUsuario, String password, String nombre) {
        SimpleMailMessage mensaje = new SimpleMailMessage();
        mensaje.setTo(emailPersonal);
        mensaje.setSubject("Bienvenido a Epicentro Gourmet - Tus Credenciales");
        mensaje.setText("Hola " + nombre + ",\n\nTu cuenta de staff ha sido reactivada con éxito.\n" +
                "Correo de acceso: " + emailUsuario + "\n" +
                "Contraseña temporal: " + password + "\n\nPor seguridad, recordá cambiarla en tu primer ingreso.");
        mailSender.send(mensaje);
    }

    private void enviarCorreoConCredenciales(String emailPersonal, String emailUsuario, String password, String nombre) {
        SimpleMailMessage mensaje = new SimpleMailMessage();
        mensaje.setTo(emailPersonal);
        mensaje.setSubject("Bienvenido a Epicentro Gourmet - Tus Credenciales");
        mensaje.setText("Hola " + nombre + ",\n\nTu cuenta de staff ha sido creada con éxito.\n" +
                "Correo de acceso: " + emailUsuario + "\n" +
                "Contraseña temporal: " + password + "\n\nPor seguridad, recordá cambiarla en tu primer ingreso.");
        mailSender.send(mensaje);
    }

    @Override
    public List<Empleado> filtrar(UnidadVenta unidadVenta, @Valid FiltroEmpleadoDTO f) {
        return empleadoRepository.findByUnidadAndActivoTrue(unidadVenta).stream()
                .filter(e -> contiene(e.getNombre(), f.getNombre()))
                .filter(e -> contiene(e.getApellido(), f.getApellido()))
                .filter(e -> f.getDni() == null || e.getDni() == f.getDni())
                .filter(e -> coincideTipo(e, f.getTipoEmpleado()))
                .filter(e -> f.getCategoriaCocinero() == null
                        || (e instanceof Cocinero c && c.getCategoria() == f.getCategoriaCocinero()))
                .filter(e -> f.getTurnoTrabajo() == null
                        || (e instanceof Cajero c && c.getTurnoTrabajo() == f.getTurnoTrabajo()))
                .filter(e -> f.getIngresoDesde() == null || !e.getFechaIngreso().isBefore(f.getIngresoDesde()))
                .filter(e -> f.getIngresoHasta() == null || !e.getFechaIngreso().isAfter(f.getIngresoHasta()))
                .toList();
    }

    private boolean contiene(String valor, String texto) {
        return texto == null || texto.isBlank()
                || valor.toLowerCase().contains(texto.trim().toLowerCase());
    }

    private boolean coincideTipo(Empleado e, String tipo) {
        if (tipo == null || tipo.isBlank()) return true;
        return switch (tipo.toUpperCase()) {
            case "COCINERO" -> e instanceof Cocinero;
            case "CAJERO" -> e instanceof Cajero;
            default -> true;
        };
    }
}
