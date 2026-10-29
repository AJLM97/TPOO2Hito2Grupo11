package edu.unla.epicentro_gourmet.services.implementation;

import edu.unla.epicentro_gourmet.entities.EnumRolUsuario;
import edu.unla.epicentro_gourmet.repositories.IUsuarioRepository;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Service("usuarioService")
public class UsuarioService implements UserDetailsService {

    private IUsuarioRepository usuarioRepository;

    public UsuarioService(IUsuarioRepository usuarioRepository) {
        this.usuarioRepository = usuarioRepository;
    }

    @Override
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
        edu.unla.epicentro_gourmet.entities.Usuario usuario = usuarioRepository.findByEmail(email);
        if (usuario == null) {
            throw new UsernameNotFoundException("Usuario no encontrado con el email: " + email);
        }
        return buildUsuario(usuario, buildGrantedAuthority(usuario.getRol()));
    }

    private User buildUsuario(edu.unla.epicentro_gourmet.entities.Usuario usuario, List<GrantedAuthority> grantedAuthorities) {
        return new User(usuario.getEmail(), usuario.getPassword(), usuario.isActivo(),
                true, true, true,
                grantedAuthorities);
    }

    private List<GrantedAuthority> buildGrantedAuthority(EnumRolUsuario rolUsuario) {
        Set<GrantedAuthority> grantedAuthorities = new HashSet<>();
        if (rolUsuario != null) {
            grantedAuthorities.add(new SimpleGrantedAuthority(rolUsuario.name()));
        }
        return new ArrayList<>(grantedAuthorities);
    }

    public edu.unla.epicentro_gourmet.entities.Usuario findByEmail(String email) {
        return usuarioRepository.findByEmail(email);
    }

    public void save(edu.unla.epicentro_gourmet.entities.Usuario usuario) {
        usuarioRepository.save(usuario);
    }
}