package edu.unla.epicentro_gourmet.services;

import edu.unla.epicentro_gourmet.dtos.ReporteMayoresCanonDTO;

import java.util.List;

public interface IReporteMayoresCanonService {
    List<ReporteMayoresCanonDTO> mayoresCanon(long idFestival, int cantidad);
}
