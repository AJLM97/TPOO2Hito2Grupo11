# Datos de demostración

`seed.sql` carga un conjunto integrado de Festival, costos, usuarios, unidades de venta, empleados, platos, pedidos e ítems para probar los flujos del Hito 2.

## Uso

Este script es exclusivo para una base MySQL de desarrollo vacía. No se ejecuta automáticamente por Spring Boot y no debe aplicarse a producción ni a una base que ya contenga registros. Los identificadores se fijan para que las relaciones del ejemplo sean fáciles de inspeccionar; por eso el seed no es idempotente.

1. Crear la base indicada por `DB_NAME` y configurar `DB_HOST`, `DB_USER` y `DB_PASS`.
2. Ejecutar la aplicación una vez en el perfil de desarrollo para que Hibernate cree el esquema con `ddl-auto: update`.
3. Desde la raíz del proyecto, cargar manualmente el script en esa base:

```powershell
Get-Content .\src\main\resources\db\seed.sql | mysql -h localhost -u root -p bd_tp_grupal_oo2_grupo11
```

Reemplazar host, usuario y nombre de base por los valores configurados localmente. El comando solicita la contraseña de MySQL.

## Acceso de demostración

Los cuatro usuarios del seed comparten la contraseña `DemoEpicentro2026!`. El valor almacenado en `usuarios.password` es BCrypt, no texto plano. El inicio de sesión solo estará operativo cuando se integre la configuración de autenticación de la aplicación.

## Reglas de aplicación de costos

- `costoSuperficie`: alcance inicial `TODAS`; se calcula por m² para cada unidad incluida.
- `costoMontaje`: alcance inicial `PUESTO_DESARMABLE`; se aplica al montaje del puesto.
- `plusElectricidad`: alcance inicial `FOOD_TRUCK`; además, solo corresponde a food trucks con `conexion_electrica = true`.
- `sueldoBase`, `plusAntiguedad` y adicionales de cocina se usan en liquidaciones de empleados, no en el canon de la unidad.

El servicio rechaza la configuración de `plusElectricidad` con alcance `PUESTO_DESARMABLE`, porque ese tipo de unidad no registra conexión eléctrica.

Los alcances del canon se guardan en `costos` y se devuelven en la configuración del Festival. Las fechas del Festival y de los pedidos se calculan al ejecutar el script, para que los pedidos de ejemplo no queden con una fecha futura. El seed permite inspeccionar los datos y los tipos; los cálculos de canon y haberes se implementan en sus casos de uso correspondientes.