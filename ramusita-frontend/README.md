# Ramudu–Sita Frontend (React + Vite)

This module contains the React + TypeScript frontend for the Ramudu–Sita online chits game.
The app is optimized for mobile browsers and talks to the backend via REST + WebSockets (STOMP).

## Tech stack

- React + TypeScript
- Vite
- Tailwind CSS
- Framer Motion (animations)
- Axios (HTTP)
- `@stomp/stompjs` for WebSocket / STOMP

## Local development

### Prerequisites

- Node.js 20+ (or a recent LTS)
- npm (or pnpm / yarn)

### Environment variables

Create `.env.development` for local dev:

```env
VITE_API_BASE_URL=http://localhost:8080/api
VITE_WS_BASE_URL=ws://localhost:8080/ws
```

For production builds (served from S3/CloudFront) we typically use relative URLs and let CloudFront route requests:

```env
# .env (used for production build)
VITE_API_BASE_URL=/api
VITE_WS_BASE_URL=/ws
```

### Install & run locally

```bash
cd ramusita-frontend

# Install dependencies
npm install

# Start the Vite dev server
npm run dev
```

Vite will usually start on http://localhost:5173 and the frontend will call the backend at http://localhost:8080.

### Build for production

```bash
npm run build
```

This creates a `dist/` directory containing static assets.

## Deploying to AWS (S3 + CloudFront)

Typical setup:

- S3 bucket: `ramsitagame-frontend-prod`
- CloudFront distribution: serves `https://ramsitagame.com` from the bucket
- CloudFront can route `/api/*` and `/ws*` to the backend service

### 1) Build

```bash
cd ramusita-frontend
npm run build
```

### 2) Sync to S3

```bash
aws s3 sync dist/ s3://ramsitagame-frontend-prod/ --delete
```

The `--delete` flag removes files in the bucket that are no longer present in `dist/`.
Prefer serving via CloudFront with origin access (OAC) rather than making the bucket public.

### 3) Invalidate CloudFront cache

Replace `<CLOUDFRONT_DISTRIBUTION_ID>` with your distribution ID:

```bash
aws cloudfront create-invalidation \
  --distribution-id <CLOUDFRONT_DISTRIBUTION_ID> \
  --paths "/*"
```

This ensures users receive the latest JS/CSS bundles.

## API & WebSocket usage

HTTP wrapper (example: `src/api/http.ts`):

```ts
// src/api/http.ts
import axios from "axios";
import { API_BASE_URL } from "../config";

export const http = axios.create({
  baseURL: API_BASE_URL,
  withCredentials: true, // sends PLAYER_TOKEN cookie
});
```

STOMP client (example: `src/api/ws.ts`):

```ts
// src/api/ws.ts
import { Client } from "@stomp/stompjs";

export function createStompClient(gameId: string, onState: (state: any) => void) {
  const client = new Client({
    brokerURL: import.meta.env.VITE_WS_BASE_URL,
    reconnectDelay: 2000,
    onConnect: () => {
      client.subscribe(`/topic/games/${gameId}/state`, (msg) => {
        onState(JSON.parse(msg.body));
      });
    },
  });

  client.activate();
  return client;
}
```

## Quick production push (reference)

Whenever you want to push frontend changes to production:

```bash
cd ramusita-frontend
npm run build
aws s3 sync dist/ s3://ramsitagame-frontend-prod/ --delete
aws cloudfront create-invalidation \
  --distribution-id <CLOUDFRONT_DISTRIBUTION_ID> \
  --paths "/*"
```

Replace `<CLOUDFRONT_DISTRIBUTION_ID>` with your real distribution ID (for example `E123ABC456XYZ`).

---

## Backend build & deploy (brief)

```bash
cd ramudu-sita
./mvnw clean package -DskipTests

# If using the EB CLI:
eb deploy ramsitagame-backend
```
