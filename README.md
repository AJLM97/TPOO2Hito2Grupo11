# Sistema de Gestión "Epicentro Gourmet"

Proyecto desarrollado para la asignatura [Orientación a Objetos 2] - [2026 - Segundo Cuatrimestre]. Esta es una aplicación web desarrollada con Spring Boot para administrar festivales temáticos, sus unidades de venta, empleados, platos y resultados económicos.

## Equipo

- **Número de grupo:** 11
- **Integrantes:**
  - Leal, Arian Gabriel — [@ari2709](https://github.com/ari2709)
  - Ledesma Miño, Alejandro Javier — [@AJLM97](https://github.com/AJLM97)
  - Boullon, Juan Bautista — [@JuanBa7](https://github.com/JuanBa7)
  - Vogt, Thomas Gebhard — [@Thomy98](https://github.com/Thomy98)

## Diagrama de clases

![Diagrama de clases](./diagrama.png)

## Tecnologías y requisitos

- Java 21 o 25
- MySQL
- Maven

## Preparar y ejecutar la aplicación

1. Instalar Java 21 o 25 y MySQL, e iniciar el servidor de MySQL.
2. Crear la base de datos:

   ```sql
   CREATE DATABASE bd_tp_grupal_oo2_grupo11;
   ```

3. En la raíz del proyecto, crear un archivo `.env` con las credenciales locales. Este archivo está excluido de Git:

   ```properties
   DB_HOST=localhost:3306
   DB_NAME=bd_tp_grupal_oo2_grupo11
   DB_USER=root
   DB_PASS=tu_clave_de_mysql
   SMTP_USER=tu_correo@gmail.com
   SMTP_PASS=tu_clave_de_aplicacion
   ```

   `SMTP_USER` y `SMTP_PASS` se usan para enviar correos. Para Gmail, `SMTP_PASS` debe ser una contraseña de aplicación. La configuración de base de datos también tiene valores predeterminados en `application.yaml`, pero se recomienda usar los valores locales del `.env`.

4. Desde la raíz del proyecto, iniciar la aplicación:

   ```bash
   # 1. Compilar el empaquetado de Maven
   ./mvnw clean package -DskipTests
   # 2. Iniciar la aplicación
   ./mvnw spring-boot:run
   ```

5. Opcionalmente, para cargar los datos de demostración, se recomienda ejecutar el archivo:
    ```
    .\src\main\resources\db\seed.sql
    ``` 
    Recomendablemente desde MySQL Workbench o un programa similar.

## Casos de uso implementados y responsables

| Caso de uso | Funcionalidad | Responsable | Pull-Request |
|---|---|---|---|
| `CUIniciarSesionAlejandroLedesma` | Inicio y cierre de sesión de usuarios. | Alejandro Javier Ledesma Miño ([@AJLM97](https://github.com/AJLM97)) | [#10](https://github.com/AJLM97/TPOO2Hito2Grupo11/pull/10) |
| `CUGestionarAccesoPublicoAlejandroLedesma` | Consulta pública de festivales y acceso a la información disponible sin iniciar sesión. | Alejandro Javier Ledesma Miño ([@AJLM97](https://github.com/AJLM97)) | [#23](https://github.com/AJLM97/TPOO2Hito2Grupo11/pull/23) |
| `CUGestionUnidadesDeVentaAlejandroLedesma` | Consulta y administración de unidades de venta asociadas a festivales. | Alejandro Javier Ledesma Miño ([@AJLM97](https://github.com/AJLM97)) | [#13](https://github.com/AJLM97/TPOO2Hito2Grupo11/pull/13) |
| `CUGestionEmpleadoAlejandroLedesma` | Alta, consulta, actualización y baja de empleados de una unidad. | Alejandro Javier Ledesma Miño ([@AJLM97](https://github.com/AJLM97)) | [#11](https://github.com/AJLM97/TPOO2Hito2Grupo11/pull/11) |
| `CUConsultarRankingUnidadesAlejandroLedesma` | Ranking de unidades de venta por recaudación dentro de un festival. | Alejandro Javier Ledesma Miño ([@AJLM97](https://github.com/AJLM97)) | [#24](https://github.com/AJLM97/TPOO2Hito2Grupo11/pull/24) |
| `CUReporteMayoresCanonAlejandroLedesma` | Consulta de las unidades con mayor canon de un festival. | Alejandro Javier Ledesma Miño ([@AJLM97](https://github.com/AJLM97)) | [#25](https://github.com/AJLM97/TPOO2Hito2Grupo11/pull/25) |
| `CUPlatoEstrellaThomasVogt` | Identifica el plato con mayor cantidad de unidades vendidas. | Thomas Gebhard Vogt ([@Thomy98](https://github.com/Thomy98)) | [#20](https://github.com/AJLM97/TPOO2Hito2Grupo11/pull/20) |
| `CURentabilidadNetaThomasVogt` | Cálculo de rentabilidad neta de una unidad, considerando ventas, costos, sueldos y canon. | Thomas Gebhard Vogt ([@Thomy98](https://github.com/Thomy98)) | [#19](https://github.com/AJLM97/TPOO2Hito2Grupo11/pull/19) |
| `CUReporteRecaudacionThomasVogt` | Reportes de recaudación de la unidad y sus pedidos cerrados. | Thomas Gebhard Vogt ([@Thomy98](https://github.com/Thomy98)) | [#18](https://github.com/AJLM97/TPOO2Hito2Grupo11/pull/18) |
| `CULiquidacionHaberesThomasVogt` | Cálculo y consulta de liquidaciones de haberes del personal. | Thomas Gebhard Vogt ([@Thomy98](https://github.com/Thomy98)) | [#17](https://github.com/AJLM97/TPOO2Hito2Grupo11/pull/17) |
| `CUCalculoCanonThomasVogt` | Cálculo del canon de una unidad con su desglose de costos. | Thomas Gebhard Vogt ([@Thomy98](https://github.com/Thomy98)) | [#16](https://github.com/AJLM97/TPOO2Hito2Grupo11/pull/16) |
| `CUGestionPedidosThomasVogt` | Gestión de pedidos y sus ítems por parte del personal de la unidad. | Thomas Gebhard Vogt ([@Thomy98](https://github.com/Thomy98)) | [#15](https://github.com/AJLM97/TPOO2Hito2Grupo11/pull/15) |
| `CUGestionarPlatosThomasVogt` | Alta, consulta, actualización y baja de platos de una unidad. | Thomas Gebhard Vogt ([@Thomy98](https://github.com/Thomy98)) | [#12](https://github.com/AJLM97/TPOO2Hito2Grupo11/pull/12) |
| `CUGestionarFestivalesThomasVogt` | Alta, consulta, actualización y baja de festivales. | Thomas Gebhard Vogt ([@Thomy98](https://github.com/Thomy98)) | [#05](https://github.com/AJLM97/TPOO2Hito2Grupo11/pull/5) |
| `CUFiltrarEmpleadosArianLeal` | Agrega un filtro para que el responsable consulte variables de los empleados. | Arian Gabriel Leal ([@ari2709](https://github.com/ari2709)) | [#29](https://github.com/AJLM97/TPOO2Hito2Grupo11/pull/29) |
| `CUInformacionEmpArianLeal` | Muestra la información principal de un empleado como Unidad de venta y festival al que pertenece. | Arian Gabriel Leal ([@ari2709](https://github.com/ari2709)) | [#33](https://github.com/AJLM97/TPOO2Hito2Grupo11/pull/33) |
| `CUReciboSueldoArianLeal` | Genera un archivo PDF con el recibo de sueldo del empleado. | Arian Gabriel Leal ([@ari2709](https://github.com/ari2709)) | [#34](https://github.com/AJLM97/TPOO2Hito2Grupo11/pull/34) |
| `CUCerrarCajaDelDiaJuanBoullon` | Genera un archivo PDF con el recibo de sueldo del empleado. | Arian Gabriel Leal ([@ari2709](https://github.com/ari2709)) | [#34](https://github.com/AJLM97/TPOO2Hito2Grupo11/pull/34) |
| `CUPosicionRankingArianLeal` | Cajero cierra la caja del día, eliminando pedidos abiertos y mostrando la recaudación del día. | Juan Bautista Boullon ([@JuanBa7](https://github.com/JuanBa7)) | [#39](https://github.com/AJLM97/TPOO2Hito2Grupo11/pull/39) |

## Casos de uso personales y responsables

| Caso de uso | Funcionalidad | Responsable | Pull-Request |
|---|---|---|---|
| `CUAuditoriaPedidosThomasVogt` | Búsqueda de los pedidos de la unidad por rango de fechas, estado (abierto o cerrado) y plato, con el total y la cantidad de ítems de cada uno. Permite revisar las ventas de un día y detectar pedidos abiertos | Thomas Gebhard Vogt ([@Thomy98](https://github.com/Thomy98)) | [#21](https://github.com/AJLM97/TPOO2Hito2Grupo11/pull/21) |
| `CUPuntoEquilibrioThomasVogt` | Indica si la unidad ya cubrió el canon y los sueldos con su ganancia bruta (ventas cerradas menos costo de los platos), con el porcentaje cubierto, lo que falta ganar y una estimación de lo que falta vender | Thomas Gebhard Vogt ([@Thomy98](https://github.com/Thomy98)) | [#22](https://github.com/AJLM97/TPOO2Hito2Grupo11/pull/22) |
| `CUConsultarPlatosMasRentablesAlejandroLedesma` | Lista platos ordenados por rentabilidad en una unidad de venta. | Alejandro Javier Ledesma Miño ([@AJLM97](https://github.com/AJLM97)) | [#26](https://github.com/AJLM97/TPOO2Hito2Grupo11/pull/26) |
| `CURankingConcurrenciaPorTemporadaAlejandroLedesma` | Ranking de festivales por temporada según pedidos cerrados, usados como estimación de concurrencia; muestra también el año del festival. | Alejandro Javier Ledesma Miño ([@AJLM97](https://github.com/AJLM97)) | [#28](https://github.com/AJLM97/TPOO2Hito2Grupo11/pull/28) |
| `CUBusquedaPlatosArianLeal` | Agrega un filtro para que el responsable consulte variables de los platos. | Arian Gabriel Leal ([@ari2709](https://github.com/ari2709)) | [#37](https://github.com/AJLM97/TPOO2Hito2Grupo11/pull/37) |
