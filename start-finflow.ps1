$ErrorActionPreference = "Stop"
$PSNativeCommandUseErrorActionPreference = $false

$projectRoot = Split-Path -Parent $MyInvocation.MyCommand.Path

function Wait-HttpReady {
    param(
        [string]$Uri,
        [int]$TimeoutSeconds = 90
    )

    $deadline = (Get-Date).AddSeconds($TimeoutSeconds)

    while ((Get-Date) -lt $deadline) {
        try {
            $response = Invoke-WebRequest -Uri $Uri -UseBasicParsing -TimeoutSec 5
            if ($response.StatusCode -ge 200 -and $response.StatusCode -lt 500) {
                return
            }
        } catch {
            if ($_.Exception.Response -and [int]$_.Exception.Response.StatusCode -ge 400 -and [int]$_.Exception.Response.StatusCode -lt 500) {
                return
            }
        }

        Start-Sleep -Seconds 2
    }

    throw "Timed out while waiting for $Uri to become available."
}

function Wait-GatewayRouteReady {
    param(
        [string]$Uri,
        [int]$TimeoutSeconds = 90
    )

    $deadline = (Get-Date).AddSeconds($TimeoutSeconds)

    while ((Get-Date) -lt $deadline) {
        try {
            Invoke-WebRequest -Uri $Uri -Method GET -UseBasicParsing -TimeoutSec 5 | Out-Null
            return
        } catch {
            if ($_.Exception.Response) {
                $statusCode = [int]$_.Exception.Response.StatusCode
                if ($statusCode -ne 503) {
                    return
                }
            }
        }

        Start-Sleep -Seconds 2
    }

    throw "Timed out while waiting for gateway route $Uri to become available."
}

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
Wait-HttpReady -Uri "http://localhost:8761"

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

Write-Host "Aguardando os servicos do backend ficarem disponiveis..."
Wait-HttpReady -Uri "http://localhost:8084/actuator/health"
Wait-HttpReady -Uri "http://localhost:8081/actuator/health"
Wait-HttpReady -Uri "http://localhost:8082/actuator/health"
Wait-HttpReady -Uri "http://localhost:8083/actuator/health"
Wait-HttpReady -Uri "http://localhost:8761/eureka/apps/FINFLOW-AUTH"
Wait-HttpReady -Uri "http://localhost:8761/eureka/apps/FINFLOW-GATEWAY"
Wait-HttpReady -Uri "http://localhost:8761/eureka/apps/FINFLOW-INCOME"
Wait-HttpReady -Uri "http://localhost:8761/eureka/apps/FINFLOW-EXPENSE"
Wait-HttpReady -Uri "http://localhost:8761/eureka/apps/FINFLOW-REPORTS"
Wait-HttpReady -Uri "http://localhost:8080/actuator/health"
Wait-GatewayRouteReady -Uri "http://localhost:8080/api/auth/login"
