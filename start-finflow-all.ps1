$ErrorActionPreference = "Stop"
$PSNativeCommandUseErrorActionPreference = $false

$projectRoot = Split-Path -Parent $MyInvocation.MyCommand.Path
$frontendRoot = Join-Path $projectRoot "finflow-frontend"

Write-Host "Iniciando stack completa do FinFlow..."

Write-Host "Subindo backend..."
try {
    & (Join-Path $projectRoot "start-finflow.ps1")
} catch {
    Write-Error "A inicializacao do backend falhou. Ajuste a infraestrutura e tente novamente."
    exit 1
}

if (-not (Test-Path $frontendRoot)) {
    Write-Host "Frontend nao encontrado em $frontendRoot"
    exit 1
}

Write-Host "Abrindo finflow-frontend ..."
Start-Process powershell -ArgumentList @(
    "-NoExit",
    "-Command",
    "Set-Location '$frontendRoot'; npm run dev"
)

Write-Host "FinFlow completo iniciado."
Write-Host "Frontend: http://localhost:5173"
Write-Host "Gateway:  http://localhost:8080"
