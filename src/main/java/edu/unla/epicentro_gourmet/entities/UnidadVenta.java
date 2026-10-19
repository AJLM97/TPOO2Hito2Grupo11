package edu.unla.epicentro_gourmet.entities;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.HashSet;
import java.util.Set;

@Entity
@Table(name = "unidades_venta")
@Inheritance(strategy = InheritanceType.JOINED)
@Getter
@Setter
@NoArgsConstructor
public abstract class UnidadVenta {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "nombre_comercial", nullable = false)
    protected String nombreComercial;

    @Column(nullable = false)
    protected double superficie;

    @JoinColumn(name = "id_responsable_usuario", unique = true)
    protected Usuario responsable;

    @OneToMany(mappedBy = "unidad", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    protected Set<Empleado> staff = new HashSet<>();

    @Column(unique = true, length = 10, nullable = false)
    private String codigo;

    @OneToMany(mappedBy = "unidad", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    protected Set<Plato> platos = new HashSet<>();

    @OneToMany(mappedBy = "unidadVenta", cascade = CascadeType.ALL)
    private Set<Pedido> pedidos = new HashSet<>();

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "festival_id", nullable = false)
    private Festival festival;

    public UnidadVenta(String nombreComercial, String codigo, double superficie, Festival festival, Usuario responsable) {
        this.nombreComercial = nombreComercial;
        this.codigo = codigo;
        this.superficie = superficie;
        this.festival = festival;
        this.responsable = responsable;
    }
}
