<#
  Crea el historial Git siguiendo Gitflow y lo publica en GitHub.
    main      -> versiones liberadas (tag v1.0.0)
    develop   -> integracion
    feature/* -> una rama por funcionalidad, integrada a develop con merge --no-ff
    release/1.0.0 -> estabilizacion antes de pasar a main
  Uso:  powershell -ExecutionPolicy Bypass -File scripts\publicar-github.ps1
#>
param([string]$RepoName = "composite-api-ecommerce")
$ErrorActionPreference = "Continue"
$root = Resolve-Path (Join-Path $PSScriptRoot "..")
Set-Location $root

function G { & git.exe @args; if ($LASTEXITCODE -ne 0) { throw "git $args fallo" } }
function Commit($paths, $msg) { G add -- @paths; G commit -q -m $msg; Write-Host "  commit: $msg" -ForegroundColor DarkGray }
function Feature($name, [scriptblock]$body, $mergeMsg) {
    G checkout -q -b "feature/$name" develop
    & $body
    G checkout -q develop
    G merge -q --no-ff "feature/$name" -m $mergeMsg
    Write-Host "Integrada feature/$name en develop" -ForegroundColor Cyan
}

if (Test-Path ".git") { throw "Este folder ya tiene un repositorio Git (.git). Eliminalo si quieres regenerar el historial." }

G init -q -b main
if (-not (git config user.name))  { G config user.name  "Jesus Gerardo Ojeda Martinez" }
if (-not (git config user.email)) { G config user.email "jgeraojeda@gmail.com" }

# --- main: estructura base -------------------------------------------------
Commit @("pom.xml", ".gitignore") "chore: estructura base del proyecto Maven multi-modulo"
G checkout -q -b develop

# --- feature/catalog-service ----------------------------------------------
Feature "catalog-service" {
    Commit @("catalog-service/pom.xml", "catalog-service/src/main/java/mx/edu/utch/catalog/CatalogServiceApplication.java", "catalog-service/src/main/resources") "feat(catalog): crear microservicio de catalogo en el puerto 8081"
    Commit @("catalog-service/src/main/java/mx/edu/utch/catalog/model", "catalog-service/src/main/java/mx/edu/utch/catalog/repository") "feat(catalog): modelo Product y repositorio en memoria con productos semilla"
    Commit @("catalog-service") "feat(catalog): endpoints GET /microservices/products y /{id} con manejo de 404"
} "Merge branch 'feature/catalog-service' into develop"

# --- feature/inventory-service --------------------------------------------
Feature "inventory-service" {
    Commit @("inventory-service/pom.xml", "inventory-service/src/main/java/mx/edu/utch/inventory/InventoryServiceApplication.java", "inventory-service/src/main/resources") "feat(inventory): crear microservicio de inventario en el puerto 8082"
    Commit @("inventory-service/src/main/java/mx/edu/utch/inventory/model", "inventory-service/src/main/java/mx/edu/utch/inventory/repository") "feat(inventory): modelo de stock, almacen y regla IN_STOCK/LOW_STOCK/OUT_OF_STOCK"
    Commit @("inventory-service") "feat(inventory): endpoint GET /microservices/inventory/{productId} y simulador de fallas"
} "Merge branch 'feature/inventory-service' into develop"

# --- feature/composite-api ------------------------------------------------
$c = "composite-api/src/main/java/mx/edu/utch/composite"
Feature "composite-api" {
    Commit @("composite-api/pom.xml", "$c/CompositeApiApplication.java", "$c/config", "composite-api/src/main/resources/application.yml") "feat(composite): configuracion de WebClient con timeouts por servicio"
    Commit @("$c/dto") "feat(composite): DTOs de los microservicios y contrato consolidado ProductSummaryResponse"
    Commit @("$c/exception") "feat(composite): excepciones y GlobalExceptionHandler con respuestas JSON uniformes"
    Commit @("$c/client/CatalogClient.java", "$c/client/InventoryClient.java") "feat(composite): clientes de catalogo e inventario con timeout y reintento"
    Commit @("$c/client/ExchangeRateClient.java") "feat(composite): consumo de Frankfurter API con fallback a ExchangeRate-API y cache"
    Commit @("$c/service", "$c/controller") "feat(composite): orquestacion en paralelo con Mono.zip y endpoint /api/v1/product-summary/{id}"
} "Merge branch 'feature/composite-api' into develop"

# --- feature/dashboard ----------------------------------------------------
Feature "dashboard" {
    Commit @("composite-api/src/main/resources/static") "feat(dashboard): interfaz web que consume la Composite API"
} "Merge branch 'feature/dashboard' into develop"

# --- feature/docs ---------------------------------------------------------
Feature "docs" {
    Commit @("README.md", "postman", "scripts") "docs: README con instrucciones, coleccion de Postman y scripts de ejecucion"
    $rest = git status --porcelain
    if ($rest) { G add -A; G commit -q -m "docs: evidencias y documentacion adicional" }
} "Merge branch 'feature/docs' into develop"

# --- release/1.0.0 -> main ------------------------------------------------
G checkout -q -b release/1.0.0 develop
G checkout -q main
G merge -q --no-ff release/1.0.0 -m "Release 1.0.0: Composite API E-Commerce"
G tag -a v1.0.0 -m "Version 1.0.0 - entrega de la practica"
G checkout -q develop
G merge -q --no-ff main -m "Merge release 1.0.0 back into develop"
G checkout -q main
Write-Host "`nHistorial Gitflow creado:" -ForegroundColor Green
git log --oneline --graph --all -n 40

# --- Publicar en GitHub ----------------------------------------------------
if (-not (Get-Command gh -ErrorAction SilentlyContinue)) {
    Write-Host "Instalando GitHub CLI con winget..." -ForegroundColor Yellow
    winget install --id GitHub.cli -e --accept-source-agreements --accept-package-agreements
    $env:Path = [System.Environment]::GetEnvironmentVariable("Path", "Machine") + ";" + [System.Environment]::GetEnvironmentVariable("Path", "User")
}
gh auth status *> $null
if ($LASTEXITCODE -ne 0) {
    Write-Host "Inicia sesion en GitHub en el navegador que se abrira..." -ForegroundColor Yellow
    gh auth login --hostname github.com --git-protocol https --web
}
gh repo create $RepoName --public --source . --remote origin --description "Composite API (API Aggregator) con Spring Boot WebFlux: catalogo, inventario y tipo de cambio - UTCH Desarrollo Web Integral U3"
G push -u origin main develop
G push origin "feature/catalog-service" "feature/inventory-service" "feature/composite-api" "feature/dashboard" "feature/docs" "release/1.0.0"
G push origin --tags
$url = gh repo view --json url -q .url
Write-Host "`nRepositorio publicado: $url" -ForegroundColor Green
New-Item -ItemType Directory -Force -Path (Join-Path $root "logs") | Out-Null
$url | Out-File (Join-Path $root "logs\repo-url.txt") -Encoding utf8 -Force -ErrorAction SilentlyContinue
