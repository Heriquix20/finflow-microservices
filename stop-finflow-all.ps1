$projectRoot = Split-Path -Parent $MyInvocation.MyCommand.Path

Write-Host "Parando stack backend do FinFlow..."
& (Join-Path $projectRoot "stop-finflow.ps1")
Write-Host "FinFlow backend parado."
