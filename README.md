# Proyecto Tienda_Virtual (Arquitectura Base de Datos SQL)

Sistema para Tienda Virtual desarrollado en **Java SE (Cero dependencias externas y Cero archivos JSON)** con **Arquitectura N-Capas**, patrón de diseño **DAO (Data Access Object)** y conector **JDBC (`java.sql.*`)** listo para integrarse con cualquier motor de Base de Datos SQL (MySQL, PostgreSQL, MariaDB, SQLite, H2, Oracle, SQL Server).

## Estructura Simplificada a 5 Entidades Esenciales
1. `Producto`: Catálogo de productos para la API REST tipo CRUD.
2. `Stock`: Gestión de inventario (Relación 1 a 1 con Producto).
3. `Cliente`: Registro de compradores.
4. `Pedido`: Cabecera de la venta.
5. `DetallePedido`: Ítems comprados por venta.

---

## Archivos SQL Incluidos
- **[`sql/schema.sql`](file:///c:/Users/USER/Desktop/Tienda_Virtual/sql/schema.sql)**: Script DDL de creación de tablas relacionales con llaves primarias, foráneas y restricciones.
- **[`sql/data.sql`](file:///c:/Users/USER/Desktop/Tienda_Virtual/sql/data.sql)**: Script DML de datos semilla iniciales.

---

## Compilación y Ejecución (Sin Librerías Externas)

### Compilar todo el proyecto:
```powershell
javac -encoding UTF-8 -d bin model/*.java data_access/*.java data_access/dao/*.java data_access/impl/*.java controller/*.java api/*.java vista/*.java
```

### Ejecutar la Aplicación (CLI Consola + Servidor REST API puerto 8080):
```powershell
java -cp bin vista.VistaMain
```

### Endpoints API REST (`http://localhost:8080`):
- `GET /api/productos`: Listar productos.
- `POST /api/productos`: Crear un nuevo producto.
- `GET /api/productos?id={id}`: Obtener un producto por ID.
- `PUT /api/productos`: Actualizar un producto.
- `DELETE /api/productos?id={id}`: Eliminar un producto.
- `GET /api/stock`: Consultar existencias de inventario.
- `PUT /api/stock`: Actualizar stock.
- `POST /api/ventas`: Registrar una venta y descontar automáticamente del inventario SQL.
- `GET /api/pedidos`: Listar ventas realizadas.
