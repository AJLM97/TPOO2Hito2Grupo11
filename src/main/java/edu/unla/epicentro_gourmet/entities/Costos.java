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

    @Column(name = "sueldo_base", nullable = false)
    private float sueldoBase;

    @Enumerated(EnumType.STRING)
    @Column(name = "aplicacion_costo_superficie")
    private EnumAplicacionCosto aplicacionCostoSuperficie = EnumAplicacionCosto.TODAS;

    @Enumerated(EnumType.STRING)
    @Column(name = "aplicacion_costo_montaje")
    private EnumAplicacionCosto aplicacionCostoMontaje = EnumAplicacionCosto.PUESTO_DESARMABLE;

    @Enumerated(EnumType.STRING)
    @Column(name = "aplicacion_plus_electricidad")
    private EnumAplicacionCosto aplicacionPlusElectricidad = EnumAplicacionCosto.FOOD_TRUCK;

    // Constructor parametrizado útil para las pre-cargas del Hito 2
    public Costos(float costoSuperficie, float costoMontaje, float plusElectricidad,
                  float plusLavaplatos, float plusAyudante, float plusCocinero, float plusAntiguedad) {
        this(costoSuperficie, costoMontaje, plusElectricidad, plusLavaplatos, plusAyudante,
                plusCocinero, plusAntiguedad, 0);
    }

    public Costos(float costoSuperficie, float costoMontaje, float plusElectricidad,
                  float plusLavaplatos, float plusAyudante, float plusCocinero,
                  float plusAntiguedad, float sueldoBase) {
        this.costoSuperficie = costoSuperficie;
        this.costoMontaje = costoMontaje;
        this.plusElectricidad = plusElectricidad;
        this.plusLavaplatos = plusLavaplatos;
        this.plusAyudante = plusAyudante;
        this.plusCocinero = plusCocinero;
        this.plusAntiguedad = plusAntiguedad;
        this.sueldoBase = sueldoBase;
    }

    public EnumAplicacionCosto getAplicacionCostoSuperficie() {
        return aplicacionCostoSuperficie != null ? aplicacionCostoSuperficie : EnumAplicacionCosto.TODAS;
    }

    public EnumAplicacionCosto getAplicacionCostoMontaje() {
        return aplicacionCostoMontaje != null ? aplicacionCostoMontaje : EnumAplicacionCosto.PUESTO_DESARMABLE;
    }

    public EnumAplicacionCosto getAplicacionPlusElectricidad() {
        return aplicacionPlusElectricidad != null ? aplicacionPlusElectricidad : EnumAplicacionCosto.FOOD_TRUCK;
    }

    public boolean aplicaCostoSuperficie(UnidadVenta unidad) {
        return aplicaA(getAplicacionCostoSuperficie(), unidad);
    }

    public boolean aplicaCostoMontaje(UnidadVenta unidad) {
        return aplicaA(getAplicacionCostoMontaje(), unidad);
    }

    public boolean aplicaPlusElectricidad(UnidadVenta unidad) {
        return unidad instanceof FoodTruck foodTruck
                && foodTruck.isConexionElectrica()
                && aplicaA(getAplicacionPlusElectricidad(), unidad);
    }

    private boolean aplicaA(EnumAplicacionCosto aplicacion, UnidadVenta unidad) {
        if (unidad == null) {
            return false;
        }
        return switch (aplicacion) {
            case TODAS -> true;
            case FOOD_TRUCK -> unidad instanceof FoodTruck;
            case PUESTO_DESARMABLE -> unidad instanceof PuestoDesarmable;
        };
    }
}
