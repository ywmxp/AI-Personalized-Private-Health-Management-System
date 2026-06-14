#!/usr/bin/env bash

set -euo pipefail

ROOT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")/.." && pwd)"

if [[ ! -f "$ROOT_DIR/.env" ]]; then
  cp "$ROOT_DIR/.env.example" "$ROOT_DIR/.env"
  echo "Created .env from .env.example"
fi

cd "$ROOT_DIR"

# Load env vars for AI service host/port (only reads lines without export)
AI_SERVICE_HOST=$(grep -E '^AI_SERVICE_HOST=' "$ROOT_DIR/.env" | cut -d'=' -f2-)
AI_SERVICE_PORT=$(grep -E '^AI_SERVICE_PORT=' "$ROOT_DIR/.env" | cut -d'=' -f2-)
AI_SERVICE_HOST="${AI_SERVICE_HOST:-127.0.0.1}"
AI_SERVICE_PORT="${AI_SERVICE_PORT:-8000}"

VENV_PYTHON="$ROOT_DIR/backend/.venv/bin/python"
if [[ ! -f "$VENV_PYTHON" ]]; then
  echo "Creating Python virtual environment..."
  python3 -m venv "$ROOT_DIR/backend/.venv"
fi

echo "Installing AI service dependencies..."
"$VENV_PYTHON" -m pip install -q -r "$ROOT_DIR/backend/src/main/python/requirements.txt"

echo "Starting AI service on ${AI_SERVICE_HOST}:${AI_SERVICE_PORT}..."
cd "$ROOT_DIR/backend/src/main/python"
"$VENV_PYTHON" main.py
