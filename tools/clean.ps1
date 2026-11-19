$ErrorActionPreference = "Stop"
$ProjectRoot = Split-Path -Parent $PSScriptRoot
$BuildDir = Join-Path $ProjectRoot "build"
if (Test-Path $BuildDir) {
    Remove-Item $BuildDir -Recurse -Force
}
Write-Host "EaglePad build output cleaned." -ForegroundColor Green
