#!/bin/bash
set -e

# This script is run by the Postgres container on startup if the DB is empty.
# Environment variables are passed from docker-compose.yml

if [ -n "$POSTGRES_DB" ]; then
    echo "Creating database: $POSTGRES_DB"
    # The DB is usually created by the default entrypoint script based on POSTGRES_DB,
    # but we can do extra setup here if needed.
fi

echo "Database initialization completed."
