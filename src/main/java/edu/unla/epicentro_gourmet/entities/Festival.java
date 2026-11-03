package edu.unla.epicentro_gourmet.entities;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDate;
import java.util.HashSet;
import java.util.Set;

@Entity
@Table(name = "festivales")
@Getter
@Setter
@NoArgsConstructor
public class Festival {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String nombre;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private EnumTemporadaFestival temporada;

    @Column(nullable = false, name = "fecha_inicio")
    private LocalDate fechaInicio;

    @Column(nullable = false, name = "fecha_fin")
    private LocalDate fechaFin;

    @OneToMany(mappedBy = "festival", cascade = CascadeType.ALL)
    private Set<UnidadVenta> unidades = new HashSet<>();

    @OneToOne(cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @JoinColumn(name = "id_costos", unique = true)
    private Costos costo;

    public Festival(String nombre, EnumTemporadaFestival temporada, LocalDate fechaInicio, LocalDate fechaFin, Costos costos) {
        this.nombre = nombre;
        this.temporada = temporada;
        this.fechaInicio = fechaInicio;
        this.fechaFin = fechaFin;
        this.costo = costos;
    }
}
