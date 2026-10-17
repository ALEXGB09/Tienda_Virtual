-- IMPORTANTE:
-- Este script elimina las tablas existentes y sus datos (DROP TABLE). Ejecutarlo SOLAMENTE al crear una base de datos desde cero.

--DROP TABLE IF EXISTS DETALLE_PEDIDO;
--DROP TABLE IF EXISTS PEDIDO;
--DROP TABLE IF EXISTS CLIENTE;
--DROP TABLE IF EXISTS STOCK;
--DROP TABLE IF EXISTS PRODUCTO;

CREATE TABLE PRODUCTO (
    producto_id VARCHAR(50) PRIMARY KEY,
    nombre VARCHAR(150) NOT NULL,
    descripcion VARCHAR(MAX),
    precio_base DECIMAL(12,2) NOT NULL,
    estado_activo BIT NOT NULL
        CONSTRAINT DF_PRODUCTO_estado_activo DEFAULT (1),

    CONSTRAINT CK_PRODUCTO_precio
        CHECK (precio_base >= 0)
);

CREATE TABLE STOCK (
    stock_id VARCHAR(50) PRIMARY KEY,
    producto_id VARCHAR(50) UNIQUE NOT NULL,
    cantidad_disponible INT NOT NULL DEFAULT 0,
    stock_minimo INT NOT NULL DEFAULT 0,
    ultima_actualizacion VARCHAR(50),

    CONSTRAINT CK_STOCK_cantidad
        CHECK (cantidad_disponible >= 0),

    CONSTRAINT CK_STOCK_minimo
        CHECK (stock_minimo >= 0),

    CONSTRAINT FK_STOCK_PRODUCTO
        FOREIGN KEY (producto_id)
        REFERENCES PRODUCTO(producto_id)
        ON DELETE CASCADE
);

CREATE TABLE CLIENTE (
    cliente_id VARCHAR(50) PRIMARY KEY,
    nombre VARCHAR(100) NOT NULL,
    apellido VARCHAR(100) NOT NULL,
    email VARCHAR(150) UNIQUE NOT NULL,
    telefono VARCHAR(20),
    fecha_registro VARCHAR(50)
);

CREATE TABLE PEDIDO (
    pedido_id VARCHAR(50) PRIMARY KEY,
    cliente_id VARCHAR(50) NOT NULL,
    direccion_envio VARCHAR(255) NOT NULL,
    fecha_pedido VARCHAR(50) NOT NULL,
    monto_total DECIMAL(12,2) NOT NULL,
    estado VARCHAR(50) NOT NULL DEFAULT 'COMPLETADO',

    CONSTRAINT CK_PEDIDO_monto
        CHECK (monto_total >= 0),

    CONSTRAINT FK_PEDIDO_CLIENTE
        FOREIGN KEY (cliente_id)
        REFERENCES CLIENTE(cliente_id)
);

CREATE TABLE DETALLE_PEDIDO (
    detalle_id VARCHAR(50) PRIMARY KEY,
    pedido_id VARCHAR(50) NOT NULL,
    producto_id VARCHAR(50) NOT NULL,
    cantidad INT NOT NULL,
    precio_unitario DECIMAL(12,2) NOT NULL,

    CONSTRAINT CK_DETALLE_cantidad
        CHECK (cantidad > 0),

    CONSTRAINT CK_DETALLE_precio
        CHECK (precio_unitario >= 0),

    CONSTRAINT FK_DETALLE_PEDIDO
        FOREIGN KEY (pedido_id)
        REFERENCES PEDIDO(pedido_id)
        ON DELETE CASCADE,

    CONSTRAINT FK_DETALLE_PRODUCTO
        FOREIGN KEY (producto_id)
        REFERENCES PRODUCTO(producto_id)
);
