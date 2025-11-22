# Ramudu–Sita Game Backend

### Spring Boot · WebSockets (STOMP) · In-Memory Game Engine

This module contains the backend service powering the Ramudu–Sita (Rama–Sita) online multiplayer chits game — a Telugu classic from Andhra Pradesh.
The backend provides REST APIs, WebSocket real-time updates, rate-limiting, and player session logic.

---

## 🚀 Features

### Game engine
- Create and join games using a short game code
- In-memory game store with cleanup and TTL expiration
- Supports Ramudu, Sita, Lakshman, Hanuman, Bharata & Shatrughna roles
- Multi-round gameplay with scoring logic
- Score-by-round tracking

### API & real-time updates
- REST endpoints under `/api/**`
- WebSocket endpoint: `ws://<domain>/ws` (or `wss://` in production)
- STOMP pub/sub topic: `/topic/games/{gameId}/state`
- Every join/start/guess action broadcasts the public game state to subscribers

### Security & abuse protection
- Cookie-based lightweight player identity (`PLAYER_TOKEN`)
- CORS restrictions per environment
- IP-based rate limiting and spam protection (e.g. "Too Many Active Games")

### Environment profiles
- `dev` (local)
- `prod` (AWS Elastic Beanstalk)

---

## 🛠 Tech stack

| Layer      | Technology                                            |
|------------|-------------------------------------------------------|
| Language   | **Java 21+**                                          |
| Framework  | **Spring Boot 3.5**                                   |
| WebSockets | STOMP over Spring Messaging                           |
| Build      | Maven Wrapper (`./mvnw`)                              |
| Deployment | AWS Elastic Beanstalk (Java 21 platform)              |
| Infra      | CloudFront → EB (origin routing), S3 for frontend     |

---

## 📁 Project structure

```
ramudu-sita/
├── src/
│   ├── main/
│   │   ├── java/com/game/ramudu_sita/...
│   │   └── resources/
│   │       ├── application.yaml
│   │       ├── application-dev.yaml
│   │       └── application-prod.yaml
├── target/ (generated)
├── pom.xml
└── README.md  <-- this file
```

---

## 🔧 Local development

### Prerequisites

- JDK 21+
- Maven (or use the included `./mvnw` wrapper)

The backend runs by default at: `http://localhost:8080`.

### Run the service (dev)

```bash
cd ramudu-sita
./mvnw spring-boot:run -Dspring-boot.run.profiles=dev
```

### Build a JAR (production / packaging)

```bash
cd ramudu-sita
./mvnw clean package -DskipTests
```

Run the packaged JAR:

```bash
java -jar target/ramudu-sita-0.0.1-SNAPSHOT.jar
```

---

## 🔌 REST API endpoints (selected)

Game lifecycle

| Method | Endpoint                      | Description                  |
|--------|-------------------------------|------------------------------|
| POST   | /api/games                    | Create a new game            |
| POST   | /api/games/join               | Join using game code         |
| POST   | /api/games/{id}/start         | Host starts the game         |
| GET    | /api/games/{id}/me            | Get current player's view    |

Gameplay

| Method | Endpoint                                         | Description                |
|--------|--------------------------------------------------|----------------------------|
| POST   | /api/games/{id}/rounds/current/guess             | Ramudu (host) makes a guess|

Responses are per-player views so chit secrecy is preserved.

---

## 🔔 WebSocket (STOMP)

Connect to the WebSocket endpoint (wss in prod):

```text
wss://<domain>/ws
```

Subscribe to game state updates:

```text
/topic/games/{gameId}/state
```

Server pushes GamePublicState on events like:
- Player join
- Game start
- Guess made
- Round reveal
- Final scores

Clients send actions to the application destination, for example:

```text
/app/games/{gameId}/guess
```

(The server maps `/app/**` to controller methods that process client actions.)

---

## 🧩 Configuration files (examples)

`application-dev.yaml` (local dev profile)

```yaml
spring:
  config:
    activate:
      on-profile: dev

app:
  allowed-origins:
    - "http://localhost:5173"
    - "*"

server:
  port: 8080
```

`application-prod.yaml` (Elastic Beanstalk / prod profile)

```yaml
spring:
  config:
    activate:
      on-profile: prod

app:
  allowed-origins:
    - "https://ramsitagame.com"

server:
  port: 5000
```

Make sure your EB environment variables include:

```bash
SPRING_PROFILES_ACTIVE=prod
JAVA_TOOL_OPTIONS=-Dserver.port=5000
```

---

## 🛡 Rate limiting & anti-spam

The service applies IP-based rate limiting and spam protection. Example behavior:

- Creating too many games too quickly returns HTTP 429 RATE_LIMIT_EXCEEDED
- Exceeding per-creator/IP active-game limits returns a structured error, e.g.:

```json
{
  "error": "TOO_MANY_ACTIVE_GAMES",
  "message": "Too many active games for this client"
}
```

Tests covering these behaviors are in `GameControllerIntegrationTest.java`.

---

## 🧪 Running tests

Run unit and integration tests:

```bash
cd ramudu-sita
./mvnw test
```

Tests cover GameService logic, scoring, chit distribution, rate limiting, WebSocket broadcast flow and REST controller error handling.

---

## 🏗 Deploying to AWS Elastic Beanstalk

1) Build the JAR

```bash
cd ramudu-sita
./mvnw clean package -DskipTests
```

2) Deploy using the EB CLI (recommended)

```bash
# Init (run once)
eb init -p java-21 ramsitagame-backend --region us-east-1

# Deploy new version
eb deploy ramsitagame-backend
```

Replace the environment name if different.

3) Ensure EB environment variables are set (see above).

---

## ☁️ CloudFront origin setup (routing)

Configure CloudFront to route requests to the backend (Elastic Beanstalk origin):

```text
/api/*  →  Elastic Beanstalk origin
/ws*    →  Elastic Beanstalk origin (WebSockets)
```

Recommended CloudFront policies:
- Cache policy: CachingDisabled
- Origin request policy: AllViewer (forward all headers/cookies)

---

If you'd like, I can add a short contributing/development checklist, or add quick-start scripts to the repo.
