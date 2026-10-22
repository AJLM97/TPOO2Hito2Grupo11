package edu.unla.epicentro_gourmet.entities;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "food_trucks")
@Getter
@Setter
@NoArgsConstructor
public class FoodTruck extends UnidadVenta {
    @Column(nullable = false)
    private String patente;
    @Column(name = "conexion_electrica", nullable = false)
    private boolean conexionElectrica;

    public FoodTruck(String nombreComercial, String codigo, double superficie, Festival festival,
                     String patente, boolean conexionElectrica, Usuario responsable) {
        super(nombreComercial, codigo, superficie, festival, responsable);
        this.patente = patente;
        this.conexionElectrica = conexionElectrica;
    }
}
