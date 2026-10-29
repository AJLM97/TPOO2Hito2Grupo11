-- Demo data for a dedicated, empty development database only.
-- Schema must already exist. See src/main/resources/db/README.md.

INSERT INTO costos (
    id, costo_superficie, costo_montaje, plus_electricidad,
    plus_lavaplatos, plus_ayudante, plus_cocinero, plus_antiguedad,
    sueldo_base, aplicacion_costo_superficie, aplicacion_costo_montaje,
    aplicacion_plus_electricidad
) VALUES
(9001, 500, 10, 2000, 50000, 100000, 150000, 5000, 100000,
 'TODAS', 'PUESTO_DESARMABLE', 'FOOD_TRUCK');

INSERT INTO festivales (id, nombre, temporada, fecha_inicio, fecha_fin, id_costos)
VALUES (9001, 'Festival Gourmet de Primavera', 'PRIMAVERA', CURRENT_DATE,
    DATE_ADD(CURRENT_DATE, INTERVAL 2 DAY), 9001);

INSERT INTO usuarios (id, email, password, rol, activo) VALUES
(9201, 'admin.demo@epicentro.local', '$2a$10$4yWW84ZZZ.rzgEAABvrZZeIYorhznqhIUO/H7em.eRNNSqK9ELGO6', 'ADMINISTRADOR', true),
(9202, 'responsable.demo@epicentro.local', '$2a$10$4yWW84ZZZ.rzgEAABvrZZeIYorhznqhIUO/H7em.eRNNSqK9ELGO6', 'RESPONSABLE', true),
(9203, 'cocinero.demo@epicentro.local', '$2a$10$4yWW84ZZZ.rzgEAABvrZZeIYorhznqhIUO/H7em.eRNNSqK9ELGO6', 'EMPLEADO', true),
(9204, 'cajero.demo@epicentro.local', '$2a$10$4yWW84ZZZ.rzgEAABvrZZeIYorhznqhIUO/H7em.eRNNSqK9ELGO6', 'EMPLEADO', true);

INSERT INTO unidades_venta (
    id, nombre_comercial, superficie, id_responsable_usuario, codigo, festival_id
) VALUES
(9101, 'Sabores sobre Ruedas', 20, 9202, 'FT-SPR-001', 9001),
(9102, 'Puesto La Huerta', 12, NULL, 'PD-SPR-001', 9001);

INSERT INTO food_trucks (id, patente, conexion_electrica)
VALUES (9101, 'AB123CD', true);

INSERT INTO puestos_desarmables (id, cantidad_de_carpas, tiempo_montaje_minutos)
VALUES (9102, 2, 45);

INSERT INTO empleados (
    id, nombre, apellido, dni, fecha_nacimiento, fecha_ingreso,
    sueldo_base, activo, id_unidad_venta, id_usuario
) VALUES
(9301, 'Lucia', 'Fernandez', 30123456, '1995-03-12', '2020-02-01', 100000, true, 9101, 9203),
(9302, 'Mateo', 'Gomez', 28987654, '1993-07-20', '2019-06-15', 100000, true, 9101, 9204);

INSERT INTO cocineros (id, categoria)
VALUES (9301, 'COCINERO');

INSERT INTO cajeros (id, turno_trabajo)
VALUES (9302, 'MANANA');

INSERT INTO platos (id, nombre, precio_venta, costo_produccion, id_unidad_venta) VALUES
(9401, 'Empanada de hongos', 3500, 1200, 9101),
(9402, 'Sanguche de vegetales', 6200, 2300, 9101),
(9403, 'Limonada de estación', 2800, 700, 9102);

INSERT INTO pedidos (id, fecha_transaccion, cerrado, festival_id, unidad_venta_id) VALUES
(9501, CURRENT_TIMESTAMP, true, 9001, 9101),
(9502, CURRENT_TIMESTAMP, true, 9001, 9102);

INSERT INTO items_pedido (id_item_pedido, id_plato, id_pedido, cantidad) VALUES
(9601, 9401, 9501, 3),
(9602, 9402, 9501, 2),
(9603, 9403, 9502, 4);