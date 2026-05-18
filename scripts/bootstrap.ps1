$ErrorActionPreference = "Stop"

$RootDir = Split-Path -Parent $PSScriptRoot
Set-Location $RootDir

if (-not (Test-Path ".env")) {
  Copy-Item ".env.example" ".env"
  Write-Host "Created .env from .env.example"
}

Write-Host "Installing frontend dependencies..."
Set-Location "$RootDir/frontend"
npm install

Write-Host "Bootstrap complete."
Write-Host "Next steps:"
Write-Host "  1. Review .env if local ports or credentials need changes"
Write-Host "  2. Start database: docker compose up -d"
Write-Host "  3. Start backend: Set-Location backend; .\mvnw.cmd spring-boot:run"
Write-Host "  4. Start frontend: Set-Location frontend; npm run dev"
