#!/bin/bash
#Режим 1: Development (Инфраструктура в Docker, мои приложения локально через Gradle)
# В этом режиме не нужно указывать профили. Вы просто запускаете инфраструктуру командой:
docker compose up -d
# После этого вы можете запускать api-gateway и post-service прямо из IntelliJ IDEA (или через терминал ./gradlew bootRun),
# передавая необходимые переменные окружения (VAULT_TOKEN, пароли).
# Приложения подхватят стандартный application.yml и будут обращаться к инфраструктуре через localhost.

# Run by Gradle my application api-gateway:
# VAULT_TOKEN="hvs.xxx" ./gradlew api-gateway:bootRun
# VAULT_TOKEN="hvs.xxx" ./gradlew post-service:bootRun

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