#!/usr/bin/env bash
# set-env.sh – Load local environment variables for Spring Boot
# Usage: source ./set-env.sh

ENV_FILE="$(dirname "$0")/student-service/.env"

while IFS='=' read -r key value; do
  # Skip blank lines and comments
  [[ -z "$key" || "$key" == \#* ]] && continue
  export "$key"="$value"
  echo "  export $key"
done < "$ENV_FILE"

echo ""
echo "Environment variables loaded. You can now run the Spring Boot service."
