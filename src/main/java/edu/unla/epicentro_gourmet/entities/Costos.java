package edu.unla.epicentro_gourmet.entities;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "costos")
@Getter
@Setter
@NoArgsConstructor
public class Costos {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long id;

    @Column(name = "costo_superficie", nullable = false)
    private float costoSuperficie;

    @Column(name = "costo_montaje", nullable = false)
    private float costoMontaje;

    @Column(name = "plus_electricidad", nullable = false)
    private float plusElectricidad;

    @Column(name = "plus_lavaplatos", nullable = false)
    private float plusLavaplatos;

    @Column(name = "plus_ayudante", nullable = false)
    private float plusAyudante;

    @Column(name = "plus_cocinero", nullable = false)
    private float plusCocinero;

    @Column(name = "plus_antiguedad", nullable = false)
    private float plusAntiguedad;

    // Constructor parametrizado útil para las pre-cargas del Hito 2
    public Costos(float costoSuperficie, float costoMontaje, float plusElectricidad,
                  float plusLavaplatos, float plusAyudante, float plusCocinero, float plusAntiguedad) {
        this.costoSuperficie = costoSuperficie;
        this.costoMontaje = costoMontaje;
        this.plusElectricidad = plusElectricidad;
        this.plusLavaplatos = plusLavaplatos;
        this.plusAyudante = plusAyudante;
        this.plusCocinero = plusCocinero;
        this.plusAntiguedad = plusAntiguedad;
    }
}
