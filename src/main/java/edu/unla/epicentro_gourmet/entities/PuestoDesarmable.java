package edu.unla.epicentro_gourmet.entities;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "puestos_desarmables")
@Getter
@Setter
@NoArgsConstructor
public class PuestoDesarmable extends UnidadVenta {
    @Column(name = "cantidad_de_carpas")
    private long cantidadDeCarpas;
    @Column(name = "tiempo_montaje_minutos")
    private long tiempoMontajeMinutos;

    public PuestoDesarmable(String nombreComercial, String codigo, double superficie,
                            Festival festival, long cantidadDeCarpas, long tiempoMontajeMinutos, Usuario responsable) {
        super(nombreComercial, codigo, superficie, festival, responsable);
        this.cantidadDeCarpas = cantidadDeCarpas;
        this.tiempoMontajeMinutos = tiempoMontajeMinutos;
    }
}