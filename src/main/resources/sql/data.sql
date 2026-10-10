INSERT INTO CLIENTE (cliente_id, nombre, apellido, email, telefono, fecha_registro) VALUES 
('CLI001', 'Alexis', 'Giraldo', 'alexis.giraldo@email.com', '+573001234567', '2026-01-15'),
('CLI002', 'Maria', 'Lopez', 'maria.lopez@email.com', '+573109876543', '2026-02-20');

INSERT INTO PRODUCTO (producto_id, nombre, descripcion, precio_base, estado_activo) VALUES 
('PROD001', 'Laptop Gaming Pro 15', 'Computador portátil Intel i7 16GB RAM 512GB SSD', 4500000.00, true),
('PROD002', 'Mouse Inalámbrico Ergonómico', 'Mouse óptico recargable Bluetooth 2.4GHz', 120000.00, true),
('PROD003', 'Teclado Mecánico RGB', 'Teclado gamer switches blue retroiluminado', 250000.00, true),
('PROD004', 'Monitor 27 Pulgadas 144Hz', 'Monitor IPS Full HD respuesta 1ms', 1100000.00, true);

INSERT INTO STOCK (stock_id, producto_id, cantidad_disponible, stock_minimo, ultima_actualizacion) VALUES 
('STK001', 'PROD001', 10, 2, '2026-09-25T10:00:00'),
('STK002', 'PROD002', 50, 5, '2026-09-25T10:00:00'),
('STK003', 'PROD003', 30, 3, '2026-09-25T10:00:00'),
('STK004', 'PROD004', 15, 2, '2026-09-25T10:00:00');
