# uChat Backend

Spring Boot 4 backend for the existing uChat frontend.

## Requirements

- Java 21+
- Maven Wrapper included

## Run

From backend directory:

```bash
cd /Volumes/NVME/github/xiangxik/uChat/uChat-backend
./mvnw spring-boot:run
```

From repository root (if current directory is `/Volumes/NVME/github/xiangxik/uChat`):

```bash
/Volumes/NVME/github/xiangxik/uChat/uChat-backend/mvnw -f /Volumes/NVME/github/xiangxik/uChat/uChat-backend/pom.xml spring-boot:run
```

Application starts on `http://localhost:8080`.

## Test

```bash
./mvnw test
```

## Storage provider

Backend uses a shared storage provider switch for persistence-related modules.

- `uchat.storage.provider=in-memory` (default): chat history + feedback use in-memory stores.
- `uchat.storage.provider=postgres`: chat history + feedback are persisted into PostgreSQL.

Configure in [src/main/resources/application.yml](src/main/resources/application.yml):

```yaml
uchat:
  storage:
    provider: in-memory
```

### PostgreSQL mode

Use the `postgres` profile (which sets `uchat.storage.provider=postgres`) and provide datasource variables:

```bash
export SPRING_DATASOURCE_URL='jdbc:postgresql://127.0.0.1:5432/uchat'
export SPRING_DATASOURCE_USERNAME='postgres'
export SPRING_DATASOURCE_PASSWORD='postgres'
```

Then start backend:

```bash
cd /Volumes/NVME/github/xiangxik/uChat/uChat-backend
./mvnw spring-boot:run -Dspring-boot.run.profiles=postgres
```

Flyway migrations are auto-applied on startup and create:

- `chat_history_turns`
- `feedback_entries`

## REST endpoints

### Get frontend config

```bash
curl http://localhost:8080/api/config
curl "http://localhost:8080/api/config?locale=zh"
curl -H "Accept-Language: zh-CN,zh;q=0.9" http://localhost:8080/api/config
```

Example response:

```json
{
  "appName": "uChat",
  "defaultLocale": "en",
  "locale": "en",
  "initialBotMessage": "Welcome to the uChat Enterprise Service Center. Tell us what you would like to ask and we will arrange a specialist to follow up.",
  "messagePlaceholder": "For example: I want to learn about business loan options and eligibility",
  "sendLabel": "Send message",
  "tipText": "Note: Responses on this page are for demo purposes only. Please refer to official documents and human support for formal business matters.",
  "thankYouText": "Thank you for your feedback",
  "thinkingText": "Advisor is checking the details...",
  "serviceCenterText": "Service Center",
  "onlineText": "Online",
  "ratingLabels": ["Poor", "Fair", "Good", "Very good", "Excellent"],
  "webSocketEndpoint": "/ws",
  "chatSendDestination": "/app/chat.send",
  "chatMessageSubscription": "/user/queue/chat.messages",
  "chatErrorSubscription": "/user/queue/chat.errors"
}
```

Note:
- UI text fields in this response are sourced from backend i18n bundles under `src/main/resources/messages_en.properties` and `src/main/resources/messages_zh.properties`.
- To adjust copy, update those message files instead of `application.yml`.
- Locale resolution order is: query `locale` > `Accept-Language` header > backend `defaultLocale`.

### Submit feedback

```bash
curl -X POST http://localhost:8080/api/feedback \
  -H 'Content-Type: application/json' \
  -d '{"messageId":"msg-1","rating":5}'
```

### Health

```bash
curl http://localhost:8080/actuator/health
```

## WebSocket integration

- Endpoint: `/ws`
- Allowed origins:
  - `http://localhost:5173`
  - `http://127.0.0.1:5173`
- Send destination: `/app/chat.send`
- Message subscription: `/user/queue/chat.messages`
- Error subscription: `/user/queue/chat.errors`

### Send payload

```json
{
  "conversationId": "conv-1",
  "clientMessageId": "client-1",
  "content": "I need a loan",
  "locale": "en"
}
```

### Bot message payload

```json
{
  "id": "generated-message-id",
  "clientMessageId": "client-1",
  "conversationId": "conv-1",
  "sender": "bot",
  "content": "We can walk you through personal loans, mortgages, and SME financing. Share your budget range and I will suggest a direction first.",
  "timestamp": "2026-06-20T16:00:00Z"
}
```

### Error payload

```json
{
  "code": "CHAT_BAD_REQUEST",
  "message": "Unsupported locale: fr",
  "clientMessageId": "client-1",
  "timestamp": "2026-06-20T16:00:00Z"
}
```

## Behavior notes

- Empty `content` does not produce a 500. The backend returns a friendly bot reply.
- Supported locales are `en` and `zh`.
- Chat reply generation is isolated behind `ChatReplyService` for future AI integration.

## Local integration checklist

1. Start backend first:

```bash
cd /Volumes/NVME/github/xiangxik/uChat/uChat-backend
./mvnw spring-boot:run
```

Or from repository root:

```bash
/Volumes/NVME/github/xiangxik/uChat/uChat-backend/mvnw -f /Volumes/NVME/github/xiangxik/uChat/uChat-backend/pom.xml spring-boot:run
```

2. Start frontend:

```bash
cd /Volumes/NVME/github/xiangxik/uChat/uChat-frontend
npm run dev -- --host 127.0.0.1
```

3. Open `http://127.0.0.1:5173` and send a chat message.

## Troubleshooting

- `zsh: no such file or directory: ./mvnw`:
  - You are not inside `uChat-backend`.
  - Fix by either running `cd /Volumes/NVME/github/xiangxik/uChat/uChat-backend && ./mvnw spring-boot:run` or use the root-safe command with `-f`.
- `No plugin found for prefix 'spring-boot'`:
  - Maven is executing against the wrong project (usually repo root without backend pom).
  - Run with backend pom explicitly:

```bash
/Volumes/NVME/github/xiangxik/uChat/uChat-backend/mvnw -f /Volumes/NVME/github/xiangxik/uChat/uChat-backend/pom.xml spring-boot:run
```
- `Process terminated with exit code: 143` after `spring-boot:run`:
  - This usually means the Java process received a termination signal (for example terminal cleanup or manual stop).
  - It is expected when stopping a long-running dev server and does not indicate a code failure.
- `Web server failed to start. Port 8080 was already in use`:
  - Stop the old process using port 8080, then restart backend.
- Frontend shows `Chat response timed out`:
  - Confirm backend is running and `/actuator/health` returns `UP`.
  - Refresh frontend page after backend restart so websocket reconnects.
- WebSocket can connect but no message arrives:
  - Ensure send destination is `/app/chat.send`.
  - Ensure subscription is `/user/queue/chat.messages`.
