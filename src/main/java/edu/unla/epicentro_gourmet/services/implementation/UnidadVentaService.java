package edu.unla.epicentro_gourmet.services.implementation;

import edu.unla.epicentro_gourmet.entities.UnidadVenta;
import edu.unla.epicentro_gourmet.repositories.IUnidadVentaRepository;
import edu.unla.epicentro_gourmet.services.IUnidadVentaService;
import org.springframework.stereotype.Service;

import java.util.List;

@Service("unidadVentaService")
public class UnidadVentaService implements IUnidadVentaService {

    private final IUnidadVentaRepository unidadVentaRepository;

    public UnidadVentaService(IUnidadVentaRepository unidadVentaRepository) {
        this.unidadVentaRepository = unidadVentaRepository;
    }

    @Override
    public List<UnidadVenta> getAll() {
        return unidadVentaRepository.findAll();
    }

    @Override
    public UnidadVenta findById(long id) {
        return unidadVentaRepository.findById(id).orElse(null);
    }

    @Override
    public UnidadVenta save(UnidadVenta unidadVenta) {
        return unidadVentaRepository.save(unidadVenta);
    }

    @Override
    public void delete(long id) {
        unidadVentaRepository.deleteById(id);
    }

    @Override
    public UnidadVenta getUnidadVentaFromResponsableEmail(String email) {
        return unidadVentaRepository.findUnidadVentaFromResponsableEmail(email);
    }
}
