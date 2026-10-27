package edu.unla.epicentro_gourmet.entities;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDate;

@Entity
@Table(name = "empleados")
@Inheritance(strategy = InheritanceType.JOINED)
@Getter
@Setter
@NoArgsConstructor
public abstract class Empleado {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    protected long id;

    @Column(nullable = false)
    protected String nombre;

    @Column(nullable = false)
    protected String apellido;

    @Column(nullable = false, unique = true)
    protected long dni;

    @Column(name = "fecha_nacimiento", nullable = false)
    protected LocalDate fechaNacimiento;

    @Column(name = "fecha_ingreso", nullable = false)
    protected LocalDate fechaIngreso;

    @Column(name = "sueldo_base", nullable = false)
    protected double sueldoBase;

    @Column(nullable = false)
    protected boolean activo = true;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_unidad_venta")
    protected UnidadVenta unidad;

    @OneToOne(cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @JoinColumn(name = "id_usuario", unique = true)
    protected Usuario usuario;

    public Empleado(String nombre, String apellido, long dni, LocalDate fechaNacimiento,
                    LocalDate fechaIngreso, double sueldoBase, UnidadVenta unidad, Usuario usuario) {
        this.nombre = nombre;
        this.apellido = apellido;
        this.dni = dni;
        this.fechaNacimiento = fechaNacimiento;
        this.fechaIngreso = fechaIngreso;
        this.sueldoBase = sueldoBase;
        this.unidad = unidad;
        this.usuario = usuario;
        this.activo = true;
    }
}
