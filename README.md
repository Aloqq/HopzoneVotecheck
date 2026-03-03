# Hopzone Vote Verify

Веб-сервис для проверки статуса голосования Hopzone по IP. Игрок загружает скриншоты, сервис проверяет API Hopzone и при `voted=false` создаёт отчёт для модераторов.

## Стек

- Java 21, Spring Boot 3, Thymeleaf
- MySQL 8, Flyway
- reCAPTCHA v2, Telegram Bot API

## Возможности

- Проверка голоса по IP через Hopzone API
- Два скриншота: IP в игре + «You voted» на Hopzone
- При `voted=true` — только успех, без сохранения
- При `voted=false` — создание кейса, отчёт, уведомление в Telegram
- Команда `/compensation-send nick vote` для модераторов
- Сохранение данных формы в localStorage (кроме скриншотов)
- Кнопка «Удалить» для смены загруженного фото
- Rate limit: 10 запросов/минута на IP
- Автоочистка кейсов старше 14 дней

## Страницы

| URL | Описание |
|-----|----------|
| `/verify` | Форма проверки |
| `/history` | История кейсов (фильтры, пагинация) |
| `/report/{hash}` | Публичный отчёт кейса |

## Конфигурация

Скопируй `.env.example` в `.env` и заполни:

```env
MYSQL_URL=jdbc:mysql://localhost:3306/hopzone_verify?...
MYSQL_USER=root
MYSQL_PASS=root

APP_BASE_URL=https://your-domain.com

HOPZONE_TOKEN=...
RECAPTCHA_SITE_KEY=...
RECAPTCHA_SECRET_KEY=...

TELEGRAM_BOT_TOKEN=...
TELEGRAM_CHAT_ID=-100...
TELEGRAM_MESSAGE_THREAD_ID=...   # ID темы канала (опционально)

STORAGE_DIR=uploads
```

## Запуск локально

```bash
# БД
mysql -u root -p -e "CREATE DATABASE hopzone_verify CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;"

# Сборка и запуск
mvn spring-boot:run
# или
./run.sh
```

## Развёртывание на сервер

### 1. Сборка и копирование

```bash
mvn package -DskipTests
scp target/hopzone-vote-verify-*.jar user@server:/home/user/hopzone/target/
scp .env start.sh stop.sh logs.sh nginx.conf user@server:/home/user/hopzone/
```

На сервере:
```bash
mkdir -p /home/user/hopzone/target /home/user/hopzone/uploads
chmod +x start.sh stop.sh logs.sh
```

### 2. Скрипты

| Скрипт | Действие |
|--------|----------|
| `./start.sh` | Запуск в фоне, логи в `logs/app.log` |
| `./stop.sh` | Остановка |
| `./logs.sh` | Просмотр логов (tail -f) |

### 3. Nginx + SSL

```bash
sudo apt install nginx certbot python3-certbot-nginx
sudo cp nginx.conf /etc/nginx/sites-available/hopzone
# Поправь путь alias в nginx.conf при необходимости
sudo ln -s /etc/nginx/sites-available/hopzone /etc/nginx/sites-enabled/
sudo nginx -t && sudo systemctl reload nginx
sudo certbot --nginx -d your-domain.com
```

### 4. reCAPTCHA

Добавь домен в [Google reCAPTCHA Admin](https://www.google.com/recaptcha/admin).

## Структура проекта

```
├── src/main/java/          # Spring Boot приложение
├── src/main/resources/
│   ├── db/                 # Flyway миграции, schema.sql
│   ├── templates/          # Thymeleaf
│   └── static/             # CSS, JS
├── .env.example
├── nginx.conf
├── start.sh, stop.sh, logs.sh, run.sh
└── pom.xml
```

## Ссылки

- **Репозиторий:** [github.com/Aloqq/HopzoneVotecheck](https://github.com/Aloqq/HopzoneVotecheck)

## Лицензия

Made with 💙 by [Aloq](https://github.com/Aloqq)
