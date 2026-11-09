# SENLA Binance Price Tracker

REST API для сбора и анализа цен торговой пары **BNBUSDT**. Приложение периодически получает среднюю цену через Binance API, сохраняет историю в PostgreSQL и предоставляет данные через HTTP.

**Технологии:** Java 21, Spring Boot 3.5, Gradle, PostgreSQL 17, Flyway, Docker Compose, JUnit 5, Mockito, Testcontainers.

## Запуск через Docker Compose

Требуется установленный Docker с поддержкой Compose.

1. Создайте в корне проекта файл `.env`:

   ```dotenv
   DB_PASSWORD=postgres
   ```

2. Запустите приложение и базу данных:

   ```bash
   docker compose -f docker-compose.yaml up --build -d
   ```

3. Просмотрите логи:

   ```bash
   docker compose -f docker-compose.yaml logs -f app
   ```

API доступно по адресу **http://localhost:8080**. PostgreSQL работает в контейнере; с хоста к нему можно подключиться по адресу `localhost:5434`. Таблицы создаются миграциями Flyway автоматически.

Остановка контейнеров:

```bash
docker compose -f docker-compose.yaml down
```

Данные сохраняются в Docker-томе `postgres_data`. Команда `docker compose -f docker-compose.yaml down -v` удалит том вместе с данными.

## Локальный запуск

Требуются JDK 21 и PostgreSQL с созданной базой `senla_test_task_db`. По умолчанию приложение подключается к `localhost:5433` с пользователем `postgres`.

```bash
./gradlew bootRun
```

Для изменения параметров подключения задайте переменные `SPRING_DATASOURCE_URL`, `SPRING_DATASOURCE_USERNAME` и `SPRING_DATASOURCE_PASSWORD`.

## Переменные окружения

| Переменная | Назначение | Значение |
|---|---|---|
| `DB_PASSWORD` | Пароль PostgreSQL для Docker Compose | Указывается в `.env` |
| `SPRING_DATASOURCE_URL` | JDBC-адрес БД | В Docker: `jdbc:postgresql://db:5432/senla_test_task_db` |
| `SPRING_DATASOURCE_USERNAME` | Пользователь БД | `postgres` |
| `SPRING_DATASOURCE_PASSWORD` | Пароль БД для Spring Boot | В Docker: значение `DB_PASSWORD` |
| `BINANCE_POLL_INTERVAL` | Интервал опроса Binance в формате ISO-8601 | `PT30S` — 30 секунд |

Другие примеры интервала: `PT1M` (1 минута), `PT5M` (5 минут). В `docker-compose.yaml` интервал задан через `BINANCE_POLL_INTERVAL`; при необходимости измените его в секции `app.environment`.

## REST API

Базовый адрес: `http://localhost:8080`. Даты передаются в формате `yyyy-MM-dd`. В примерах ниже приведены условные данные; фактические цены и время зависят от записей в БД.

### 1. Последняя цена — `GET /api/v1/price`

```bash
curl http://localhost:8080/api/v1/price
```

**Успешный ответ — HTTP 200:**

```json
{
  "symbol": "BNBUSDT",
  "price": "625.500000000000000",
  "fetchedAt": "2026-11-06T09:30:00Z"
}
```

**Если записей нет — HTTP 404:**

```json
{
  "status": 404,
  "message": "SymbolData not found"
}
```

### 2. История цен — `GET /api/v1/prices`

```bash
curl "http://localhost:8080/api/v1/prices?from=2026-11-01&to=2026-11-06&page=0&size=10"
```

`from` (включительно) и `to` (не включительно) обязательны, `from` должно быть раньше `to`; `page` и `size` необязательны (по умолчанию `0` и `10`).

**Успешный ответ — HTTP 200:**

```json
{
  "items": [
    {
      "symbol": "BNBUSDT",
      "price": "625.500000000000000",
      "fetchedAt": "2026-11-06T09:30:00Z"
    },
    {
      "symbol": "BNBUSDT",
      "price": "624.000000000000000",
      "fetchedAt": "2026-11-06T09:29:00Z"
    }
  ],
  "page": 0,
  "size": 10,
  "totalElements": 2,
  "hasNext": false
}
```

**Если за указанный период данных нет — HTTP 200:**

```json
{
  "items": [],
  "page": 0,
  "size": 10,
  "totalElements": 0,
  "hasNext": false
}
```

**Если `from` позже или равно `to` — HTTP 404:**

```json
{
  "status": 404,
  "message": "fromDate(2026-11-06) can't be after or equal toDate(2026-11-01)"
}
```

### 3. Средняя цена — `GET /api/v1/prices/average`

```bash
curl "http://localhost:8080/api/v1/prices/average?from=2026-11-01&to=2026-11-06"
```

**Успешный ответ — HTTP 200:**

```json
{
  "averagePrice": 624.750000000000000
}
```

**Если за период нет данных — HTTP 404:**

```json
{
  "status": 404,
  "message": "SymbolData not found"
}
```

При `from` позже или равно `to` возвращается такой же ответ об ошибке дат, как в пункте 2.

### 4. Тренд — `GET /api/v1/prices/trend`

```bash
curl "http://localhost:8080/api/v1/prices/trend?from=2026-11-01&to=2026-11-06"
```

**Успешный ответ — HTTP 200:**

```json
{
  "symbol": "BNBUSDT",
  "start_date": "2026-11-01",
  "end_date": "2026-11-06",
  "trend": "UPWARD"
}
```

Возможные значения `trend`: `UPWARD`, `DOWNWARD`, `FLAT`. Тренд определяется сравнением последней цены за период со средней ценой за тот же период.

**Если за период нет данных — HTTP 404:**

```json
{
  "status": 404,
  "message": "SymbolData not found"
}
```

При `from` позже или равно `to` возвращается ответ об ошибке дат, показанный в пункте 2.

## Тестирование

```bash
./gradlew test
```

Для интеграционных тестов с Testcontainers требуется запущенный Docker. Тестовый профиль отключает периодический опрос Binance.
