package edu.unla.epicentro_gourmet.services.implementation;

import edu.unla.epicentro_gourmet.entities.Plato;
import edu.unla.epicentro_gourmet.entities.UnidadVenta;
import edu.unla.epicentro_gourmet.repositories.IPlatoRepository;
import edu.unla.epicentro_gourmet.services.EnumResultadoGuardadoPlato;
import edu.unla.epicentro_gourmet.services.IPlatoService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

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

    @Override
    public List<Plato> findByUnidadVenta(UnidadVenta unidadVenta) {
        return platoRepository.findByUnidadAndActivoTrue(unidadVenta);
    }

    @Override
    public Optional<Plato> findByIdAndUnidadVenta(long id, UnidadVenta unidadVenta) {
        return platoRepository.findByIdAndUnidadAndActivoTrue(id, unidadVenta);
    }

    @Override
    @Transactional
    public EnumResultadoGuardadoPlato altaOReactivar(Plato candidato) {
        String nombre = candidato.getNombre().trim();
        List<Plato> mismoNombre = platoRepository.findByUnidadAndNombreIgnoreCase(candidato.getUnidad(), nombre);

        if (mismoNombre.stream().anyMatch(Plato::isActivo)) {
            return EnumResultadoGuardadoPlato.NOMBRE_DUPLICADO;
        }

        if (!mismoNombre.isEmpty()) {
            Plato existente = mismoNombre.get(0);
            existente.setNombre(nombre);
            existente.setPrecioVenta(candidato.getPrecioVenta());
            existente.setCostoProduccion(candidato.getCostoProduccion());
            existente.setActivo(true);
            platoRepository.save(existente);
            return EnumResultadoGuardadoPlato.REACTIVADO;
        }

        candidato.setId(null);
        candidato.setNombre(nombre);
        platoRepository.save(candidato);
        return EnumResultadoGuardadoPlato.CREADO;
    }

    @Override
    @Transactional
    public EnumResultadoGuardadoPlato actualizar(UnidadVenta unidadVenta, Plato datos) {
        Optional<Plato> encontrado = platoRepository.findByIdAndUnidadAndActivoTrue(datos.getId(), unidadVenta);
        if (encontrado.isEmpty()) {
            return EnumResultadoGuardadoPlato.NO_ENCONTRADO;
        }

        Plato plato = encontrado.get();
        String nombre = datos.getNombre().trim();
        boolean nombreEnOtroPlato = platoRepository.findByUnidadAndNombreIgnoreCase(unidadVenta, nombre).stream()
                .anyMatch(otro -> otro.isActivo() && !otro.getId().equals(plato.getId()));
        if (nombreEnOtroPlato) {
            return EnumResultadoGuardadoPlato.NOMBRE_DUPLICADO;
        }

        plato.setNombre(nombre);
        plato.setPrecioVenta(datos.getPrecioVenta());
        plato.setCostoProduccion(datos.getCostoProduccion());
        platoRepository.save(plato);
        return EnumResultadoGuardadoPlato.ACTUALIZADO;
    }

    @Override
    @Transactional
    public void logicalDelete(UnidadVenta unidadVenta, long id) {
        platoRepository.findByIdAndUnidadAndActivoTrue(id, unidadVenta).ifPresent(plato -> {
            plato.setActivo(false);
            platoRepository.save(plato);
        });
    }
}
