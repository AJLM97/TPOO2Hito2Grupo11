package edu.unla.epicentro_gourmet.entities;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDate;

@Entity
@Table(name = "cocineros")
@Getter
@Setter
@NoArgsConstructor
public class Cocinero extends Empleado{
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private EnumCategoriaCocinero categoria;

    public Cocinero(String nombre, String apellido, long dni, LocalDate fechaNacimiento, LocalDate fechaIngreso,
                    double sueldoBase, UnidadVenta unidad, EnumCategoriaCocinero categoria, Usuario usuario) {
        super(nombre, apellido, dni, fechaNacimiento, fechaIngreso, sueldoBase, unidad, usuario);
        this.categoria = categoria;
    }
}
