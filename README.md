# SENLA Binance Price Tracker

REST API для сбора и анализа цен торговой пары **BNBUSDT**. Приложение периодически запрашивает среднюю цену через Binance API, сохраняет историю в PostgreSQL и предоставляет данные через HTTP.

**Технологии:** Java 21, Spring Boot 3.5, Gradle, PostgreSQL 17, Flyway, Docker Compose, JUnit 5, Mockito и Testcontainers.

## Запуск через Docker Compose

Для запуска требуется Docker с поддержкой Compose.

1. Укажите пароль PostgreSQL в файле `.env` в корне проекта:

   ```dotenv
   DB_PASSWORD=postgres
   ```

2. Соберите и запустите приложение с базой данных:

   ```bash
   docker compose -f docker-compose.yaml up --build -d
   ```

3. Проверьте логи приложения:

   ```bash
   docker compose -f docker-compose.yaml logs -f app
   ```

API будет доступно по адресу `http://localhost:8080`. PostgreSQL работает в отдельном контейнере и доступен с хоста по адресу `localhost:5434`. Схема базы данных создаётся автоматически миграциями Flyway.

Остановка контейнеров:

```bash
docker compose -f docker-compose.yaml down
```

Данные PostgreSQL сохраняются в Docker-томе `postgres_data`. Чтобы удалить контейнеры вместе с данными, выполните:

```bash
docker compose -f docker-compose.yaml down -v
```

## Локальный запуск

Для локального запуска требуются JDK 21 и PostgreSQL с созданной базой `senla_test_task_db`. По умолчанию приложение подключается к `localhost:5433` с пользователем `postgres`.

```bash
./gradlew bootRun
```

Параметры подключения можно изменить через переменные `SPRING_DATASOURCE_URL`, `SPRING_DATASOURCE_USERNAME` и `SPRING_DATASOURCE_PASSWORD`.

## Переменные окружения

| Переменная | Назначение | Значение по умолчанию или пример |
|---|---|---|
| `DB_PASSWORD` | Пароль PostgreSQL для Docker Compose | Указывается в `.env` |
| `SPRING_DATASOURCE_URL` | JDBC-адрес базы данных | `jdbc:postgresql://localhost:5433/senla_test_task_db` |
| `SPRING_DATASOURCE_USERNAME` | Пользователь базы данных | `postgres` |
| `SPRING_DATASOURCE_PASSWORD` | Пароль базы данных | `postgres` |
| `BINANCE_POLL_INTERVAL` | Интервал опроса Binance в формате ISO-8601 | В Docker Compose: `PT30S` |

Примеры интервалов: `PT30S` — 30 секунд, `PT1M` — одна минута, `PT5M` — пять минут.

## REST API

Базовый адрес API: `http://localhost:8080`.

### Общие требования к параметрам

- Параметры `from` и `to` обязательны для запросов истории, средней цены и тренда.
- Даты передаются в формате ISO `yyyy-MM-dd`, например `2026-11-09`.
- `from` включается в период, а `to` не включается: запрос с `from=2026-11-01&to=2026-11-06` выбирает записи с `2026-11-01T00:00:00Z` включительно до `2026-11-06T00:00:00Z`, не включая эту границу.
- Значение `from` должно быть строго раньше `to`. Одинаковые даты недопустимы.
- Параметр `page` должен быть целым неотрицательным числом. Значение по умолчанию — `0`.
- Параметр `size` должен быть целым числом от `1` до `100`. Значение по умолчанию — `10`.

### HTTP-коды

| Код | Значение |
|---|---|
| `200 OK` | Запрос успешно выполнен. Для истории цен пустой период возвращает пустой список. |
| `400 Bad Request` | Отсутствует обязательный параметр, дата или число имеют неверный формат, `from` позже или равно `to`, либо `page`/`size` выходят за допустимые границы. |
| `404 Not Found` | Подходящие данные отсутствуют для последней цены, средней цены или тренда. |
| `500 Internal Server Error` | Произошла непредвиденная внутренняя ошибка. |

Ошибки бизнес-валидации и отсутствия данных возвращаются в формате:

```json
{
  "status": 400,
  "message": "Описание ошибки"
}
```

Отсутствующий обязательный параметр, дата неверного формата и нечисловой `page` или `size` обрабатываются Spring Boot. Например, запрос без параметра `from` возвращает `400 Bad Request`:

```json
{
  "timestamp": "2026-11-09T09:30:00.000+00:00",
  "status": 400,
  "error": "Bad Request",
  "path": "/api/v1/prices"
}
```

Такую же структуру имеет ответ на неверный формат параметра, например `from=09-11-2026` или `page=first`. Значения `timestamp` и `path` зависят от запроса.

Непредвиденная ошибка приложения возвращает `500 Internal Server Error` в стандартном формате Spring Boot:

```json
{
  "timestamp": "2026-11-09T09:30:00.000+00:00",
  "status": 500,
  "error": "Internal Server Error",
  "path": "/api/v1/prices"
}
```

## Методы API

### 1. Последняя цена

```http
GET /api/v1/price
```

Параметры не требуются.

```bash
curl http://localhost:8080/api/v1/price
```

Успешный ответ — `200 OK`:

```json
{
  "symbol": "BNBUSDT",
  "price": "625.500000000000000",
  "fetchedAt": "2026-11-09T09:30:00Z"
}
```

Если в базе данных нет записей — `404 Not Found`:

```json
{
  "status": 404,
  "message": "SymbolData not found"
}
```

### 2. История цен

```http
GET /api/v1/prices
```

| Параметр | Обязательный | Требования | По умолчанию |
|---|---|---|---|
| `from` | Да | Дата в формате `yyyy-MM-dd` | — |
| `to` | Да | Дата в формате `yyyy-MM-dd`, строго позже `from`; верхняя граница не включается | — |
| `page` | Нет | Целое число, `page >= 0` | `0` |
| `size` | Нет | Целое число, `1 <= size <= 100` | `10` |

```bash
curl "http://localhost:8080/api/v1/prices?from=2026-11-01&to=2026-11-07&page=0&size=10"
```

Успешный ответ — `200 OK`:

```json
{
  "items": [
    {
      "symbol": "BNBUSDT",
      "price": "624.000000000000000",
      "fetchedAt": "2026-11-06T09:29:00Z"
    },
    {
      "symbol": "BNBUSDT",
      "price": "625.500000000000000",
      "fetchedAt": "2026-11-06T09:30:00Z"
    }
  ],
  "page": 0,
  "size": 10,
  "totalElements": 2,
  "hasNext": false
}
```

Если за период нет данных, метод всё равно возвращает `200 OK`:

```json
{
  "items": [],
  "page": 0,
  "size": 10,
  "totalElements": 0,
  "hasNext": false
}
```

Если `from` позже или равно `to` — `400 Bad Request`:

```json
{
  "status": 400,
  "message": "from(2026-11-06) can't be after to(2026-11-01)"
}
```

Если `page` отрицательный — `400 Bad Request`:

```json
{
  "status": 400,
  "message": "Page must be non negative (received page: -1) and size must be between 1 and 100 (received size:10)"
}
```

```json
{
  "status": 400,
  "message": "Page must be non negative (received page: 0) and size must be between 1 and 100 (received size:0)"
}
```

Если `size` больше `100` — `400 Bad Request`:

```json
{
  "status": 400,
  "message": "Page must be non negative (received page: 0) and size must be between 1 and 100 (received size:101)"
}
```

Ответы на отсутствующие параметры и параметры неверного формата приведены в разделе «HTTP-коды».

### 3. Средняя цена

```http
GET /api/v1/prices/average
```

| Параметр | Обязательный | Требования |
|---|---|---|
| `from` | Да | Дата в формате `yyyy-MM-dd` |
| `to` | Да | Дата в формате `yyyy-MM-dd`, строго позже `from`; верхняя граница не включается |

```bash
curl "http://localhost:8080/api/v1/prices/average?from=2026-11-01&to=2026-11-06"
```

Успешный ответ — `200 OK`:

```json
{
  "averagePrice": 624.750000000000000
}
```

Если `from` позже или равно `to` — `400 Bad Request`:

```json
{
  "status": 400,
  "message": "from(2026-11-06) can't be after to(2026-11-01)"
}
```

Если за период нет данных — `404 Not Found`:

```json
{
  "status": 404,
  "message": "SymbolData not found"
}
```

Ответы на отсутствующие параметры и даты неверного формата приведены в разделе «HTTP-коды».

### 4. Тренд цены

```http
GET /api/v1/prices/trend
```

| Параметр | Обязательный | Требования |
|---|---|---|
| `from` | Да | Дата в формате `yyyy-MM-dd` |
| `to` | Да | Дата в формате `yyyy-MM-dd`, строго позже `from`; верхняя граница не включается |

```bash
curl "http://localhost:8080/api/v1/prices/trend?from=2026-11-01&to=2026-11-06"
```

Успешный ответ — `200 OK`:
```json
{
  "symbol": "BNBUSDT",
  "start_date": "2026-11-01",
  "end_date": "2026-11-06",
  "trend": "UPWARD"
}
```

Возможные значения `trend`:

- `UPWARD` — последняя цена выше средней цены за период;
- `DOWNWARD` — последняя цена ниже средней цены за период;
- `FLAT` — последняя цена равна средней цене за период.

Если `from` позже или равно `to` — `400 Bad Request`:

```json
{
  "status": 400,
  "message": "from(2026-11-06) can't be after to(2026-11-01)"
}
```

Если за период нет данных — `404 Not Found`:

```json
{
  "status": 404,
  "message": "SymbolData not found"
}
```

Ответы на отсутствующие параметры и даты неверного формата приведены в разделе «HTTP-коды».

## Тестирование

Для запуска тестов выполните:

```bash
./gradlew test
```

Для интеграционных тестов требуется запущенный Docker. Testcontainers автоматически создаёт PostgreSQL-контейнер, а тестовый профиль отключает периодический опрос Binance.