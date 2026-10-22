package edu.unla.epicentro_gourmet.controllers;

import edu.unla.epicentro_gourmet.dtos.RespuestaFestival;
import edu.unla.epicentro_gourmet.dtos.SolicitudFestival;
import edu.unla.epicentro_gourmet.services.IServicioFestival;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/festivales")
public class FestivalControl {
    private final IServicioFestival servicioFestival;

    public FestivalControl(IServicioFestival servicioFestival) {
        this.servicioFestival = servicioFestival;
    }

    @PostMapping(consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<RespuestaFestival> crear(@RequestBody SolicitudFestival solicitud) {
        return new ResponseEntity<>(servicioFestival.crear(solicitud), HttpStatus.CREATED);
    }

    @PutMapping(value = "/{id}", consumes = MediaType.APPLICATION_JSON_VALUE,
            produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<RespuestaFestival> actualizar(
            @PathVariable("id") Long id,
            @RequestBody SolicitudFestival solicitud
    ) {
        return new ResponseEntity<>(servicioFestival.actualizar(id, solicitud), HttpStatus.OK);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> eliminar(@PathVariable("id") Long id) {
        servicioFestival.eliminar(id);
        return new ResponseEntity<>(HttpStatus.NO_CONTENT);
    }
}