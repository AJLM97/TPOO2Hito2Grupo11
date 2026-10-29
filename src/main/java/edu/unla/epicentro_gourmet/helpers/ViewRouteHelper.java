package edu.unla.epicentro_gourmet.helpers;

public class ViewRouteHelper {
    /**** VIEWS ****/
    // HOME
    public final static String INDEX = "home/index";

    // ADMINISTRADOR
    public final static String ADMIN_FESTIVALES = "admin/festivales";
    public final static String ADMIN_REGISTRO_FESTIVAL = "admin/festivales-form";
    public final static String ADMIN_EDITAR_FESTIVAL = "admin/festivales-edit";
    public final static String ADMIN_UNIDADES = "admin/unidades";
    public final static String ADMIN_UNIDADES_FORM = "admin/unidades-form";
    public final static String ADMIN_UNIDADES_EDIT = "responsable/platos-edit";
    public final static String ADMIN_RANKING_RECAUDACION = "admin/ranking-recaudacion";
    public final static String ADMIN_MAYORES_CANON = "admin/mayores-canon";

    // RESPONSABLE
    public final static String RESPONSABLE_LIQUIDACION = "responsable/liquidacion";
    public final static String RESPONSABLE_EMPLEADOS = "responsable/empleados";
    public final static String RESPONSABLE_AUDITORIA_PEDIDOS = "responsable/auditoria-pedidos";
    public final static String RESPONSABLE_EMPLEADO_FORM = "responsable/empleados-form";
    public final static String RESPONSABLE_REPORTE_RENTABILIDAD = "responsable/reporte-rentabilidad";
    public final static String RESPONSABLE_PUNTO_EQUILIBRIO = "responsable/punto-equilibrio";
    public final static String RESPONSABLE_PLATOS = "responsable/platos";
    public final static String RESPONSABLE_REPORTE_RECAUDACION = "responsable/reporte-recaudacion";
    public final static String RESPONSABLE_PLATO_FORM = "responsable/platos-form";
    public final static String RESPONSABLE_REPORTE_PLATO_ESTRELLA = "responsable/reporte-plato-estrella";
    public final static String RESPONSABLE_PLATO_EDIT = "responsable/platos-edit";
    public final static String RESPONSABLE_CANON = "responsable/canon";

    // EMPLEADO
    public final static String CAJERO_PEDIDOS = "empleado/pedidos";
    public final static String CAJERO_PEDIDO_FORM = "empleado/pedidos-form";

    /**** REDIRECTS ****/
    public final static String USER_LOGIN = "user/login";
    public final static String USER_LOGOUT = "user/logout";

    public final static String REDIRECT_ADMIN_FESTIVALES = "/admin/festivales";
    public final static String REDIRECT_CAJERO_PEDIDOS = "/empleado/pedidos";
}
