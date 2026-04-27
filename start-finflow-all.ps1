$ErrorActionPreference = "Stop"

$projectRoot = Split-Path -Parent $MyInvocation.MyCommand.Path

Write-Host "Iniciando stack backend completa do FinFlow..."

try {
    & (Join-Path $projectRoot "start-finflow.ps1")
} catch {
    Write-Error "A inicializacao da stack backend falhou. Ajuste a infraestrutura e tente novamente."
    exit 1
}

Write-Host "FinFlow backend iniciado."
Write-Host "Eureka:  http://localhost:8761"
Write-Host "Auth:    http://localhost:8084"
Write-Host "Gateway: http://localhost:8080"
