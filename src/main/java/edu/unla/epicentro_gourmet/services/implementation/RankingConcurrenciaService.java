package edu.unla.epicentro_gourmet.services.implementation;

import edu.unla.epicentro_gourmet.dtos.RankingConcurrenciaDTO;
import edu.unla.epicentro_gourmet.entities.EnumTemporadaFestival;
import edu.unla.epicentro_gourmet.repositories.IFestivalRepository;
import edu.unla.epicentro_gourmet.services.IRankingConcurrenciaService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service("rankingConcurrenciaService")
public class RankingConcurrenciaService implements IRankingConcurrenciaService {
    private final IFestivalRepository festivalRepository;

    public RankingConcurrenciaService(IFestivalRepository festivalRepository) {
        this.festivalRepository = festivalRepository;
    }

    @Override
    @Transactional(readOnly = true)
    public List<RankingConcurrenciaDTO> rankingPorTemporada(EnumTemporadaFestival temporada) {
        return festivalRepository.findRankingConcurrenciaPorTemporada(temporada);
    }
}
