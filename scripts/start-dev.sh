#!/usr/bin/env bash

set -euo pipefail

ROOT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")/.." && pwd)"

if [[ ! -f "$ROOT_DIR/.env" ]]; then
  cp "$ROOT_DIR/.env.example" "$ROOT_DIR/.env"
  echo "Created .env from .env.example"
fi

echo "Starting database..."
cd "$ROOT_DIR"
docker compose up -d

echo "Starting backend on a new terminal tab/window manually:"
echo "  cd \"$ROOT_DIR/backend\" && ./mvnw spring-boot:run"

echo "Starting frontend on a new terminal tab/window manually:"
echo "  cd \"$ROOT_DIR/frontend\" && npm run dev"

echo "Use this script as an entry guide. It does not spawn background terminals automatically."
