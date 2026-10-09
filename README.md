# Tienda Virtual — API REST

Proyecto académico de una tienda virtual implementado en **Java puro** con
arquitectura **n-capas** y persistencia dual (**en memoria** / **MySQL JDBC**).

## Arquitectura


## Tecnologías

- **Java 17+** (usa `com.sun.net.httpserver.HttpServer` del JDK)
- **JDBC** con `PreparedStatement` (SQL puro, sin ORM)
- **MySQL 8** (opcional; hay DAO en memoria para pruebas)
- **Git** con estrategia Git Flow

## Requisitos

- JDK 17 o superior
- (Opcional) MySQL 8 + conector `mysql-connector-j-8.x.x.jar`

## Estructura del repositorio


## Compilación

**PowerShell:**
```powershell
javac -d bin (Get-ChildItem -Recurse -Filter *.java).FullName

CDM/BASH
javac -d bin model/*.java data_access/*.java data_access/dao/*.java data_access/impl/*.java controller/*.java api/*.java vista/*.java

EJECUCIÓN:
Aplicación principal (consola + API en :8080):
java -cp bin vista.VistaMain

Prueba automática de verificación (API en :8085):
java -cp bin vista.TestVerification


Endpoints de la API REST
Base URL por defecto: http://localhost:8080/api

Método	Endpoint	Descripción
GET	/api/productos	Lista todos los productos
GET	/api/productos/{id}	Obtiene un producto por ID
POST	/api/productos	Crea un producto
PUT	/api/productos/{id}	Actualiza un producto
DELETE	/api/productos/{id}	Elimina un producto
GET	/api/stock	Lista el inventario
GET	/api/pedidos	Historial de pedidos
POST	/api/ventas	Registra una venta (descuenta inventario)

Ejemplos
*Listar productos:

curl http://localhost:8080/api/productos

*Crear producto:

bash
curl -X POST http://localhost:8080/api/productos \
  -H "Content-Type: application/json" \
  -d '{"producto_id":"PROD010","nombre":"Webcam HD","descripcion":"1080p","precio_base":180000,"estado_activo":true,"cantidad_inicial":20,"stock_minimo":3}'

  Flujo de trabajo:

bash
git checkout main
git pull
git checkout -b feature/mi-cambio
# ... cambios ...
git add .
git commit -m "feat: descripción del cambio"
git push -u origin feature/mi-cambio
# Pull Request → main

