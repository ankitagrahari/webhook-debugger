# HookSpy — Project Summary & Handoff Document

## What Is HookSpy

A webhook capture, inspect, and replay platform targeting developers integrating
services like Stripe, Razorpay, GitHub, Shopify. Developers get a unique URL,
point any webhook source at it, and see every request arrive in real time.
They can inspect headers, body, query params, replay to any target, and export
as cURL commands.

**Pricing:** Free (₹0), Pro (₹299/month), Team (₹799/month)
**Target audience:** Indian indie developers and small SaaS teams

---

## Architecture

```
External Service (Stripe, Razorpay etc.)
        │
        │  POST https://hookspy.dev/h/{slug}
        ▼
┌─────────────────────────────────────────┐
│  capture-service :8080                  │
│  Spring Boot WebFlux + Kafka Binder     │
│  Receives webhook → Sinks.Many buffer   │
│  → Kafka topic: webhook-events          │
│  Returns 200 immediately to caller      │
└─────────────────────────────────────────┘
        │ Kafka KRaft (no Zookeeper)
        ▼
┌─────────────────────────────────────────┐
│  processor-service :8081                │
│  Spring Boot + Spring Kafka             │
│  @KafkaListener → validates tier limits │
│  → saves to PostgreSQL                  │
│  RetryableTopic: 3 retries + DLT        │
│  Nightly cleanup: deletes expired data  │
└─────────────────────────────────────────┘
        │ PostgreSQL (shared DB)
        ▼
┌─────────────────────────────────────────┐
│  ui-service :8082                       │
│  Spring Boot + Vaadin 24.4.4            │
│  Dashboard, auth, replay, tier UI       │
│  Polls DB every 3s for live updates     │
└─────────────────────────────────────────┘
```

---

## Tech Stack

| Component | Technology |
|---|---|
| Language | Java 21 |
| Spring Boot | 4.0.5 |
| Spring Cloud | 2025.1.1 |
| Kafka | KRaft mode (no Zookeeper), confluentinc/cp-kafka:7.7.0 |
| Kafka producer | Spring Cloud Stream Kafka Binder (capture-service) |
| Kafka consumer | Spring Kafka @KafkaListener (processor-service) |
| Database | PostgreSQL 16 |
| ORM | Spring Data JPA + Hibernate |
| Migrations | Flyway |
| Frontend | Vaadin 24.4.4 |
| Security | Spring Security + BCrypt + VaadinWebSecurity |
| Build | Maven multi-module |
| Package | `org.backendbrilliance` (ui-service) |
| DB creds | username: webhook, password: webhook, db: webhookdb |

---

## Project Structure

```
webhook-debugger/
├── pom.xml                          # Parent pom, Spring Boot 4.0.5
├── docker-compose.yml               # Dev: Kafka KRaft + PostgreSQL
├── docker-compose.prod.yml          # Prod: all services + nginx
├── .env.example                     # Secrets template
├── deploy.sh                        # Build + dockerize + start
├── landing/index.html               # Static landing page
├── nginx/nginx.conf                 # Reverse proxy + rate limiting
├── common/                          # Shared DTOs, enums, constants
│   └── src/main/java/dev/webhookdebugger/common/
│       ├── dto/WebhookEventDto.java
│       ├── dto/EndpointDto.java
│       ├── enums/Tier.java          # FREE(1ep,100/day,1d) PRO(10,∞,30d) TEAM(50,∞,90d)
│       └── constants/KafkaConstants.java
├── capture-service/                 # WebFlux + Kafka Binder
│   └── src/main/java/dev/webhookdebugger/capture/
│       ├── CaptureServiceApplication.java
│       ├── config/KafkaProducerConfig.java   # Sinks.Many + Supplier<Flux>
│       ├── controller/WebhookCaptureController.java  # POST /h/{slug}
│       └── producer/WebhookEventProducer.java
├── processor-service/               # Kafka consumer + DB persistence
│   └── src/main/java/dev/webhookdebugger/processor/
│       ├── ProcessorServiceApplication.java  # @EnableScheduling
│       ├── config/KafkaConsumerConfig.java
│       ├── config/KafkaTopicConfig.java
│       ├── consumer/WebhookEventConsumer.java # RetryableTopic, manual ack
│       ├── entity/Endpoint.java
│       ├── entity/WebhookRequest.java
│       ├── entity/User.java
│       ├── repository/ (Endpoint, WebhookRequest, User repos)
│       ├── service/WebhookProcessorService.java  # tier enforcement
│       ├── service/RetentionCleanupService.java  # nightly cleanup @2am
│       └── service/AlertService.java             # Slack alerts (Pro)
└── ui-service/                      # Vaadin UI
    └── src/main/java/org/backendbrilliance/ui/
        ├── UiServiceApplication.java
        ├── config/SecurityConfig.java  # VaadinWebSecurity + DaoAuth
        ├── entity/ (Endpoint, WebhookRequest, User)
        ├── repository/ (Endpoint, WebhookRequest, User repos)
        ├── security/
        │   ├── AuthenticatedUser.java      # Vaadin AuthenticationContext
        │   └── HookSpyUserDetailsService.java
        ├── service/
        │   ├── EndpointService.java        # CRUD + tier limit check
        │   ├── WebhookRequestService.java  # tier-aware history query
        │   ├── UserService.java            # register with tier selection
        │   ├── ReplayService.java          # Java HttpClient replay
        │   └── RazorpayService.java        # payment order + upgrade
        ├── controller/PaymentController.java  # POST /api/payment/confirm
        └── views/
            ├── MainLayout.java     # AppLayout + sidebar + refresh button
            ├── HomeView.java       # Landing CTA
            ├── LoginView.java      # Spring Security login
            ├── RegisterView.java   # Tier selection + registration
            ├── DashboardView.java  # 3-panel: URL bar + requests + detail
            └── UpgradeView.java    # Razorpay checkout
```

---

## Database Schema

```sql
-- users
id UUID PK, email VARCHAR UNIQUE, password_hash VARCHAR,
tier VARCHAR (FREE/PRO/TEAM), created_at, updated_at

-- endpoints
id UUID PK, slug VARCHAR(32) UNIQUE, user_id UUID FK→users,
label VARCHAR(100), created_at, expires_at

-- webhook_requests
id UUID PK, endpoint_id UUID FK→endpoints, method VARCHAR(10),
headers JSONB, body TEXT, query_params JSONB,
source_ip VARCHAR(45), content_type VARCHAR, body_size BIGINT,
received_at TIMESTAMP
```

Flyway migrations:
- `V1__initial_schema.sql` — creates all tables + indexes
- `V2__tier_enforcement.sql` — CHECK constraint on tier + indexes
- `V3__users_index.sql` — index on users.email for login performance

---

## Key Design Decisions

**Kafka Binder vs Spring Kafka:**
- capture-service uses Kafka Binder (Spring Cloud Stream) — reactive producer
  via `Sinks.Many<WebhookEventDto>` + `Supplier<Flux>`. No explicit serializers
  needed — binder uses Jackson Message Converter via `content-type: application/json`
- processor-service uses Spring Kafka directly — `@KafkaListener` with manual ack
  and `RetryableTopic` for fine-grained reliability control

**Why capture-service returns 200 immediately:**
External services (Stripe etc.) have short timeout windows. We return 200 before
processing to never make callers wait. Kafka buffers the event.

**Tier enforcement layers:**
1. processor-service: checks daily request count before saving (discards if over limit)
2. ui-service: checks endpoint count before creating (throws TierLimitException)
3. ui-service: filters history by retention window in WebhookRequestRepository
4. processor-service: nightly cleanup deletes requests older than tier window

**WebhookCaptureController accepts all HTTP methods:**
```java
@RequestMapping(value = "/h/{slug}", method = {GET, POST, PUT, PATCH, DELETE})
```
Because different services use different methods for their webhooks.

**Replay uses Java HttpClient directly:**
Not through capture-service. ReplayService fires HTTP directly to target URL
with original headers/body. Skips forbidden headers (host, content-length etc.)

---

## What's Built and Working

```
✅ capture-service — receives webhooks, produces to Kafka
✅ processor-service — consumes from Kafka, saves to PostgreSQL
✅ Kafka KRaft (no Zookeeper) — confirmed working
✅ Spring Cloud 2025.1.1 + Boot 4.0.5 + Kafka Binder — confirmed working
✅ ui-service — Vaadin dashboard with live polling
✅ Request list with method badges, source IP, size, timestamp
✅ Request detail — Body (JSON pretty-print), Headers, Query Params tabs
✅ Replay — fires original request to any target URL, shows response
✅ cURL export — copies any request as curl command
✅ Auth — DB-backed registration + login (BCrypt, Spring Security)
✅ Tier selection on registration (Free/Pro/Team radio button UI)
✅ Tier enforcement — endpoint limits, daily request limits, retention
✅ Nightly cleanup — deletes expired requests per tier
✅ Delete endpoint — from sidebar with confirm dialog
✅ Refresh button — refreshes sidebar endpoint list
✅ Razorpay upgrade flow — JS checkout + /api/payment/confirm endpoint
✅ Landing page — static HTML, matches app design
✅ Dockerfiles — all 3 services
✅ docker-compose.prod.yml — full stack with nginx
✅ nginx config — rate limiting, Vaadin push support
✅ deploy.sh — build + dockerize + health check
✅ Slack alerts — for Pro/Team, hourly inactivity check
```

---

## Known Issues / Pending Fixes

### Active bugs
1. **Request count badge shows 0** — badge initialised once at render, not updated
   by polling task. Fix: store as field, update with `countRequests()` in polling.

2. **Vaadin UI design feels heavy** — shadow DOM makes CSS control difficult.
   Dark mode attempted but unreliable. Current theme: light only.

### Security gaps (must fix before production)
3. **Razorpay signature not verified** — PaymentController.confirm() trusts the
   client payload. Anyone can POST and get Pro for free. Fix: HMAC-SHA256
   verification using razorpay secret key.

4. **No rate limiting on /register** — open to spam account creation.

### Missing features
5. **HTTPS not configured** — needs Let's Encrypt before going live.
6. **No welcome email** — no email provider integrated yet.
7. **No forgot password flow**.
8. **No request filtering** — can't filter by method or date.
9. **No error page** — blank screen on errors.

---

## Next Step — Switch from Vaadin to React

### Why

Vaadin is hurting the product:
- Shadow DOM makes dark mode impossible without JS hacks
- Every CSS rule needs `!important` to pierce web components
- UI feels "heavy" — lots of bundled JS
- Styling fights the framework constantly

### What the React rewrite means

**Backend stays 100% identical:**
- capture-service — no changes
- processor-service — no changes
- All entities, repositories, services — no changes
- PostgreSQL schema — no changes
- Kafka pipeline — no changes

**Only ui-service changes:**
- Remove Vaadin dependency
- Add Spring MVC REST controllers (replacing Vaadin views)
- Add JWT or session-based auth REST endpoints
- Create a separate `frontend/` React app

### Proposed React stack

| Component | Technology | Why |
|---|---|---|
| Framework | React 18 + Vite | Fast build, modern |
| Styling | TailwindCSS | Full CSS control, no shadow DOM |
| Components | shadcn/ui | Pre-built, accessible, excellent design |
| Routing | React Router v6 | Standard |
| Data fetching | TanStack Query | Caching, polling, loading states |
| Live updates | EventSource (SSE) | Replaces Vaadin push polling |
| Auth | HTTP sessions (Spring Security) | Simpler than JWT for same-domain |
| HTTP client | Axios or fetch | Standard |

### REST API endpoints needed in ui-service

```
POST   /api/auth/login              — Spring Security form login
POST   /api/auth/logout             — Invalidate session
POST   /api/auth/register           — Create user with tier
GET    /api/auth/me                 — Current user info + tier

GET    /api/endpoints               — List user's endpoints
POST   /api/endpoints               — Create endpoint
DELETE /api/endpoints/{id}          — Delete endpoint

GET    /api/endpoints/{slug}/requests          — Request history (tier-filtered)
GET    /api/endpoints/{slug}/requests/stream   — SSE stream for live updates
POST   /api/requests/{id}/replay              — Replay request to target URL

POST   /api/payment/confirm         — Razorpay callback
```

### React views to build

```
/login          → LoginPage.jsx
/register       → RegisterPage.jsx  (tier selector)
/               → HomePage.jsx      (redirect to first endpoint or empty state)
/dashboard/:slug → DashboardPage.jsx
  ├── Sidebar.jsx          (endpoint list + create + delete)
  ├── UrlBar.jsx           (endpoint URL + copy + live indicator + tier badge)
  ├── RequestList.jsx      (grid with method badge, path, source, size, time)
  └── RequestDetail.jsx    (tabs: Body | Headers | Query — replay + cURL)
/upgrade        → UpgradePage.jsx   (Razorpay checkout)
```

### SSE live updates (replaces 3-second Vaadin polling)

```java
// In ui-service REST controller — add SSE endpoint
@GetMapping(value = "/api/endpoints/{slug}/requests/stream",
            produces = MediaType.TEXT_EVENT_STREAM_VALUE)
public SseEmitter streamRequests(@PathVariable String slug) {
    SseEmitter emitter = new SseEmitter(Long.MAX_VALUE);
    // Register emitter, send new requests as they arrive
    // processor-service publishes to an in-memory event bus
    // ui-service listens and pushes to SSE emitters
    return emitter;
}
```

In React:
```javascript
// In RequestList.jsx
useEffect(() => {
    const source = new EventSource(`/api/endpoints/${slug}/requests/stream`);
    source.onmessage = (e) => {
        const newRequest = JSON.parse(e.data);
        setRequests(prev => [newRequest, ...prev]);
    };
    return () => source.close();
}, [slug]);
```

### How to structure the migration

**Phase 1 — REST API layer (2-3 days)**
- Add REST controllers to ui-service
- Add CORS config for local dev (React dev server on :5173, API on :8082)
- Test all endpoints with curl

**Phase 2 — React app (5-7 days)**
- Scaffold with Vite: `npm create vite@latest frontend -- --template react`
- Install: `tailwindcss shadcn/ui react-router-dom @tanstack/react-query axios`
- Build views one by one
- Wire SSE for live updates

**Phase 3 — Production build (1 day)**
- `npm run build` outputs to `frontend/dist/`
- Copy dist into ui-service `src/main/resources/static/`
- Spring Boot serves React app at `/`
- API at `/api/*`
- No separate nginx config needed for the frontend

### Files to create in Phase 1

The AI in the next chat should create these REST controllers in ui-service:

```
ui-service/src/main/java/org/backendbrilliance/ui/
├── rest/
│   ├── AuthController.java        — login, logout, register, me
│   ├── EndpointController.java    — CRUD endpoints
│   ├── RequestController.java     — list requests, SSE stream, replay
│   └── dto/                       — Request/Response DTOs for REST API
└── config/
    ├── WebConfig.java             — CORS config for dev
    └── SecurityConfig.java        — update to support both Vaadin + REST
                                     (or remove Vaadin entirely)
```

---

## Environment Variables Reference

```env
DB_NAME=webhookdb
DB_USER=webhook
DB_PASSWORD=webhook

RAZORPAY_KEY_ID=rzp_live_...
RAZORPAY_KEY_SECRET=...

SLACK_WEBHOOK_URL=https://hooks.slack.com/services/...

DOMAIN=hookspy.dev
```

---

## How to Run Locally

```bash
# 1. Start infra
docker compose up kafka postgres -d

# 2. Build all modules
mvn clean install -DskipTests

# 3. Start services (3 separate terminals)
cd processor-service && mvn spring-boot:run   # runs Flyway migrations first
cd capture-service && mvn spring-boot:run
cd ui-service && mvn spring-boot:run

# 4. Access
open http://localhost:8082

# 5. Test webhook capture
curl -X POST http://localhost:8080/h/test-slug \
  -H "Content-Type: application/json" \
  -d '{"event":"payment.success","amount":500}'
```

---

## Services and Ports

| Service | Port | Tech |
|---|---|---|
| capture-service | 8080 | Spring Boot WebFlux |
| processor-service | 8081 | Spring Boot MVC |
| ui-service | 8082 | Spring Boot + Vaadin |
| PostgreSQL | 5432 | postgres:16 |
| Kafka | 9092 | KRaft, cp-kafka:7.7.0 |

---

## Important Notes for Next Chat

1. **Spring Cloud 2025.1.1 works with Spring Boot 4.0.5** — confirmed working
   despite documentation saying otherwise. Do not downgrade.

2. **Kafka Binder (capture-service) requires `spring.function.definition`** in
   application.yml — without it the binder won't wire the Supplier beans.

3. **ui-service has a reactor-netty conflict** — Spring Cloud BOM pulls in
   reactor-netty-http transitively. Fix in pom.xml:
   ```xml
   <exclusion>
       <groupId>io.projectreactor.netty</groupId>
       <artifactId>reactor-netty-http</artifactId>
   </exclusion>
   ```
   And in application.yml: `spring.main.web-application-type: servlet`

4. **Vaadin theme requires** `src/main/frontend/themes/webhook-debugger/`
   folder with at least an empty `styles.css` and `theme.json` with
   `{"parent": "lumo"}`. Missing this causes build failure.

5. **AuthenticatedUser uses Vaadin's AuthenticationContext**, not
   `SecurityContextHolder` — the latter doesn't work on Vaadin's push threads.

6. **`@Configuration(proxyBeanMethods = false)`** is required on all config
   classes — CGLIB proxying fails on Java 21 without this.

7. **Flyway is only in processor-service** — ui-service connects to the same
   DB but does not run migrations. Start processor-service first.
