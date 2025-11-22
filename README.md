# Ramudu–Sita Chits Game (Online Multiplayer)

Ramudu–Sita (Rama–Sita) is a classic chits game popular in Andhra Pradesh.
This repository contains an online multiplayer implementation that lets friends join from their phones, draw chits, and play while the app tracks scores across rounds.

Live site: https://ramsitagame.com

## Features

- Create / join games with a short game code
- Mobile-friendly UI (optimized for phone browsers)
- Real-time updates via WebSockets (STOMP)
- Secret chits for each player (Ramudu, Sita, Lakshman, Hanuman, Bharata, Shatrughna…)
- Round-based scoring and per-round score history
- Final scoreboard with crown and confetti for the winner
- “How to play” help modal and dedicated rules page

## Tech Stack

- **Frontend**
  - React + TypeScript
  - Vite bundler
  - Tailwind CSS
  - Framer Motion (animations)
  - STOMP over WebSocket

- **Backend**
  - Spring Boot (Java 25)
  - In-memory game store
  - STOMP WebSocket endpoint
  - Cookie-based player identity
  - Simple rate limiting & spam protection

- **Infrastructure**
  - AWS S3 – static hosting for frontend
  - AWS CloudFront – CDN + HTTPS + routing for `/api/*` and `/ws*`
  - AWS Elastic Beanstalk – backend deployment (Java)
  - Custom domain: `ramsitagame.com`

## Repository structure

```text
ramsita-game/
├─ README.md                # This file
├─ ramudu-sita/             # Spring Boot backend service
│  ├─ README.md
│  └─ src/ ...
└─ ramusita-frontend/       # React/Vite frontend app
  ├─ README.md
  └─ src/ ...
```

## Running locally

### Backend (Spring Boot)

```bash
cd ramudu-sita
# using the bundled mvnw wrapper on macOS/linux (or use 'mvn' if you have Maven installed)
./mvnw spring-boot:run -Dspring-boot.run.profiles=dev
```

This starts the backend on http://localhost:8080 using the `dev` profile (see `application-dev.yaml`).

### Frontend (React + Vite)

```bash
cd ramusita-frontend

# Install dependencies
npm install

# Start development server (Vite)
npm run dev
```
## Deployment overview

Sync the frontend build output to the S3 bucket, then invalidate the CloudFront distribution cache:

```bash
aws s3 sync dist/ s3://ramsitagame-frontend-prod/ --delete

aws cloudfront create-invalidation \
  --distribution-id E1071P5JZR01CY \
  --paths "/*"
```