# Tienda Virtual — API REST

Proyecto académico de una tienda virtual desarrollado en Java, con arquitectura por capas, API REST y persistencia de datos mediante JDBC y SQL Server.

## Arquitectura

El proyecto organiza sus responsabilidades en diferentes capas:

- **Modelo (`model`):** representa las entidades del sistema: `Producto`, `Stock`, `Cliente`, `Pedido` y `DetallePedido`.
- **Acceso a datos (`data_access`):** interfaces DAO y sus implementaciones JDBC para cada entidad, con control transaccional.
- **Servicios (`service`):** lógica de negocio aislada. Cada servicio recibe sus dependencias por constructor (inyección de dependencias).
- **Controladores (`controller`):** coordinan las operaciones delegando en los servicios; no contienen SQL ni lógica HTTP.
- **API REST (`api`):** handlers HTTP que leen la solicitud, delegan en el controlador y devuelven JSON con el código HTTP apropiado.
- **Vista (`vista`):** interfaz de consola interactiva y punto de entrada de la aplicación (`VistaMain`).
- **Excepciones (`exception`):** jerarquía de excepciones tipadas (`ValidationException` → 400, `ResourceNotFoundException` → 404, `ConflictException` → 409, `InsufficientStockException` → 409).
- **Recursos (`src/main/resources`):** scripts SQL (`schema.sql`, `data.sql`) y especificación OpenAPI (`openapi.yaml`).

## Tecnologías

- Java 17 o superior.
- Apache Maven para la compilación y gestión de dependencias.
- JDBC y SQL Server (`mssql-jdbc 13.2.1`).
- API REST mediante `com.sun.net.httpserver.HttpServer`.
- Jackson (`jackson-databind 2.17`) para serialización / deserialización JSON.
- JUnit 5 (`junit-jupiter 5.10.2`) para pruebas automatizadas.
- Swagger UI (CDN) + OpenAPI 3.0 para la documentación interactiva.
- Git y GitHub para el control de versiones (flujo Gitflow).

## Requisitos previos

- JDK 17 o superior.
- Apache Maven (o usar el wrapper `mvnw.cmd` incluido).
- SQL Server con la base de datos del proyecto configurada.
- Variables de entorno para la conexión a la base de datos.

## Configuración de la base de datos

La aplicación lee la conexión desde variables de entorno:

| Variable           | Descripción                          | Valor por defecto                                      |
|--------------------|--------------------------------------|--------------------------------------------------------|
| `TIENDA_DB_URL`    | URL JDBC de SQL Server               | `jdbc:sqlserver://DESKTOP-DAVV:1433;databaseName=tienda_virtual;...` |
| `TIENDA_DB_USER`   | Usuario de la base de datos          | `tienda_app`                                           |
| `TIENDA_DB_PASSWORD` | Contraseña del usuario             | *(sin valor predeterminado — obligatoria)*             |

Configura las variables en PowerShell antes de arrancar:

```powershell
$env:TIENDA_DB_USER     = "tienda_app"
$env:TIENDA_DB_PASSWORD = Read-Host "Contraseña de SQL Server" -AsSecureString | ConvertFrom-SecureString -AsPlainText
$env:TIENDA_DB_URL      = "jdbc:sqlserver://localhost:1433;databaseName=tienda_virtual;encrypt=true;trustServerCertificate=true;"
```

Inicializa el esquema y los datos de ejemplo ejecutando los scripts de `src/main/resources/sql/` en SQL Server:

```sql
-- 1. Crear tablas
-- src/main/resources/sql/schema.sql

-- 2. Insertar datos iniciales
-- src/main/resources/sql/data.sql
```

> **Nunca incluyas contraseñas en el repositorio.** Las credenciales se manejan exclusivamente a través de variables de entorno.

## Compilación

Desde la carpeta raíz del proyecto:

```powershell
.\mvnw.cmd clean verify
```

Si termina con `BUILD SUCCESS`, el proyecto compiló, ejecutó los 45 tests automatizados y está listo.

## Ejecución

```powershell
.\mvnw.cmd exec:java "-Dexec.mainClass=vista.VistaMain"
```

Cuando el servidor esté activo verás:

```
  Servicio de Tienda Virtual en puerto 8080
  Rutas disponibles:
    - Catalogo:   http://localhost:8080/api/productos
    - Inventario: http://localhost:8080/api/stock
    - Clientes:   http://localhost:8080/api/clientes
    - Pedidos:    http://localhost:8080/api/pedidos
    - Ventas:     http://localhost:8080/api/ventas
    - Swagger UI: http://localhost:8080/swagger
    - OpenAPI:    http://localhost:8080/api-docs/openapi.yaml
```

## Endpoints de la API

### Productos — `/api/productos`

| Método   | Parámetros / Cuerpo                          | Código | Descripción                             |
|----------|----------------------------------------------|--------|-----------------------------------------|
| `GET`    | `?id=PROD001` (opcional)                     | 200    | Lista todos los productos o uno por ID  |
| `POST`   | JSON con `producto_id`, `nombre`, `precio_base` | 201 | Crea un producto e inicializa su stock  |
| `PUT`    | `?id=PROD001` y/o `producto_id` en el cuerpo | 200    | Actualiza nombre, precio o estado       |
| `DELETE` | `?id=PROD001`                                | 204    | Elimina el producto y su stock asociado |

### Inventario — `/api/stock`

| Método | Parámetros / Cuerpo                          | Código | Descripción                             |
|--------|----------------------------------------------|--------|-----------------------------------------|
| `GET`  | `?producto_id=PROD001` (opcional)            | 200    | Consulta el inventario global o de uno  |
| `PUT`  | JSON con `producto_id` y `cantidad`          | 200    | Actualiza la cantidad disponible        |

### Clientes — `/api/clientes`

| Método | Parámetros / Cuerpo                                      | Código | Descripción                         |
|--------|----------------------------------------------------------|--------|-------------------------------------|
| `GET`  | `?id=CLI001` (opcional)                                  | 200    | Lista todos los clientes o uno por ID |
| `POST` | JSON con `cliente_id`, `nombre`, `apellido`, `email`     | 201    | Registra un nuevo cliente           |

### Pedidos — `/api/pedidos`

| Método | Parámetros                         | Código | Descripción                          |
|--------|------------------------------------|--------|--------------------------------------|
| `GET`  | `?id=PED_123` (opcional)           | 200    | Consulta pedidos o uno por ID        |

### Ventas — `/api/ventas`

| Método | Cuerpo JSON                                                     | Código | Descripción                                          |
|--------|-----------------------------------------------------------------|--------|------------------------------------------------------|
| `GET`  | —                                                               | 200    | Historial de ventas                                  |
| `POST` | `cliente_id`, `direccion_envio`, `detalles: [{producto_id, cantidad}]` | 201 | Procesa una venta transaccional con descuento de stock |

### Documentación

| URL                         | Descripción                       |
|-----------------------------|-----------------------------------|
| `/swagger`                  | Swagger UI interactivo            |
| `/api-docs/openapi.yaml`    | Especificación OpenAPI 3.0 (YAML) |

## Códigos HTTP de respuesta

| Código | Significado                                       |
|--------|---------------------------------------------------|
| 200    | Consulta o actualización exitosa                  |
| 201    | Recurso creado exitosamente                       |
| 204    | Eliminación exitosa (sin cuerpo de respuesta)     |
| 400    | Solicitud mal formada o datos inválidos           |
| 404    | Recurso no encontrado                             |
| 405    | Método HTTP no permitido en esa ruta              |
| 409    | Conflicto: duplicado, stock insuficiente o producto inactivo |
| 500    | Error inesperado del servidor                     |

## Ejemplos de peticiones

```powershell
# Listar productos
Invoke-RestMethod -Uri "http://localhost:8080/api/productos" -Method Get

# Crear producto
Invoke-RestMethod -Uri "http://localhost:8080/api/productos" -Method Post `
  -ContentType "application/json" `
  -Body '{"producto_id":"PROD010","nombre":"Webcam HD","precio_base":180000,"cantidad_inicial":20}'

# Registrar venta con múltiples productos
Invoke-RestMethod -Uri "http://localhost:8080/api/ventas" -Method Post `
  -ContentType "application/json" `
  -Body '{"cliente_id":"CLI001","direccion_envio":"Calle 100 # 15-30","detalles":[{"producto_id":"PROD001","cantidad":2}]}'

# Consultar inventario de un producto
Invoke-RestMethod -Uri "http://localhost:8080/api/stock?producto_id=PROD001" -Method Get

# Eliminar producto (respuesta 204 sin cuerpo)
Invoke-RestMethod -Uri "http://localhost:8080/api/productos?id=PROD010" -Method Delete
```

También puedes usar `api.http` (para IDEs con soporte REST) o `pruebas-api.html` desde el navegador.

## Pruebas automatizadas

El proyecto incluye **45 pruebas JUnit 5** distribuidas en:

| Clase de prueba            | Pruebas | Cubre                                               |
|----------------------------|---------|-----------------------------------------------------|
| `ClienteServiceTest`       | 6       | Registro, búsqueda, duplicados, validación de email |
| `ProductoServiceTest`      | 11      | CRUD completo, precio inválido, ID duplicado        |
| `StockServiceTest`         | 7       | Consulta, actualización, descuento de stock         |
| `VentaServiceTest`         | 7       | Venta exitosa, cliente/producto inexistente, stock insuficiente |
| `RestApiIntegrationTest`   | 14      | Endpoints HTTP reales: productos, stock, clientes, pedidos, ventas, Swagger |

Ejecutar todas las pruebas:

```powershell
.\mvnw.cmd test
```

## Control de versiones (Gitflow)

El proyecto sigue un flujo Gitflow:

- **`main`**: rama estable, solo recibe merges de `develop` revisados.
- **`develop`**: rama de integración continua.
- **`feature/*`**: una rama por funcionalidad o tarea.

Flujo de trabajo:

```bash
git checkout develop
git pull origin develop
git checkout -b feature/nombre-del-cambio

# ... realizar cambios y probarlos ...

git add .
git commit -m "tipo(alcance): descripción concisa"
git push -u origin feature/nombre-del-cambio
# Abrir Pull Request hacia develop
```

Ramas integradas hasta ahora:
- `feature/layer-separation-solid` — separación de capas y SOLID
- `feature/rest-endpoints-handlers` — handlers REST, `ClienteHandler`, `PedidoHandler`, códigos HTTP
- `feature/swagger-openapi` — documentación OpenAPI y Swagger UI
- `feature/automated-tests` — 45 pruebas JUnit 5
- `feature/clean-code-and-docs` — clean code, corrección de bugs, README
