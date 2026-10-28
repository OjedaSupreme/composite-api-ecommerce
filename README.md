# E-Commerce Gateway & Dashboard — Composite API

Práctica de la **Unidad 3 de Desarrollo Web Integral** (Universidad Tecnológica de Chihuahua, IDGS).
Autor: **Jesús Gerardo Ojeda Martínez**, grupo IDGS81N.

Un cliente web que muestra el detalle de un producto necesitaba hacer tres peticiones separadas
(catálogo, inventario y tipo de cambio). Aquí una **Composite API** (API Aggregator) recibe una sola
petición, consulta los tres servicios **en paralelo** y devuelve un único JSON consolidado.

```
Navegador / Postman
        │  GET /api/v1/product-summary/{id}
        ▼
┌──────────────────────────┐   Mono.zip (en paralelo)
│  composite-api  :8080    │──────────────┬───────────────────────────┐
└──────────────────────────┘              │                           │
        ▼                                 ▼                           ▼
catalog-service :8081          inventory-service :8082     Frankfurter API (externa)
/microservices/products/{id}   /microservices/inventory/{id}  └─ respaldo: open.er-api.com
```

## Tecnologías

| Componente | Tecnología |
|---|---|
| Lenguaje | Java 17 |
| Framework | Spring Boot 3.3.5 + Spring WebFlux (Reactor) |
| Cliente HTTP | `WebClient` sobre Reactor Netty |
| Build | Maven (proyecto multi-módulo) |
| API externa | [Frankfurter](https://api.frankfurter.app) — respaldo [ExchangeRate-API](https://open.er-api.com) |
| Frontend | Dashboard HTML/JS servido por la Composite API |
| Pruebas | Postman (colección en `postman/`) |

## Estructura

```
composite-api-ecommerce/
├── pom.xml                     # POM padre (multi-módulo)
├── catalog-service/            # Microservicio 1 - Catálogo        (puerto 8081)
├── inventory-service/          # Microservicio 2 - Inventario      (puerto 8082)
├── composite-api/              # API agregadora + dashboard        (puerto 8080)
│   └── src/main/java/mx/edu/utch/composite/
│       ├── client/             # CatalogClient, InventoryClient, ExchangeRateClient
│       ├── config/             # WebClient, CORS, propiedades
│       ├── dto/                # Contratos de entrada/salida
│       ├── exception/          # Manejo global de errores
│       ├── service/            # Orquestación con Mono.zip
│       └── controller/         # /api/v1/product-summary/{id}
├── postman/                    # Colección de pruebas
└── scripts/                    # Scripts de ejecución y publicación
```

## Requisitos

- JDK 17 o superior
- Maven 3.9+ (si no está instalado, `scripts/iniciar-servicios.ps1` descarga una copia portable)
- Conexión a internet (para la API de tipo de cambio)

## Cómo ejecutar

### Opción A — script (Windows)

```powershell
powershell -ExecutionPolicy Bypass -File scripts\iniciar-servicios.ps1
```

Compila los tres módulos y abre una ventana por servicio.

### Opción B — manual (cualquier sistema)

```bash
mvn clean install -DskipTests
# en tres terminales distintas:
mvn -pl catalog-service spring-boot:run
mvn -pl inventory-service spring-boot:run
mvn -pl composite-api spring-boot:run
```

Luego abre **http://localhost:8080** para ver el dashboard.

## Endpoints

| Servicio | Método | Endpoint | Descripción |
|---|---|---|---|
| Catálogo | GET | `http://localhost:8081/microservices/products` | Lista de productos |
| Catálogo | GET | `http://localhost:8081/microservices/products/{id}` | Detalle y precio base USD |
| Inventario | GET | `http://localhost:8082/microservices/inventory/{productId}` | Stock, almacén y entrega |
| Inventario | POST | `http://localhost:8082/microservices/inventory/chaos?enabled=true` | Simula caída (pruebas) |
| Externa | GET | `https://api.frankfurter.app/latest?from=USD&to=MXN` | Tipo de cambio USD/MXN |
| **Composite** | GET | `http://localhost:8080/api/v1/product-summary/{id}` | **Resumen consolidado** |
| Composite | GET | `http://localhost:8080/api/v1/products/{id}/summary` | Ruta equivalente |
| Composite | GET | `http://localhost:8080/api/v1/exchange-rate` | Tipo de cambio normalizado |

Productos de prueba: `P-101` a `P-105` (`P-104` está agotado y `P-102` tiene pocas unidades).

### Respuesta consolidada (ejemplo)

```json
{
  "product_id": "P-101",
  "title": "Laptop Gamer XYZ",
  "price_usd": 1200.00,
  "price_mxn": 22104.00,
  "stock_status": "IN_STOCK",
  "available_units": 15,
  "currency_conversion_source": "Frankfurter External API",
  "status": "SUCCESS",
  "description": "...",
  "exchange_rate": 18.42,
  "warehouse": "CEDIS Chihuahua",
  "estimated_delivery_days": 2,
  "warnings": [],
  "response_time_ms": 245
}
```

## Manejo de errores y resiliencia

| Escenario | Comportamiento | HTTP |
|---|---|---|
| Todo responde | `status: SUCCESS` | 200 |
| Producto inexistente | `error_code: PRODUCT_NOT_FOUND` | 404 |
| Catálogo caído | `error_code: DEPENDENCY_UNAVAILABLE` (sin catálogo no hay resumen) | 503 |
| Inventario caído o lento | `status: PARTIAL_SUCCESS`, `stock_status: UNKNOWN` + advertencia | 200 |
| Frankfurter caída | Se usa ExchangeRate-API como respaldo | 200 |
| Ambas APIs de divisas caídas | Último tipo de cambio en caché; si no hay, `price_mxn: null` + advertencia | 200 |

Cada cliente tiene *timeout* (2 s internos, 4 s externos) y un reintento con *backoff* de 200 ms.

## Flujo de trabajo Git (Gitflow)

- `main`: versiones liberadas (tag `v1.0.0`)
- `develop`: integración
- `feature/catalog-service`, `feature/inventory-service`, `feature/composite-api`, `feature/dashboard`, `feature/docs`
- `release/1.0.0`: preparación de la entrega

Commits con convención *Conventional Commits* (`feat:`, `docs:`, `chore:`).
