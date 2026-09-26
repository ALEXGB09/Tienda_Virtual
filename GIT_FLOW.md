# Estrategia de Ramas Git Flow / GitLab Flow - Tienda_Virtual

Este proyecto implementa la estrategia de control de versiones **Git Flow / GitLab Flow** para garantizar la estabilidad de la rama principal de producción y un flujo estructurado de desarrollo n-capas.

## Estructura de Ramas Principal

1. **`main` / `master`**: 
   - Contiene únicamente código estable y en estado de producción.
   - Cada entrega a `main` lleva una etiqueta de versión (ejemplo: `v1.0.0`).

2. **`develop`**: 
   - Rama principal de integración para el desarrollo continuo.
   - Refleja los últimos cambios integrados de las características desarrolladas.

## Ramas de Soporte / Funcionalidades (`feature/*`)

Cada requerimiento del sistema se trabaja en su propia rama de característica derivada de `develop`:

- `feature/modelos-entidades`:
  - Implementación de las clases del modelo E-R (`Cliente`, `Producto`, `Stock`, `Pedido`, `Pago`, etc.).
- `feature/persistencia-json`:
  - Implementación de la capa `data_access` y manejo de serialización/deserialización con `Gson`.
- `feature/controladores-negocio`:
  - Implementación de la capa `controller` y las reglas de negocio (procesamiento de ventas y descuento automático de inventario en `Stock`).
- `feature/api-rest`:
  - Implementación del servidor REST API HTTP en la capa `api` con soporte para `GET`, `POST`, `PUT`, `DELETE`.
- `feature/interfaz-vista`:
  - Implementación del menú CLI en la capa `vista`.

## Flujo de Trabajo (Comandos de Referencia)

```bash
# 1. Crear y cambiarse a una rama feature desde develop
git checkout develop
git checkout -b feature/modelos-entidades

# 2. Realizar commits de cambios
git add .
git commit -m "feat(model): agregar entidades E-R del sistema"

# 3. Fusionar de vuelta a develop tras aprobación
git checkout develop
git merge --no-ff feature/modelos-entidades

# 4. Preparar versión de producción en main
git checkout main
git merge --no-ff develop
git tag -a v1.0.0 -m "Versión 1.0.0 - Lanzamiento Tienda Virtual"
```
