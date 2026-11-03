$(document).ready(function() {
    $('#tipoUnidadVenta').change(function() {
        var tipoSeleccionado = $(this).val();

        if (tipoSeleccionado === "PUESTO_DESARMABLE") {
            $('#contenedorCantidadDeCarpas').show();
            $('#contenedorTiempoMontajeMinutos').show();
            $('#contenedorPatente').hide();
            $('#contenedorConexionElectrica').hide();
        } else if (tipoSeleccionado === "FOOD_TRUCK") {
            $('#contenedorPatente').show();
            $('#contenedorConexionElectrica').show();
            $('#contenedorCantidadDeCarpas').hide();
            $('#contenedorTiempoMontajeMinutos').hide();
        } else {
            $('#contenedorCantidadDeCarpas').hide();
            $('#contenedorTiempoMontajeMinutos').hide();
            $('#contenedorPatente').hide();
            $('#contenedorConexionElectrica').hide();
        }
    });
});
