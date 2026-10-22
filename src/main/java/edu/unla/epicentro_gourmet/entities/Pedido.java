package edu.unla.epicentro_gourmet.entities;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;

@Entity
@Table(name = "pedidos")
@Getter
@Setter
@NoArgsConstructor
public class Pedido {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @JoinColumn(name = "fecha_transaccion", nullable = false)
    private LocalDateTime fechaTransaccion;

    @Column(nullable = false)
    private boolean cerrado;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "festival_id", nullable = false)
    private Festival festival;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "unidad_venta_id", nullable = false)
    private UnidadVenta unidadVenta;

    @OneToMany(mappedBy = "pedido", cascade = CascadeType.ALL, fetch = FetchType.LAZY, orphanRemoval = true)
    private Set<ItemPedido> items = new HashSet<>();

    public Pedido(LocalDateTime fechaTransaccion, UnidadVenta unidadVenta) {
        this.fechaTransaccion = fechaTransaccion;
        this.unidadVenta = unidadVenta;
        this.cerrado = false;
    }
}
