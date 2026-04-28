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

Write-Host "Parando containers Docker do FinFlow..."
docker compose down

Write-Host "Encerrando processos do backend..."
Stop-ServiceByPort -Port 8761 -Name "finflow-discovery"
Stop-ServiceByPort -Port 8084 -Name "finflow-auth"
Stop-ServiceByPort -Port 8080 -Name "finflow-gateway"
Stop-ServiceByPort -Port 8081 -Name "finflow-income"
Stop-ServiceByPort -Port 8082 -Name "finflow-expense"
Stop-ServiceByPort -Port 8083 -Name "finflow-reports"

Write-Host "FinFlow parado."
