package edu.unla.epicentro_gourmet.services.implementation;

import edu.unla.epicentro_gourmet.entities.Festival;
import edu.unla.epicentro_gourmet.repositories.IFestivalRepository;
import edu.unla.epicentro_gourmet.services.IFestivalService;
import org.springframework.stereotype.Service;

import java.util.List;

@Service("festivalService")
public class FestivalService implements IFestivalService {

    private final IFestivalRepository festivalRepository;

    public FestivalService(IFestivalRepository festivalRepository) {
        this.festivalRepository = festivalRepository;
    }

    @Override
    public List<Festival> getAll() {
        return festivalRepository.findAll();
    }

    @Override
    public Festival findById(long id) {
        return festivalRepository.findById(id).orElse(null);
    }

    @Override
    public void delete(long id) {
        festivalRepository.deleteById(id);
    }
}
