# URL Shortener

Сервис сокращения ссылок на Spring Boot + PostgreSQL

## Запуск

```
docker compose up --build
```

Приложение поднимется на `http://localhost:8080`

## Создать короткую ссылку

POST /api/v1/links

Content-Type: application/json

```json
{
"url": "https://example.com/some/very/long/path",
"alias": "my-alias",
"expiresAt": "2026-12-31T23:59:59Z"
}
```

- `alias` необязателен. Если не передан — код генерируется случайно
- `expiresAt` необязателен. Если не передан — ссылка вечная (`expires_at = null`)

## Перейти по ссылке

GET /{shortLink}


302 редирект на исходный URL, 404 если код не найден, 410 если срок истёк

## Примечание

После истечения срока действия строка остаётся в базе, повторное создание ссылки с тем же alias не допускается