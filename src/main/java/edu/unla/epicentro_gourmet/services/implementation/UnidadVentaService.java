package edu.unla.epicentro_gourmet.services.implementation;

import edu.unla.epicentro_gourmet.entities.EnumRolUsuario;
import edu.unla.epicentro_gourmet.entities.UnidadVenta;
import edu.unla.epicentro_gourmet.entities.Usuario;
import edu.unla.epicentro_gourmet.repositories.IUnidadVentaRepository;
import edu.unla.epicentro_gourmet.services.IUnidadVentaService;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service("unidadVentaService")
public class UnidadVentaService implements IUnidadVentaService {

    private final IUnidadVentaRepository unidadVentaRepository;
    private final PasswordEncoder passwordEncoder;
    private final UsuarioService usuarioService;

    @Autowired
    private JavaMailSender mailSender;

    public UnidadVentaService(IUnidadVentaRepository unidadVentaRepository, PasswordEncoder passwordEncoder, UsuarioService usuarioService) {
        this.unidadVentaRepository = unidadVentaRepository;
        this.passwordEncoder = passwordEncoder;
        this.usuarioService = usuarioService;
    }

    @Override
    public List<UnidadVenta> getAll() {
        return unidadVentaRepository.findAll();
    }

    @Override
    public Optional<UnidadVenta> findById(long id) {
        return unidadVentaRepository.findById(id);
    }

    @Override
    public UnidadVenta save(UnidadVenta unidadVenta) {
        return unidadVentaRepository.save(unidadVenta);
    }

    @Override
    public void delete(long id) {
        unidadVentaRepository.deleteById(id);
    }

    @Override
    public Optional<UnidadVenta> getUnidadVentaFromResponsableEmail(String email) {
        return unidadVentaRepository.findUnidadVentaFromResponsableEmail(email);
    }

    @Override
    public List<UnidadVenta> getUnidadesDeVentaFromFestival(long idFestival) {
        return unidadVentaRepository.findUnidadesDeVentaFromFestival(idFestival);
    }

    @Override
    public void altaConResponsable(UnidadVenta unidadVenta, String emailResponsable) {
        String passwordAleatoria = UUID.randomUUID().toString().substring(0, 8);

        Usuario usuarioEncontrado = usuarioService.findByEmail(emailResponsable);
        Usuario nuevoUsuario;

        if (usuarioEncontrado != null) {
            nuevoUsuario = usuarioEncontrado;
            nuevoUsuario.setActivo(true);
        } else {
            nuevoUsuario = new Usuario(
                emailResponsable,
                passwordEncoder.encode(passwordAleatoria),
                EnumRolUsuario.RESPONSABLE, true
            );
        }
        usuarioService.save(nuevoUsuario);
        unidadVenta.setResponsable(nuevoUsuario);

        enviarCorreoConCredenciales(emailResponsable, passwordAleatoria);
        save(unidadVenta);
    }

    @Override
    public Optional<UnidadVenta> findByIdWithResponsable(long id) {
        return unidadVentaRepository.findByIdWithResponsable(id);
    }

    @Override
    public void actualizarConResponsable(UnidadVenta unidadVenta, String emailResponsable) {
        String passwordAleatoria = UUID.randomUUID().toString().substring(0, 8);

        Usuario usuarioEncontrado = usuarioService.findByEmail(emailResponsable);
        Usuario nuevoUsuario;

        if (usuarioEncontrado != null) {
            nuevoUsuario = usuarioEncontrado;
            nuevoUsuario.setActivo(true);
        } else {
            nuevoUsuario = new Usuario(
                emailResponsable,
                passwordEncoder.encode(passwordAleatoria),
                EnumRolUsuario.RESPONSABLE, true
            );
        }
        Usuario viejoResponsable = unidadVenta.getResponsable();
        viejoResponsable.setActivo(false);
        usuarioService.save(viejoResponsable);
        usuarioService.save(nuevoUsuario);
        unidadVenta.setResponsable(nuevoUsuario);

        enviarCorreoConCredenciales(emailResponsable, passwordAleatoria);
        save(unidadVenta);
    }

    @Override
    public void logicalDelete(UnidadVenta unidadVenta) {
        Usuario responsable = unidadVenta.getResponsable();
        responsable.setActivo(false);
        usuarioService.save(responsable);
    }

    private void enviarCorreoConCredenciales(String emailResponsable, String password) {
        SimpleMailMessage mensaje = new SimpleMailMessage();
        mensaje.setTo(emailResponsable);
        mensaje.setSubject("Bienvenido a Epicentro Gourmet - Tus Credenciales");
        mensaje.setText("Hola responsable,\n\nTu cuenta de responsable de una unidad de venta ha sido creada con éxito.\n" +
                "Correo de acceso: " + emailResponsable + "\n" +
                "Contraseña temporal: " + password + "\n\nPor seguridad, recordá cambiarla en tu primer ingreso.");
        mailSender.send(mensaje);
    }
}
