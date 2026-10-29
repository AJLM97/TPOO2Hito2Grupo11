package edu.unla.epicentro_gourmet.services.implementation;

import edu.unla.epicentro_gourmet.dtos.LiquidacionHaberesDTO;
import edu.unla.epicentro_gourmet.entities.Cajero;
import edu.unla.epicentro_gourmet.entities.Cocinero;
import edu.unla.epicentro_gourmet.entities.Costos;
import edu.unla.epicentro_gourmet.entities.Empleado;
import edu.unla.epicentro_gourmet.entities.EnumCategoriaCocinero;
import edu.unla.epicentro_gourmet.entities.Festival;
import edu.unla.epicentro_gourmet.entities.UnidadVenta;
import edu.unla.epicentro_gourmet.repositories.IEmpleadoRepository;
import edu.unla.epicentro_gourmet.services.ILiquidacionService;
import org.hibernate.Hibernate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Period;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Optional;

@Service("liquidacionService")
public class LiquidacionService implements ILiquidacionService {

    // Tramos de la consigna: hasta 15 días paga 50%, de 16 a 30 el 100% y, pasados los 30, (días / 15 + 1) * 50%.
    private static final long DIAS_POR_TRAMO = 15;
    private static final long DIAS_TRAMO_COMPLETO = 30;
    private static final double PORCENTAJE_POR_TRAMO = 0.5;

    private final IEmpleadoRepository empleadoRepository;

    public LiquidacionService(IEmpleadoRepository empleadoRepository) {
        this.empleadoRepository = empleadoRepository;
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<List<LiquidacionHaberesDTO>> liquidarUnidad(UnidadVenta unidad) {
        UnidadVenta real = (UnidadVenta) Hibernate.unproxy(unidad);
        Festival festival = (Festival) Hibernate.unproxy(real.getFestival());
        Costos costos = costosDe(festival);
        if (costos == null) {
            return Optional.empty();
        }
        return Optional.of(empleadoRepository.findByUnidadAndActivoTrue(real).stream()
                .map(empleado -> liquidar(empleado, festival, costos))
                .toList());
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<LiquidacionHaberesDTO> liquidar(Empleado empleado) {
        Empleado real = (Empleado) Hibernate.unproxy(empleado);
        UnidadVenta unidad = (UnidadVenta) Hibernate.unproxy(real.getUnidad());
        Festival festival = unidad == null ? null : (Festival) Hibernate.unproxy(unidad.getFestival());
        Costos costos = costosDe(festival);
        if (costos == null) {
            return Optional.empty();
        }
        return Optional.of(liquidar(real, festival, costos));
    }

    private LiquidacionHaberesDTO liquidar(Empleado empleado, Festival festival, Costos costos) {
        Empleado real = (Empleado) Hibernate.unproxy(empleado);
        String rol;
        String concepto;
        double adicional;
        if (real instanceof Cocinero cocinero) {
            rol = rolDe(cocinero.getCategoria());
            concepto = "Plus por categoría";
            adicional = plusDe(cocinero.getCategoria(), costos);
        } else if (real instanceof Cajero) {
            long anios = aniosDeAntiguedad(real, festival);
            rol = "Cajero";
            concepto = "Antigüedad (" + anios + (anios == 1 ? " año)" : " años)");
            adicional = anios * costos.getPlusAntiguedad();
        } else {
            throw new IllegalArgumentException("El tipo de empleado no tiene liquidación definida");
        }

        long dias = diasDelFestival(festival);
        double porcentaje = porcentajeSegunDuracion(dias);
        double sueldoBase = costos.getSueldoBase();
        double total = porcentaje * (sueldoBase + adicional);
        return new LiquidacionHaberesDTO(real.getId(), real.getNombre() + " " + real.getApellido(), rol,
                sueldoBase, concepto, adicional, dias, porcentaje, total);
    }

    private Costos costosDe(Festival festival) {
        return festival == null ? null : (Costos) Hibernate.unproxy(festival.getCosto());
    }

    // Antigüedad en años completos hasta el inicio del festival.
    private long aniosDeAntiguedad(Empleado empleado, Festival festival) {
        return Math.max(0, Period.between(empleado.getFechaIngreso(), festival.getFechInicio()).getYears());
    }

    // Los días de inicio y de fin cuentan como días del festival.
    private long diasDelFestival(Festival festival) {
        return ChronoUnit.DAYS.between(festival.getFechInicio(), festival.getFechaFin()) + 1;
    }

    private double porcentajeSegunDuracion(long dias) {
        if (dias <= DIAS_POR_TRAMO) {
            return PORCENTAJE_POR_TRAMO;
        }
        if (dias <= DIAS_TRAMO_COMPLETO) {
            return 1.0;
        }
        return (dias / DIAS_POR_TRAMO + 1) * PORCENTAJE_POR_TRAMO;
    }

    private double plusDe(EnumCategoriaCocinero categoria, Costos costos) {
        return switch (categoria) {
            case COCINERO -> costos.getPlusCocinero();
            case COCINERO_AYUDANTE -> costos.getPlusAyudante();
            case LAVAPLATOS -> costos.getPlusLavaplatos();
        };
    }

    private String rolDe(EnumCategoriaCocinero categoria) {
        return switch (categoria) {
            case COCINERO -> "Cocinero";
            case COCINERO_AYUDANTE -> "Cocinero ayudante";
            case LAVAPLATOS -> "Lavaplatos";
        };
    }
}
