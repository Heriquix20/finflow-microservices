$ErrorActionPreference = "Stop"
$PSNativeCommandUseErrorActionPreference = $false

$projectRoot = Split-Path -Parent $MyInvocation.MyCommand.Path

Write-Host "Subindo infraestrutura Docker..."
Set-Location $projectRoot
docker compose up -d --build

if ($LASTEXITCODE -ne 0) {
    Write-Error "Nao foi possivel subir a infraestrutura Docker. Verifique se o Docker Desktop esta em execucao."
    exit $LASTEXITCODE
}

$services = @(
    @{ Name = "finflow-auth"; Module = "finflow-auth" },
    @{ Name = "finflow-gateway"; Module = "finflow-gateway" },
    @{ Name = "finflow-income"; Module = "finflow-income" },
    @{ Name = "finflow-expense"; Module = "finflow-expense" },
    @{ Name = "finflow-reports"; Module = "finflow-reports" }
)

Write-Host "Abrindo finflow-discovery ..."
Start-Process powershell -ArgumentList @(
    "-NoExit",
    "-Command",
    "Set-Location '$projectRoot'; mvn -pl finflow-discovery spring-boot:run"
)

Write-Host "Aguardando o Eureka inicializar..."
Start-Sleep -Seconds 8

foreach ($service in $services) {
    $module = $service.Module
    $name = $service.Name

    Write-Host "Abrindo $name ..."
    Start-Process powershell -ArgumentList @(
        "-NoExit",
        "-Command",
        "Set-Location '$projectRoot'; mvn -pl $module spring-boot:run"
    )
}
