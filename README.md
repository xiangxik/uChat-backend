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

## REST endpoints

### Get frontend config

```bash
curl http://localhost:8080/api/config
```

Example response:

```json
{
  "appName": "uChat",
  "defaultLocale": "en",
  "webSocketEndpoint": "/ws",
  "chatSendDestination": "/app/chat.send",
  "chatMessageSubscription": "/user/queue/chat.messages",
  "chatErrorSubscription": "/user/queue/chat.errors"
}
```

### Submit feedback

```bash
curl -X POST http://localhost:8080/api/feedback \
  -H 'Content-Type: application/json' \
  -d '{"messageId":"msg-1","rating":5}'
```

### Health

```bash
curl http://localhost:8080/api/health
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
- `Web server failed to start. Port 8080 was already in use`:
  - Stop the old process using port 8080, then restart backend.
- Frontend shows `Chat response timed out`:
  - Confirm backend is running and `/actuator/health` returns `UP`.
  - Refresh frontend page after backend restart so websocket reconnects.
- WebSocket can connect but no message arrives:
  - Ensure send destination is `/app/chat.send`.
  - Ensure subscription is `/user/queue/chat.messages`.
