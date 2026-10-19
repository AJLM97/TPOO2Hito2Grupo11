package edu.unla.epicentro_gourmet.entities;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "usuarios")
@Getter
@Setter
@NoArgsConstructor
public class Usuario {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long id;

    @Column(nullable = false, unique = true)
    private String email;

    @Column(name = "password", nullable = false)
    private String password;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private EnumRolUsuario rol;

    @Column(name = "baja_logica", nullable = false)
    private boolean bajaLogica = false;

    public Usuario(String email, String password, EnumRolUsuario rol) {
        this.email = email;
        this.password = password;
        this.rol = rol;
        this.bajaLogica = false;
    }
}