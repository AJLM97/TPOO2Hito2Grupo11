package edu.unla.epicentro_gourmet.controllers;

import edu.unla.epicentro_gourmet.dtos.RespuestaFestival;
import edu.unla.epicentro_gourmet.dtos.RespuestaUnidadVenta;
import edu.unla.epicentro_gourmet.dtos.SolicitudFestival;
import edu.unla.epicentro_gourmet.services.IFestivalService;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/v1/festivales")
public class FestivalControl {
    private final IFestivalService festivalService;

    public FestivalControl(IFestivalService festivalService) {
        this.festivalService = festivalService;
    }

    @GetMapping(produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<List<RespuestaFestival>> listar() {
        return new ResponseEntity<>(festivalService.listar(), HttpStatus.OK);
    }

    @GetMapping(value = "/{id}", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<RespuestaFestival> buscar(@PathVariable("id") Long id) {
        return new ResponseEntity<>(festivalService.buscar(id), HttpStatus.OK);
    }

    @PostMapping(consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<RespuestaFestival> crear(@RequestBody SolicitudFestival solicitud) {
        return new ResponseEntity<>(festivalService.crear(solicitud), HttpStatus.CREATED);
    }

    @PutMapping(value = "/{id}", consumes = MediaType.APPLICATION_JSON_VALUE,
            produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<RespuestaFestival> actualizar(
            @PathVariable("id") Long id,
            @RequestBody SolicitudFestival solicitud
    ) {
        return new ResponseEntity<>(festivalService.actualizar(id, solicitud), HttpStatus.OK);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> eliminar(@PathVariable("id") Long id) {
        festivalService.eliminar(id);
        return new ResponseEntity<>(HttpStatus.NO_CONTENT);
    }

    @GetMapping(value = "/{id}/unidades", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<List<RespuestaUnidadVenta>> listarUnidades(@PathVariable("id") Long id) {
        return new ResponseEntity<>(festivalService.listarUnidades(id), HttpStatus.OK);
    }
}