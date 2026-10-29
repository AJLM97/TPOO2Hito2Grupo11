package edu.unla.epicentro_gourmet;

import static org.junit.jupiter.api.Assertions.assertTrue;

import jakarta.validation.Validation;
import jakarta.validation.Validator;
import org.junit.jupiter.api.Test;

import edu.unla.epicentro_gourmet.dtos.UnidadVentaDTO;

class PruebasUnidadVentaDTOTests {
    @Test
    void generaCodigoConFormatoDePuestoDesarmable() {
        UnidadVentaDTO unidad = new UnidadVentaDTO();

        assertTrue(unidad.getCodigo().matches("PD-\\d{6}"));
    }

    @Test
    void cambiaElPrefijoSegunElTipoDeUnidad() {
        UnidadVentaDTO unidad = new UnidadVentaDTO();

        unidad.setTipoUnidadVenta("FOOD_TRUCK");
        assertTrue(unidad.getCodigo().matches("FT-\\d{6}"));

        unidad.setTipoUnidadVenta("PUESTO_DESARMABLE");
        assertTrue(unidad.getCodigo().matches("PD-\\d{6}"));
    }

    @Test
    void permiteValidarUnidadSinPasswordParaFlujosAdministrativos() {
        UnidadVentaDTO unidad = new UnidadVentaDTO();
        unidad.setNombreComercial("Unidad de prueba");
        unidad.setSuperficie(10);
        unidad.setEmailResponsable("responsable@epicentro.com");
        unidad.setTipoUnidadVenta("PUESTO_DESARMABLE");
        unidad.setCantidadDeCarpas(1);
        unidad.setTiempoMontajeMinutos(30);

        try (var factory = Validation.buildDefaultValidatorFactory()) {
            Validator validator = factory.getValidator();

            assertTrue(validator.validate(unidad).isEmpty());
        }
    }
}
