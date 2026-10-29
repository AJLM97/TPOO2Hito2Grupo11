package edu.unla.epicentro_gourmet.services;

import edu.unla.epicentro_gourmet.dtos.RankingConcurrenciaDTO;
import edu.unla.epicentro_gourmet.entities.EnumTemporadaFestival;

import java.util.List;

public interface IRankingConcurrenciaService {
    List<RankingConcurrenciaDTO> rankingPorTemporada(EnumTemporadaFestival temporada);
}
