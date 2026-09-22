# Gotcha Café API

A Spring Boot REST API for the Gotcha Café loyalty and membership platform. Members can subscribe, earn GotchaCoins, browse the menu, place orders, RSVP to events, and more.

## Tech Stack

- **Java 21** / **Spring Boot 4.x**
- **PostgreSQL** (production) / **H2** (tests)
- **Flyway** for database migrations
- **JWT** for authentication (access tokens + refresh token rotation)
- **Stripe** for subscription payments
- **AWS S3** for image storage
- **Gmail API (OAuth2)** for transactional email

## Getting Started

### Prerequisites

- Java 21+
- Docker & Docker Compose
- PostgreSQL (or use Docker)

### Run with Docker Compose

```bash
docker-compose up --build
```

### Run locally (database only via Docker)

```bash
# Start PostgreSQL
docker-compose up db

# Run the application
./mvnw spring-boot:run
```

### Build

```bash
./mvnw clean package -DskipTests
```

The API will be available at `http://localhost:8080/api`.
Swagger UI: `http://localhost:8080/swagger-ui/index.html`

## Environment Variables

| Variable | Purpose |
|---|---|
| `SPRING_DATASOURCE_URL/USERNAME/PASSWORD` | PostgreSQL connection |
| `SPRING_FLYWAY_URL/USER/PASSWORD` | Flyway migration connection |
| `STRIPE_SECRET_KEY` | Stripe API key |
| `STRIPE_WEBHOOK_SECRET` | Stripe webhook signature verification |
| `STRIPE_PRICE_ID` | Stripe price ID for the membership plan |
| `AWS_ACCESS_KEY` / `AWS_SECRET_KEY` / `AWS_REGION` / `AWS_BUCKET_NAME` | S3 image storage |
| `GMAIL_CLIENT_ID` / `GMAIL_CLIENT_SECRET` / `GMAIL_REFRESH_TOKEN` / `GMAIL_SENDER_ADDRESS` | Gmail OAuth2 for transactional email |
| `APP_FRONTEND_URL` | CORS allowed origin + base URL for email links |

## API Overview

| Controller | Prefix | Key Operations |
|---|---|---|
| `AuthController` | `/api` | register, login, logout, refresh token, forgot/reset password, verify email, resend verification |
| `ProfileController` | `/api/member/profile` | get/update profile (with image), delete account |
| `SubscriptionController` | `/api/member/subscription` | subscribe (returns Stripe `clientSecret`), cancel, refund |
| `OrderController` | `/api/member/order` | place order (costs GotchaCoins), list own orders; admin: list all orders |
| `EventController` | `/api` | member: list/get events; admin: create/update/delete events |
| `EventRSVPController` | `/api/member/event` | RSVP to events, update/delete RSVP |
| `AdminController` | `/api/admin` | list/get/delete users (paginated), admin refund |
| `ProductController` | `/api` | member: browse products; admin: CRUD products with images |
| `StaticContentController` | `/api` | public: get section; admin: list/update sections |
| `NewsController` | `/api` | news listing |
| `WebhookController` | `/api/webhook/stripe` | Stripe webhook handler |

See [API_DOCS.md](./API_DOCS.md) for full endpoint documentation.

## Authentication

- **Access token**: JWT, 15-minute expiry — returned in response body on login.
- **Refresh token**: 7-day expiry, rotated on each use — sent as `HttpOnly; Secure; SameSite=None` cookie.
- Tokens are invalidated on server restart (secret key regenerated).

### Authorization

| Path | Access |
|---|---|
| `/api/register`, `/api/login`, `/api/logout`, `/api/refresh`, `/api/webhook/stripe`, `/api/forgot-password`, `/api/reset-password`, `/api/resend-verification`, `GET /api/verify-email` | Public |
| `/api/admin/**` | `ADMIN` only |
| `/api/member/subscription/**` | `ADMIN` or `MEMBER` |
| `/api/member/**` | `MEMBER` with active/past-due subscription |

## Subscription Flow (Stripe)

1. `POST /api/member/subscription` → returns Stripe `clientSecret`
2. Frontend confirms payment via Stripe.js
3. Stripe fires `invoice.paid` webhook → activates subscription, awards **300 GotchaCoins**

Refunds: members have a **7-day window** from last invoice date. Admins have no time restriction. Both paths cancel the Stripe subscription and claw back 300 coins.

## Running Tests

```bash
# Run all tests
./mvnw test

# Run a specific test class
./mvnw test -Dtest=AuthControllerIntegrationTest

# Run a specific test method
./mvnw test -Dtest=SubscriptionControllerIntegrationTest#testRefundSubscription_ExpiredWindow_Returns400
```

Tests use H2 in-memory database (PostgreSQL compatibility mode). Flyway is disabled in tests; schema is created via `ddl-auto=create-drop`. AWS, Gmail, and Stripe beans are mocked.

## Database Migrations

Flyway migrations live in `src/main/resources/db/migration/` (naming: `V{n}__{description}.sql`). Currently at **V28**. Hibernate is set to `validate` — it never auto-creates or alters the schema.
