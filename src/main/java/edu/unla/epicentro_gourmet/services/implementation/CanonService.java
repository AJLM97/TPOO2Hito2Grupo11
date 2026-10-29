package edu.unla.epicentro_gourmet.services.implementation;

import edu.unla.epicentro_gourmet.dtos.DetalleCanonDTO;
import edu.unla.epicentro_gourmet.entities.Costos;
import edu.unla.epicentro_gourmet.entities.Festival;
import edu.unla.epicentro_gourmet.entities.FoodTruck;
import edu.unla.epicentro_gourmet.entities.PuestoDesarmable;
import edu.unla.epicentro_gourmet.entities.UnidadVenta;
import edu.unla.epicentro_gourmet.services.ICanonService;
import org.hibernate.Hibernate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Service("canonService")
public class CanonService implements ICanonService {

    // Canon = superficie * costo por m2 + plus de electricidad - tiempo de montaje * costo por minuto.
    // Cada concepto toma su valor y su alcance (tipo de unidad) de los costos del festival.
    @Override
    @Transactional(readOnly = true)
    public Optional<DetalleCanonDTO> calcular(UnidadVenta unidad) {
        // Con instanceof sobre un proxy lazy no se distingue el tipo de unidad.
        UnidadVenta real = (UnidadVenta) Hibernate.unproxy(unidad);
        Festival festival = (Festival) Hibernate.unproxy(real.getFestival());
        Costos costos = festival == null ? null : (Costos) Hibernate.unproxy(festival.getCosto());
        if (costos == null) {
            return Optional.empty();
        }

        double importeSuperficie = costos.aplicaCostoSuperficie(real)
                ? real.getSuperficie() * costos.getCostoSuperficie() : 0;
        double plusElectricidad = costos.aplicaPlusElectricidad(real) ? costos.getPlusElectricidad() : 0;

        double tiempoMontaje = 0;
        double descuentoMontaje = 0;
        if (real instanceof PuestoDesarmable puesto && costos.aplicaCostoMontaje(real)) {
            tiempoMontaje = puesto.getTiempoMontajeMinutos();
            descuentoMontaje = tiempoMontaje * costos.getCostoMontaje();
        }

        double canon = Math.max(0, importeSuperficie + plusElectricidad - descuentoMontaje);
        return Optional.of(new DetalleCanonDTO(
                festival.getNombre(), real.getNombreComercial(), real.getCodigo(), tipoDe(real),
                real.getSuperficie(), costos.getCostoSuperficie(), importeSuperficie, plusElectricidad,
                tiempoMontaje, costos.getCostoMontaje(), descuentoMontaje, canon));
    }

    private String tipoDe(UnidadVenta unidad) {
        return unidad instanceof FoodTruck ? "Food Truck" : "Puesto Desarmable";
    }
}
