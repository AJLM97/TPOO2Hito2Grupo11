package edu.unla.epicentro_gourmet.entities;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "platos")
@Getter
@Setter
@NoArgsConstructor
public class Plato {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String nombre;

    @Column(name = "precio_venta", nullable = false)
    private double precioVenta;

    @Column(name = "costo_produccion", nullable = false)
    private double costoProduccion;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_unidad_venta", nullable = false)
    private UnidadVenta unidad;

    public Plato(String nombre, double precioVenta, double costoProduccion, UnidadVenta unidad) {
        this.nombre = nombre;
        this.precioVenta = precioVenta;
        this.costoProduccion = costoProduccion;
        this.unidad = unidad;
    }
}
