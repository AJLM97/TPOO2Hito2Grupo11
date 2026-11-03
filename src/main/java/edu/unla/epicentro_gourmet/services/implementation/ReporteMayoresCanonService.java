package edu.unla.epicentro_gourmet.services.implementation;

import edu.unla.epicentro_gourmet.dtos.DetalleCanonDTO;
import edu.unla.epicentro_gourmet.dtos.ReporteMayoresCanonDTO;
import edu.unla.epicentro_gourmet.entities.UnidadVenta;
import edu.unla.epicentro_gourmet.repositories.IUnidadVentaRepository;
import edu.unla.epicentro_gourmet.services.ICanonService;
import edu.unla.epicentro_gourmet.services.IReporteMayoresCanonService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Comparator;
import java.util.List;
import java.util.Optional;

@Service("reporteMayoresCanonService")
public class ReporteMayoresCanonService implements IReporteMayoresCanonService {
    private final IUnidadVentaRepository unidadVentaRepository;
    private final ICanonService canonService;

    public ReporteMayoresCanonService(IUnidadVentaRepository unidadVentaRepository, ICanonService canonService) {
        this.unidadVentaRepository = unidadVentaRepository;
        this.canonService = canonService;
    }

    @Override
    @Transactional(readOnly = true)
    public List<ReporteMayoresCanonDTO> mayoresCanon(long idFestival, int cantidad) {
        if (cantidad < 1) {
            throw new IllegalArgumentException("La cantidad de unidades debe ser mayor que cero.");
        }

        return unidadVentaRepository.findByFestival_Id(idFestival).stream()
                .map(this::reporteDe)
                .flatMap(Optional::stream)
                .sorted(Comparator.comparingDouble(ReporteMayoresCanonDTO::getCanon).reversed()
                        .thenComparing(ReporteMayoresCanonDTO::getNombreComercial))
                .limit(cantidad)
                .toList();
    }

    private Optional<ReporteMayoresCanonDTO> reporteDe(UnidadVenta unidad) {
        return canonService.calcular(unidad)
                .map(this::desdeDetalle);
    }

    private ReporteMayoresCanonDTO desdeDetalle(DetalleCanonDTO detalle) {
        return new ReporteMayoresCanonDTO(
                detalle.getNombreComercial(),
                detalle.getCodigo(),
                detalle.getTipoUnidad(),
                detalle.getCanon(),
                detalle);
    }
}
