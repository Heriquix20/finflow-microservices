function Stop-ServiceByPort {
    param(
        [int]$Port,
        [string]$Name
    )

    try {
        $connection = Get-NetTCPConnection -LocalPort $Port -State Listen -ErrorAction SilentlyContinue |
            Select-Object -First 1

        if ($null -ne $connection) {
            Write-Host "Encerrando $Name na porta $Port (PID $($connection.OwningProcess))..."
            Stop-Process -Id $connection.OwningProcess -Force -ErrorAction SilentlyContinue
        }
    } catch {
        Write-Host "Nao foi possivel encerrar $Name pela porta $Port."
    }
}

Write-Host "Parando frontend do FinFlow..."
Stop-ServiceByPort -Port 5173 -Name "finflow-frontend"

Write-Host "Parando backend do FinFlow..."
& (Join-Path (Split-Path -Parent $MyInvocation.MyCommand.Path) "stop-finflow.ps1")

Write-Host "FinFlow completo parado."
