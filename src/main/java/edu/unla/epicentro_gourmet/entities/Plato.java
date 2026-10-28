package edu.unla.epicentro_gourmet.entities;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.ColumnDefault;

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

    // El default en la base evita ocultar los platos ya cargados al agregar la columna.
    @Column(nullable = false)
    @ColumnDefault("true")
    private boolean activo = true;

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
