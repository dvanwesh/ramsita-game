# Ramudu–Sita Chits Game (Spring Boot Backend)

A real-time multiplayer implementation of the traditional Andhra Pradesh
“Ramudu–Sita Chits” game, powered by **Spring Boot**, **WebSockets (STOMP)**, and
an **in-memory game engine**.  
Players join a room, receive hidden roles (Ramudu/Sita/others), and play rounds
with guessing and scoring.

This backend is designed for **public Internet deployment** (AWS/ECS/EC2/EB),
featuring:

- Secure player authentication (via HttpOnly cookie `PLAYER_TOKEN`)
- STOMP WebSocket game state broadcast (`/topic/games/{id}/state`)
- Environment-specific config (`application-dev.yaml`, `application-prod.yaml`)
- CORS security for production
- Rate limiting & spam prevention
- No persistent DB (fully in-memory)

---

## 🚀 Features

### Game Flow
- Create a game → host receives `PLAYER_TOKEN`
- Players join via a 6-letter `gameCode`
- WebSocket subscription:
  /topic/games/{gameId}/state
- Start game when 3+ players have joined
- Automatic chit assignment: exactly 1 RAMUDU and 1 SITA
- Round flow:
- Players see their own chit via: `/api/games/{id}/me`
- Ramudu makes a guess → correct/incorrect → score update
- Multi-round support
- Game ends → final score broadcast

### Security
- HttpOnly cookies (no JS access)
- Global CORS restrictions via `AppProperties`
- Session isolation (player only sees their own chit)
- Rate limiting (IP → N req/min)
- Spam prevention (max active games per creator IP)

---

## 🛠 Tech Stack

- **Java 21+**
- **Spring Boot 3.5**
- **Spring Web, WebSocket, STOMP**
- **Jackson (JSON)**
- **In-memory game registry (no DB)**
- **JUnit + MockMvc + WebSocketStompClient tests**

---

### Run locally:
mvn spring-boot:run -Dspring-boot.run.profiles=dev

## 🔌 REST API
### Create game
POST /api/games
{
"playerName": "Host",
"totalRounds": 3
}
Response includes:

gameId

gameCode

playerId

Set-Cookie: PLAYER_TOKEN=...

Join game

POST /api/games/join
{
"code": "ABC123",
"playerName": "P2"
}

Start game (host only)
POST /api/games/{id}/start
Get my state
GET /api/games/{id}/me

Make guess (Ramudu only)
POST /api/games/{id}/rounds/current/guess
{
"guessedPlayerId": "..."
}

🔊 WebSocket API
Connect
ws://<host>/ws

Subscribe to game updates:
/topic/games/{gameId}/state

Send actions:
/app/games/{gameId}/start
/app/games/{gameId}/rounds/current/guess


Payloads identical to REST versions.

🧪 Tests

Includes:

Full HTTP integration tests (MockMvc)

Full WebSocket integration tests (StompClient)

Unit tests for rate limiting & spam guard

State cleanup between tests for isolation

Run all tests:

mvn test

🏗 Deploying to AWS
Recommended:

AWS ECS Fargate (Docker)

AWS ALB (load balancer) → supports WebSockets

CloudFront optional for frontend

Route53 + TLS (ACM certificate)

EC2 + Nginx reverse proxy also works

Dockerfile (example)
FROM eclipse-temurin:21-jdk
COPY target/ramudu-sita.jar app.jar
ENTRYPOINT ["java","-jar","/app.jar"]
