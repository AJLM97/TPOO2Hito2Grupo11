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

    @Column(nullable = false)
    private boolean activo;

    @OneToOne(mappedBy = "usuario", fetch = FetchType.LAZY)
    private Empleado empleado;

    @OneToOne(mappedBy = "responsable", fetch = FetchType.LAZY)
    private UnidadVenta unidadVenta;

    public Usuario(String email, String password, EnumRolUsuario rol, boolean activo) {
        this.email = email;
        this.password = password;
        this.rol = rol;
        this.activo = activo;
    }
}