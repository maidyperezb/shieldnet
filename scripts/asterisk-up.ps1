# Levanta Asterisk y el softphone de demostración en Docker detectando la IP LAN activa del equipo.
# Uso: powershell -ExecutionPolicy Bypass -File .\scripts\asterisk-up.ps1
$ErrorActionPreference = "Stop"

$projectRoot = Split-Path -Parent $PSScriptRoot
Set-Location $projectRoot

if (-not $env:ASTERISK_EXTERNAL_ADDRESS) {
    $defaultRoute = Get-NetRoute -DestinationPrefix "0.0.0.0/0" |
        Sort-Object RouteMetric |
        Select-Object -First 1

    if ($defaultRoute) {
        $lanAddress = Get-NetIPAddress -AddressFamily IPv4 -InterfaceIndex $defaultRoute.InterfaceIndex |
            Where-Object { $_.IPAddress -notlike "169.254.*" } |
            Select-Object -First 1

        if ($lanAddress) {
            $env:ASTERISK_EXTERNAL_ADDRESS = $lanAddress.IPAddress
        }
    }
}

if (-not $env:ASTERISK_EXTERNAL_ADDRESS) {
    $env:ASTERISK_EXTERNAL_ADDRESS = "127.0.0.1"
}

Write-Host "ASTERISK_EXTERNAL_ADDRESS=$env:ASTERISK_EXTERNAL_ADDRESS"
docker compose up -d --build

Write-Host ""
Write-Host "Estado del contenedor:"
docker compose ps
Start-Sleep -Seconds 5
Write-Host ""
Write-Host "Extensiones registradas:"
docker exec shieldnet-asterisk asterisk -rx "sip show peers"
