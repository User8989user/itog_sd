# Скан – информационная система для проверки студенческих работ

## Описание
Система предназначена для хранения и автоматической технической проверки контрольных работ студентов (формат, размер). Состоит из трёх микросервисов: 
- **API Gateway** (порт 8080) – единая точка входа, маршрутизация, circuit breaker.
- **File Storing Service** (порт 8081) – сохраняет файлы и метаданные в PostgreSQL.
- **File Analysis Service** (порт 8082) – анализирует файлы (Tika), хранит отчёты, генерирует облако слов (Kumo).

## Требования
- Docker & Docker Compose
- Java 17 (для локальной сборки)
- Maven

## Запуск
1. Склонировать репозиторий:
   ```bash
   git clone https://github.com/your-username/cosmoscan.git
   cd cosmoscan
   ```

2. Собрать и запустить все сервисы:

   ```bash
   docker-compose up --build
   ```

3. После запуска API доступно по адресу http://localhost:8080

API Endpoints (через Gateway)
Загрузить работу
POST /api/works

form-data: studentName (string), file (файл до 1 МБ, разрешённые форматы: pdf, doc, txt)

Ответ: {"id": 1, "studentName": "...", "uploadTime": "..."}

Получить отчёт по работе
GET /api/works/1/reports (внутри Gateway перенаправляет на /api/reports/works/1)

Ответ: {"workId":1,"status":"ACCEPTED","remarks":"","analyzedAt":"..."}

Сгенерировать облако слов
GET /api/works/1/wordcloud – возвращает изображение PNG

Swagger UI
Storing Service: http://localhost:8081/swagger-ui.html

Analysis Service: http://localhost:8082/swagger-ui.html

Обработка ошибок
Если Analysis Service недоступен при загрузке – транзакция откатывается, клиент получает 503.

Если Storing Service недоступен при запросе отчёта – Gateway возвращает fallback-сообщение.

Circuit breaker в Gateway для защиты от повторных вызовов упавшего сервиса.

Тесты
Покрытие кода тестами >60% (JaCoCo). Запуск тестов:

```bash
cd storing-service && mvn test
cd ../analysis-service && mvn test
```

## Скринкаст
Нету пока

## Диаграмма архитектуры
(https://github.com/User8989user/itog_sd/blob/main/%D0%90%D1%80%D1%85%D0%B8%D1%82%D0%B5%D0%BA%D1%82%D1%83%D1%80%D0%B0.png)

## Сценарии взаимодействия
Студент загружает файл -> Gateway -> Storing сохраняет -> вызывает Analysis -> Analysis сохраняет отчёт.

Преподаватель запрашивает отчёт -> Gateway -> Analysis читает из своей БД.

Генерация облака слов -> Gateway -> Analysis запрашивает файл у Storing через REST -> обрабатывает -> возвращает PNG.

Сбой Analysis -> при загрузке Storing получает ошибку, удаляет файл и запись, возвращает 503.

Сбой Storing -> при запросе отчёта Gateway переключается на fallback.

## Дополнительно
Все сервисы контейнеризированы, docker-compose up разворачивает всю систему.

Код чистый, использованы Lombok, @ControllerAdvice, отдельные DTO.


## 7. Инструкция по сборке и запуску

1. **Установите Docker и Docker Compose**.
2. **Создайте указанную структуру папок** и скопируйте содержимое каждого файла.
3. **Соберите JAR-файлы** (перед первым `docker-compose up`):
   ```bash
   cd gateway && mvn clean package && cd ..
   cd storing-service && mvn clean package && cd ..
   cd analysis-service && mvn clean package && cd .. 
   docker-compose up
   ```
4.
Загрузить файл через Postman: POST http://localhost:8080/api/works (form-data).

Получить отчёт: GET http://localhost:8080/api/works/1/reports.

Облако слов: GET http://localhost:8080/api/works/1/wordcloud 

