#!/bin/bash
# Режим 2: Deployment (Всё в Docker: инфраструктура + мои приложения)
# Чтобы собрать образы и запустить абсолютно всё в изолированных контейнерах, активируйте профиль deployment
docker compose --profile deployment up --build -d
# В этом режиме Docker автоматически соберет api-gateway и post-service, применит профиль docker (application-docker.yml),
# переопределив localhost на внутренние сетевые имена (redis, vault, postgres-db, jaeger).

echo "Waiting running Vault..."
sleep 3

if [ -f "./vault/secrets/unseal-key.txt" ]; then
  echo "Unsealing Vault..."
  # must exist file ./vault/secrets/unseal-key.txt
  docker exec -i vault vault operator unseal "$(cat ./vault/secrets/unseal-key.txt)"
  echo "Done!"
else
  echo "File ./vault/secrets/unseal-key.txt not found!"
fi

# Show log of api-gateway in Docker container
# docker compose logs -f api-gateway

# Show log of post-gateway in Docker container
# docker compose logs -f post-service