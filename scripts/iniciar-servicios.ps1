<#
  Inicia los 3 servicios del proyecto, cada uno en su propia ventana:
    - catalog-service    -> http://localhost:8081
    - inventory-service  -> http://localhost:8082
    - composite-api      -> http://localhost:8080  (dashboard en http://localhost:8080)
  Uso:  powershell -ExecutionPolicy Bypass -File scripts\iniciar-servicios.ps1
#>
$ErrorActionPreference = "Stop"
$ProgressPreference = "SilentlyContinue"
$root = Resolve-Path (Join-Path $PSScriptRoot "..")
Set-Location $root

# 1) Verificar Java 17+
try { $jv = (cmd /c "java -version 2>&1") -join " " } catch { $jv = "" }
if ($jv -notmatch 'version "(\d+)') { throw "No se encontro Java. Instala JDK 17 o superior." }
if ([int]$Matches[1] -lt 17) { throw "Se requiere Java 17+. Version detectada: $($Matches[1])" }
Write-Host "Java detectado: $($Matches[1])" -ForegroundColor Green

# 2) Localizar Maven (o descargar una copia portable en ~/.m2/portable-maven)
$mvn = (Get-Command mvn -ErrorAction SilentlyContinue).Source
if (-not $mvn) {
    $mvnVersion = "3.9.9"
    $mvnHome = Join-Path $env:USERPROFILE ".m2\portable-maven\apache-maven-$mvnVersion"
    if (-not (Test-Path "$mvnHome\bin\mvn.cmd")) {
        Write-Host "Maven no esta en el PATH; descargando Maven $mvnVersion portable..." -ForegroundColor Yellow
        $zip = Join-Path $env:TEMP "apache-maven-$mvnVersion-bin.zip"
        Invoke-WebRequest "https://archive.apache.org/dist/maven/maven-3/$mvnVersion/binaries/apache-maven-$mvnVersion-bin.zip" -OutFile $zip
        Expand-Archive $zip -DestinationPath (Split-Path $mvnHome) -Force
    }
    $mvn = "$mvnHome\bin\mvn.cmd"
}
Write-Host "Maven: $mvn" -ForegroundColor Green

# 3) Compilar todos los modulos
& $mvn -q -DskipTests install
if ($LASTEXITCODE -ne 0) { throw "La compilacion fallo." }

# 4) Levantar cada microservicio en su propia ventana
$services = @(
    @{ name = "catalog-service";   port = 8081 },
    @{ name = "inventory-service"; port = 8082 },
    @{ name = "composite-api";     port = 8080 }
)
foreach ($s in $services) {
    $jar = Get-ChildItem "$root\$($s.name)\target\$($s.name)-*.jar" | Where-Object { $_.Name -notlike "*original*" } | Select-Object -First 1
    Start-Process powershell -ArgumentList "-NoExit", "-Command", "`$Host.UI.RawUI.WindowTitle='$($s.name) :$($s.port)'; java -jar '$($jar.FullName)'"
    Write-Host "Iniciando $($s.name) en el puerto $($s.port)..." -ForegroundColor Cyan
    Start-Sleep -Seconds 4
}
Write-Host "`nListo. Dashboard: http://localhost:8080   Composite API: http://localhost:8080/api/v1/product-summary/P-101" -ForegroundColor Green
