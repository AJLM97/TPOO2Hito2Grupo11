package edu.unla.epicentro_gourmet.dtos;

import java.util.UUID;

import jakarta.validation.constraints.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class UnidadVentaDTO {
    private long id;
    private String nombreComercial;
    @Positive(message = "La superficie debe ser mayor que cero")
    private double superficie;
    @NotBlank(message = "El email del responsable es obligatorio")
    private String emailResponsable;
    private String codigo = UUID.randomUUID().toString().substring(0, 10);
    private String tipoUnidadVenta;

    private String patente;
    private boolean conexionElectrica;

    private long cantidadDeCarpas;
    private long tiempoMontajeMinutos;

    private String passwordResponsable;
    private String repetirPassword;
    
    public UnidadVentaDTO(long id, String nombreComercial, double superficie, String emailResponsable, String codigo, String tipoUnidadVenta, String patente, boolean conexionElectrica, long cantidadDeCarpas, long tiempoMontajeMinutos, String passwordResponsable, String repetirPassword) {
        this.id = id;
        this.nombreComercial = nombreComercial;
        this.superficie = superficie;
        this.emailResponsable = emailResponsable;
        this.codigo = codigo;
        this.tipoUnidadVenta = tipoUnidadVenta;
        this.patente = patente;
        this.conexionElectrica = conexionElectrica;
        this.cantidadDeCarpas = cantidadDeCarpas;
        this.tiempoMontajeMinutos = tiempoMontajeMinutos;
        this.passwordResponsable = passwordResponsable;
        this.repetirPassword = repetirPassword;
    }

    @AssertTrue(message = "La patente no tiene un formato válido de Argentina")
    public boolean isPatenteValida() {
        if (!"FOOD_TRUCK".equalsIgnoreCase(tipoUnidadVenta)) { return true;}
        return patente != null && patente.trim().toUpperCase().matches("^(?:[A-Z]{3}\\d{3}|[A-Z]{2}\\d{3}[A-Z]{2})$");
    }

    @AssertTrue(message = "La cantidad de carpas debe ser mayor que cero")
    public boolean isCantidadDeCarpasValida() {
        return !"PUESTO_DESARMABLE".equalsIgnoreCase(tipoUnidadVenta) || cantidadDeCarpas > 0;
    }

    @AssertTrue(message = "El tiempo de montaje debe ser mayor que cero")
    public boolean isTiempoMontajeValido() {
        return !"PUESTO_DESARMABLE".equalsIgnoreCase(tipoUnidadVenta) || tiempoMontajeMinutos > 0;
    }

    @AssertTrue(message = "Las contraseñas no pueden estar vacías")
    public boolean isPasswordVacio() {
        return passwordResponsable != null && repetirPassword != null;
    }

    @AssertTrue(message = "Las contraseñas no coinciden")
    public boolean isPasswordCoinciden() {
        return passwordResponsable != null && passwordResponsable.equals(repetirPassword);
    }
}
