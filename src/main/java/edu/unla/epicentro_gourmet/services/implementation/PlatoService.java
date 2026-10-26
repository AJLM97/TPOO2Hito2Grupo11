package edu.unla.epicentro_gourmet.services.implementation;

import edu.unla.epicentro_gourmet.entities.Plato;
import edu.unla.epicentro_gourmet.repositories.IPlatoRepository;
import edu.unla.epicentro_gourmet.services.IPlatoService;
import org.springframework.stereotype.Service;

import java.util.List;

@Service("platoService")
public class PlatoService implements IPlatoService {

    private final IPlatoRepository platoRepository;

    public PlatoService(IPlatoRepository platoRepository) {
        this.platoRepository = platoRepository;
    }

    @Override
    public List<Plato> getAll() {
        return platoRepository.findAll();
    }

    @Override
    public Plato save(Plato plato) {
        return platoRepository.save(plato);
    }

    @Override
    public Plato findById(long id) {
        return platoRepository.findById(id).orElse(null);
    }

    @Override
    public void delete(long id) {
        platoRepository.deleteById(id);
    }
}
