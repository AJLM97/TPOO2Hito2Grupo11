package edu.unla.epicentro_gourmet.entities;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDate;

@Entity
@Table(name = "cajeros")
@Getter
@Setter
@NoArgsConstructor
public class Cajero extends Empleado{
    @Enumerated(EnumType.STRING)
    @Column(name = "turno_trabajo", nullable = false)
    private EnumTurnoCajero turnoTrabajo;

    public Cajero(String nombre, String apellido, long dni, LocalDate fechaNacimiento, LocalDate fechaIngreso,
                  double sueldoBase, UnidadVenta unidad, EnumTurnoCajero turnoTrabajo, Usuario usuario) {
        super(nombre, apellido, dni, fechaNacimiento, fechaIngreso, sueldoBase, unidad, usuario);
        this.turnoTrabajo = turnoTrabajo;
    }
}
