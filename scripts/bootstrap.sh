#!/usr/bin/env bash

set -euo pipefail

ROOT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")/.." && pwd)"

cd "$ROOT_DIR"

if [[ ! -f ".env" ]]; then
  cp .env.example .env
  echo "Created .env from .env.example"
fi

echo "Installing frontend dependencies..."
cd "$ROOT_DIR/frontend"
npm install

echo "Bootstrap complete."
echo "Next steps:"
echo "  1. Review .env if local ports or credentials need changes"
echo "  2. Start database: docker compose up -d"
echo "  3. Start backend: cd backend && ./mvnw spring-boot:run"
echo "  4. Start frontend: cd frontend && npm run dev"
