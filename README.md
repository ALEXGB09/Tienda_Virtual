# Tienda Virtual — API REST

Proyecto académico de una tienda virtual desarrollado en Java, con arquitectura por capas, API REST y persistencia de datos mediante JDBC y SQL Server.

## Arquitectura

El proyecto organiza sus responsabilidades en diferentes capas:

- **Modelo (`model`):** representa las entidades del sistema, como productos, clientes, pedidos y stock.
- **Acceso a datos (`data_access`):** contiene las interfaces DAO y las implementaciones encargadas de consultar y modificar los datos.
- **Servicios (`service`):** contiene la lógica de negocio separada de los controladores.
- **Controladores (`controller`):** validan y coordinan las operaciones del negocio.
- **API REST (`api`):** recibe las solicitudes HTTP y devuelve respuestas, generalmente en formato JSON.
- **Vista (`vista`):** contiene la interfaz de consola y los componentes de inicio de la aplicación.
- **Recursos (`src/main/resources`):** contiene recursos del proyecto, incluidos los scripts SQL.

## Tecnologías

- Java 17 o superior.
- Apache Maven para la compilación y gestión de dependencias.
- JDBC y consultas SQL.
- Microsoft SQL Server.
- API REST mediante `com.sun.net.httpserver.HttpServer`.
- Git y GitHub para el control de versiones.

## Requisitos previos

- JDK 17 o superior.
- Apache Maven.
- SQL Server con la base de datos del proyecto configurada.
- Credenciales válidas para acceder a la base de datos.

## Compilación

Desde la carpeta raíz del proyecto, ejecuta:

```powershell
mvn clean verify
```

Si la compilación termina con `BUILD SUCCESS`, el proyecto se compiló y verificó correctamente.

## Configuración de la base de datos

La aplicación utiliza variables de entorno para configurar la conexión:

- `TIENDA_DB_URL`: URL JDBC de SQL Server.
- `TIENDA_DB_USER`: usuario de la base de datos.
- `TIENDA_DB_PASSWORD`: contraseña del usuario.

Ejemplo en PowerShell:

```powershell
$env:TIENDA_DB_USER = "tienda_app"
$env:TIENDA_DB_PASSWORD = Read-Host "Contraseña de SQL Server"
```

Si necesitas configurar una URL diferente, puedes establecer `TIENDA_DB_URL`:

```powershell
$env:TIENDA_DB_URL = "jdbc:sqlserver://localhost:1433;databaseName=tienda_virtual;encrypt=true;trustServerCertificate=true;"
```

Ajusta el servidor y la base de datos a tu instalación. No publiques contraseñas ni las incluyas en el repositorio.

## Ejecución

Desde la raíz del proyecto, inicia la aplicación con:

```powershell
mvn exec:java '-Dexec.mainClass=vista.VistaMain'
```

Cuando el servidor esté iniciado, la API estará disponible en:

`http://localhost:8080/api`

Las variables de entorno deben estar configuradas en la misma terminal desde la que se inicia Maven.

## Endpoints principales

| Método | Endpoint | Descripción |
|---|---|---|
| GET | `/api/productos` | Lista los productos. |
| GET | `/api/productos` | Consulta un producto por ID, según los parámetros admitidos por el controlador. |
| POST | `/api/productos` | Crea un producto. |
| PUT | `/api/productos` | Actualiza un producto. |
| DELETE | `/api/productos` | Elimina un producto. |
| GET | `/api/stock` | Consulta el inventario. |
| GET | `/api/pedidos` | Consulta los pedidos. |
| POST | `/api/ventas` | Registra una venta y gestiona el descuento de inventario. |

Las rutas y los parámetros exactos deben coincidir con los controladores implementados.

## Ejemplo de consulta

Listar los productos:

```powershell
Invoke-RestMethod -Uri "http://localhost:8080/api/productos" -Method Get
```

Para probar las operaciones de creación, actualización y eliminación, se puede utilizar la página `pruebas-api.html` incluida en el proyecto.

## Control de versiones

El proyecto utiliza ramas para organizar el desarrollo de funcionalidades y correcciones.

Ejemplo de flujo de trabajo:

```bash
git checkout main
git pull
git checkout -b feature/nombre-del-cambio

# Realizar los cambios y probarlos

git add .
git commit -m "Descripción del cambio"
git push -u origin feature/nombre-del-cambio
```

Cuando corresponda, los cambios se integran en `main` mediante un Pull Request.

## Pruebas

Antes de integrar cambios, se recomienda:

1. Ejecutar `mvn clean verify`.
2. Iniciar la aplicación.
3. Probar los endpoints de la API.
4. Verificar las operaciones CRUD.
5. Comprobar que los cambios se reflejen correctamente en la base de datos.
