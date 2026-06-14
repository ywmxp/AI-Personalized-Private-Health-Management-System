$ErrorActionPreference = "Stop"

$RootDir = Split-Path -Parent $PSScriptRoot
Set-Location $RootDir

if (-not (Test-Path ".env")) {
  Copy-Item ".env.example" ".env"
  Write-Host "Created .env from .env.example"
}

$envContent = Get-Content "$RootDir\.env" -Raw
$aiHost = if ($envContent -match '(?m)^AI_SERVICE_HOST=(.+)$') { $Matches[1] } else { "127.0.0.1" }
$aiPort = if ($envContent -match '(?m)^AI_SERVICE_PORT=(.+)$') { $Matches[1] } else { "8000" }

$venvPython = "$RootDir\backend\.venv\Scripts\python.exe"
if (-not (Test-Path $venvPython)) {
  Write-Host "Creating Python virtual environment..."
  python -m venv "$RootDir\backend\.venv"
}

Write-Host "Installing AI service dependencies..."
& $venvPython -m pip install -q -r "$RootDir\backend\src\main\python\requirements.txt"

Write-Host "Starting AI service on ${aiHost}:${aiPort}..."
Set-Location "$RootDir\backend\src\main\python"
& $venvPython main.py
