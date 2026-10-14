#!/bin/bash
#docker compose down
# Build all services
# docker compose up --build -d

# Build only api-gateway
docker compose up --build -d api-gateway
# Build only post-service
docker compose up --build -d post-service

echo "Waiting running Vault..."
sleep 3

if [ -f "./vault/secrets/unseal-key.txt" ]; then
  echo "Unsealing Vault..."
  # must exist file ./vault/secrets/unseal-key.txt
  docker exec -i vault vault operator unseal "$(cat ./vault/secrets/unseal-key.txt)"
  #echo "Restart my services..."
  #docker compose restart api-gateway
  #docker compose restart post-service
  echo "Done!"
else
  echo "File ./vault/secrets/unseal-key.txt not found!"
fi