package edu.unla.epicentro_gourmet.services.implementation;

import edu.unla.epicentro_gourmet.dtos.PosicionRankingDTO;
import edu.unla.epicentro_gourmet.dtos.RankingUnidadVentaDTO;
import edu.unla.epicentro_gourmet.entities.Festival;
import edu.unla.epicentro_gourmet.entities.UnidadVenta;
import edu.unla.epicentro_gourmet.services.IPosicionRankingService;
import edu.unla.epicentro_gourmet.services.IReporteVentaService;
import org.hibernate.Hibernate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.OptionalDouble;

@Service("posicionRankingService")
public class PosicionRankingService implements IPosicionRankingService {

    // Dos recaudaciones que difieren en menos de medio centavo se consideran iguales.
    private static final double TOLERANCIA = 0.005;

    private final IReporteVentaService reporteVentaService;

    public PosicionRankingService(IReporteVentaService reporteVentaService) {
        this.reporteVentaService = reporteVentaService;
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<PosicionRankingDTO> posicionDeUnidad(UnidadVenta unidadVenta) {
        UnidadVenta unidad = (UnidadVenta) Hibernate.unproxy(unidadVenta);
        Festival festival = (Festival) Hibernate.unproxy(unidad.getFestival());

        List<RankingUnidadVentaDTO> ranking = reporteVentaService.rankingUnidadesPorRecaudacion(festival.getId());

        Optional<RankingUnidadVentaDTO> propia = ranking.stream()
                .filter(u -> u.getCodigoUnidad().equals(unidad.getCodigo()))
                .findFirst();
        if (propia.isEmpty()) {
            return Optional.empty();
        }

        double miRecaudacion = propia.get().getRecaudacion();
        double recaudacionLider = ranking.get(0).getRecaudacion();

        // Puesto con empates: 1 + unidades que recaudan más que la mía.
        long unidadesArriba = ranking.stream()
                .filter(u -> u.getRecaudacion() - miRecaudacion > TOLERANCIA)
                .count();
        int puesto = (int) unidadesArriba + 1;

        // La unidad "de arriba" es la que menos recauda entre las que recaudan más que yo.
        OptionalDouble inmediataSuperior = ranking.stream()
                .mapToDouble(RankingUnidadVentaDTO::getRecaudacion)
                .filter(r -> r - miRecaudacion > TOLERANCIA)
                .min();

        boolean empatada = ranking.stream()
                .filter(u -> Math.abs(u.getRecaudacion() - miRecaudacion) <= TOLERANCIA)
                .count() > 1;

        return Optional.of(PosicionRankingDTO.builder()
                .nombreFestival(festival.getNombre())
                .nombreUnidad(unidad.getNombreComercial())
                .codigoUnidad(unidad.getCodigo())
                .puesto(puesto)
                .totalUnidades(ranking.size())
                .recaudacion(miRecaudacion)
                .diferenciaConLider(recaudacionLider - miRecaudacion > TOLERANCIA ? recaudacionLider - miRecaudacion : 0)
                .diferenciaConSuperior(inmediataSuperior.isPresent() ? inmediataSuperior.getAsDouble() - miRecaudacion : 0)
                .lider(puesto == 1)
                .empatada(empatada)
                .build());
    }
}