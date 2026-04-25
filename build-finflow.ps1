param(
    [switch]$Clean
)

$projectRoot = Split-Path -Parent $MyInvocation.MyCommand.Path

Set-Location $projectRoot

Write-Host "Executando build completo do FinFlow..."
Write-Host "Isso vai compilar, rodar todos os testes e validar a cobertura."

if ($Clean) {
    mvn clean verify
} else {
    mvn verify
}

if ($LASTEXITCODE -eq 0) {
    Write-Host "Build finalizado com sucesso."
} else {
    Write-Host "Build falhou."
    exit $LASTEXITCODE
}
