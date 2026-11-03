$(document).ready(function() {
    $('#tipoEmpleado').change(function() {
        var tipoSeleccionado = $(this).val();

        if (tipoSeleccionado === "CAJERO") {
            $('#contenedorCajero').show();
            $('#contenedorCocinero').hide();
        } else if (tipoSeleccionado === "COCINERO") {
            $('#contenedorCocinero').show();
            $('#contenedorCajero').hide();
        } else {
            $('#contenedorCajero').hide();
            $('#contenedorCocinero').hide();
        }
    });
});
