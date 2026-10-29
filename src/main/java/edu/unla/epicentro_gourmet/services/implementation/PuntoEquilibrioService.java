package edu.unla.epicentro_gourmet.services.implementation;

import edu.unla.epicentro_gourmet.dtos.PuntoEquilibrioDTO;
import edu.unla.epicentro_gourmet.dtos.ReporteRentabilidadDTO;
import edu.unla.epicentro_gourmet.entities.Festival;
import edu.unla.epicentro_gourmet.entities.UnidadVenta;
import edu.unla.epicentro_gourmet.services.EnumEstadoFestival;
import edu.unla.epicentro_gourmet.services.IPuntoEquilibrioService;
import edu.unla.epicentro_gourmet.services.IRentabilidadService;
import org.hibernate.Hibernate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.Optional;

@Service("puntoEquilibrioService")
public class PuntoEquilibrioService implements IPuntoEquilibrioService {

    private final IRentabilidadService rentabilidadService;

    public PuntoEquilibrioService(IRentabilidadService rentabilidadService) {
        this.rentabilidadService = rentabilidadService;
    }

    // Los costos fijos (canon y sueldos) se cubren con la ganancia bruta: ventas menos el costo de los platos.
    @Override
    @Transactional(readOnly = true)
    public Optional<PuntoEquilibrioDTO> calcular(UnidadVenta unidad, LocalDate hoy) {
        UnidadVenta real = (UnidadVenta) Hibernate.unproxy(unidad);
        Festival festival = (Festival) Hibernate.unproxy(real.getFestival());
        return rentabilidadService.rentabilidad(real).map(r -> armar(r, festival, hoy));
    }

    private PuntoEquilibrioDTO armar(ReporteRentabilidadDTO r, Festival festival, LocalDate hoy) {
        double costosFijos = r.getCanon() + r.getSueldos();
        double gananciaBruta = r.getGananciaBruta();
        double porcentaje = costosFijos == 0 ? 100 : Math.max(0, gananciaBruta / costosFijos * 100);
        boolean alcanzado = gananciaBruta >= costosFijos;

        // Con el margen actual (ganancia bruta / ventas) se estima cuánto habría que vender en total.
        Double ventasNecesarias = null;
        Double ventasFaltantes = null;
        if (r.getIngresos() > 0 && gananciaBruta > 0) {
            ventasNecesarias = costosFijos / (gananciaBruta / r.getIngresos());
            ventasFaltantes = Math.max(0, ventasNecesarias - r.getIngresos());
        }

        return new PuntoEquilibrioDTO(r.getNombreFestival(), r.getNombreUnidad(), r.getCodigoUnidad(),
                estadoDe(festival, hoy), r.getCanon(), r.getSueldos(), costosFijos, r.getIngresos(),
                r.getCostoPlatos(), gananciaBruta, porcentaje, alcanzado, Math.max(0, costosFijos - gananciaBruta),
                ventasNecesarias, ventasFaltantes);
    }

    private EnumEstadoFestival estadoDe(Festival festival, LocalDate hoy) {
        if (hoy.isBefore(festival.getFechaInicio())) {
            return EnumEstadoFestival.PROXIMO;
        }
        if (hoy.isAfter(festival.getFechaFin())) {
            return EnumEstadoFestival.FINALIZADO;
        }
        return EnumEstadoFestival.EN_CURSO;
    }
}
