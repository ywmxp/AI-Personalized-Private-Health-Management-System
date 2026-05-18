$ErrorActionPreference = "Stop"

$RootDir = Split-Path -Parent $PSScriptRoot
Set-Location $RootDir

if (-not (Test-Path ".env")) {
  Copy-Item ".env.example" ".env"
  Write-Host "Created .env from .env.example"
}

Write-Host "Starting database..."
docker compose up -d

Write-Host "Start backend in another terminal:"
Write-Host "  Set-Location `"$RootDir\backend`"; .\mvnw.cmd spring-boot:run"

Write-Host "Start frontend in another terminal:"
Write-Host "  Set-Location `"$RootDir\frontend`"; npm run dev"

Write-Host "Use this script as an entry guide. It does not spawn extra terminals automatically."
