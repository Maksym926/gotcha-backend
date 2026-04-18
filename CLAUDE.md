# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Build & Run Commands

```bash
# Build the project
./mvnw clean package -DskipTests

# Run the application (requires PostgreSQL running locally on port 5432)
./mvnw spring-boot:run

# Run all tests
./mvnw test

# Run a single test class
./mvnw test -Dtest=AuthControllerIntegrationTest

# Run a single test method
./mvnw test -Dtest=SubscriptionControllerIntegrationTest#testRefundSubscription_ExpiredWindow_Returns400

# Start only the database for local development
docker-compose up db

# Build and run everything with Docker Compose
docker-compose up --build
```

## Architecture Overview

**Spring Boot 4.x** REST API for a café loyalty/membership platform. Java 21, PostgreSQL, Flyway migrations, all endpoints under `/api`.

### Package Structure

```
com.gotcha.gotcha_api/
├── controller/       # REST controllers (thin, delegate to services)
├── service/          # Business logic
├── model/            # JPA entities
│   └── dto/          # Request/Response DTOs (records)
├── repo/             # Spring Data JPA repositories
├── securityConfig/   # Spring Security, JWT filter, rate limiting, CORS
├── enums/            # Role, AccountStatus, OrderStatus, SubscriptionStatus
├── exception/        # GlobalExceptionHandler + custom exception classes
├── flywayConfig/     # Flyway programmatic config
├── s3Config/         # AWS S3 client bean
└── stripeConfig/     # Stripe API key initialization
```

### Key Entities

| Entity | Description |
|---|---|
| `User` | Central entity. Has `role` (MEMBER/ADMIN), `accountStatus` (ACTIVE/INACTIVE/SUSPENDED/PENDING/DELETED), `subscriptionStatus` (ACTIVE/INACTIVE/PAST_DUE), `gotchaCoins`, Stripe IDs, `cancelAtPeriodEnd` |
| `RefreshToken` | Persisted DB tokens with family-based revocation for replay-attack detection |
| `EmailVerificationToken` | 24-hour single-use token sent on registration or resend. Old tokens are invalidated before a new one is issued. |
| `PasswordResetToken` | 15-minute single-use token for password reset flow |
| `Event` / `EventRSVP` | Café events with RSVP (attending/not attending) per user |
| `Product` / `Order` / `OrderItem` | Café menu ordering, paid with `gotchaCoins` |
| `StaticContent` | CMS-style key/value blocks with optional S3 image keys |

### Authentication Flow

- **Access token**: JWT, **15-minute** expiry. Returned in response body on login. Contains `userId`, `role`, `email` claims. Secret key is generated fresh on startup — tokens are **invalidated on restart**.
- **Refresh token**: Persisted in DB, **7-day** expiry, rotated on each use. Sent/received as `HttpOnly; Secure; SameSite=None` cookie (`refreshToken`).
- **Token blacklist**: `TokenBlacklistService` uses an in-memory `ConcurrentHashMap`. Cleared on restart — expired access tokens effectively expire naturally after 15 min.
- **Token cleanup**: `TokenCleanupService` runs daily at 03:00 via `@Scheduled` to purge expired refresh/email/password-reset tokens from the DB.
- **`JWTFilter`**: Extracts bearer token from `Authorization` header, validates it, checks blacklist, and loads `UserPrincipal` into `SecurityContext` on every request.

### Authorization Layers

| Path | Rule |
|---|---|
| `POST /api/register`, `/api/login`, `/api/logout`, `/api/refresh`, `/api/webhook/stripe`, `/api/forgot-password`, `/api/reset-password`, `/api/resend-verification` | Public |
| `GET /api/verify-email` | Public |
| `/swagger-ui/**`, `/v3/api-docs/**` | Public |
| `/api/admin/**` | `ADMIN` authority only |
| `/api/member/subscription/**` | `ADMIN` or `MEMBER` authority |
| `/api/member/**` | `MemberSubscriptionAuthorizationManager`: `ADMIN` always allowed; `MEMBER` only if `subscriptionStatus` is `ACTIVE` or `PAST_DUE` |

**Important for tests**: `UserPrincipal.isEnabled()` returns `user.getStatus() == AccountStatus.ACTIVE`. Test users must have `status` set to `AccountStatus.ACTIVE` or login will return 401.

### API Endpoints Summary

| Controller | Prefix | Key Operations |
|---|---|---|
| `AuthController` | `/api` | register, login, logout, refresh token, forgot/reset password, verify email, resend verification |
| `ProfileController` | `/api/member/profile` | get/update profile (multipart with image), delete account |
| `SubscriptionController` | `/api/member/subscription` | subscribe (returns Stripe `clientSecret`), cancel, refund |
| `OrderController` | `/api/member/order` | place order (costs coins), list own orders; admin: list all orders with filtering |
| `EventController` | `/api` | member: list/get events; admin: create/update/delete events (multipart with image) |
| `EventRSVPController` | `/api/member/event` | RSVP to events, update/delete RSVP |
| `AdminController` | `/api/admin` | list/get/delete users (paginated), admin refund |
| `ProductController` | `/api` | member: browse products; admin: CRUD products with images |
| `StaticContentController` | `/api` | member: get content blocks; admin: CRUD content with images |
| `NewsController` | `/api` | news listing (member) |
| `WebhookController` | `/api/webhook/stripe` | Stripe webhook handler (public) |

### Stripe Integration

Subscription flow uses `DEFAULT_INCOMPLETE` payment behavior (payment confirmation handled client-side):
1. `POST /api/member/subscription` → creates Stripe Customer + Subscription → returns `clientSecret` to frontend
2. Frontend confirms payment with Stripe.js
3. Stripe fires `invoice.paid` webhook → activates subscription, adds **300 `gotchaCoins`**

**Refund rules**:
- `refundAsMember()`: enforces 7-day window from last paid invoice date; throws `RefundNotAllowedException` (→ HTTP 400) if expired
- `refundAsAdmin()`: no time restriction
- Both paths cancel the Stripe subscription immediately and claw back 300 coins via `deactivateAndClawBack()`

**Handled webhook events**: `invoice.paid`, `invoice.payment_failed`, `customer.subscription.updated`, `customer.subscription.deleted`, `charge.refunded`

### Email

Uses **Gmail API with OAuth2** — not SMTP. `EmailService` builds a `Gmail` client via `UserCredentials` (client ID + secret + refresh token) on startup. Sends:
- Email verification links (24h expiry) on registration and resend
- Password reset links (15min expiry)

**Password reset guard**: `PasswordResetService.createPasswordResetToken()` silently skips sending an email if the account is not `ACTIVE` (e.g. `PENDING`). The API still returns the same generic 200 response to prevent enumeration.

**Resend verification guard**: `UserService.resendVerificationEmail()` silently skips if the account does not exist or is not `PENDING` (e.g. already `ACTIVE`, `SUSPENDED`, `DELETED`). All existing tokens for the user are deleted before a new one is issued.

### Image Storage

AWS S3 (`S3Service`). Images uploaded as multipart in admin endpoints. Keys stored on entity fields (`profilePictureKey`, `gotchaFavDrinkPictureKey`, etc.). `S3Service.generateSignedUrl()` used to serve images.

### Rate Limiting

`RateLimitingFilter` (Bucket4j, in-memory, per-IP) runs before the JWT filter in the filter chain.

### Database Migrations

Flyway migrations in `src/main/resources/db/migration/`, naming `V{n}__{description}.sql`. Currently at **V26**. Hibernate is set to `validate` — it never creates or alters schema.

## Testing

Tests live in `src/test/java/` with config at `src/test/resources/application-test.properties`.

**Test stack**: `@SpringBootTest` + `MockMvc` + H2 in-memory (PostgreSQL compatibility mode). Flyway is disabled; H2 creates the schema via `ddl-auto=create-drop`.

**Always mock these beans** in integration tests:
- `@MockitoBean S3Service` — avoids real AWS calls
- `@MockitoBean EmailService` — avoids real Gmail API calls
- `@MockitoBean SubscriptionService` — avoids real Stripe calls (when testing controllers that use it)

**Test user setup**: set `AccountStatus.ACTIVE` on any user that needs to authenticate, otherwise Spring Security's `isEnabled()` check rejects login with 401.

```java
// Minimum viable test user for authenticated requests
user.setStatus(AccountStatus.ACTIVE);
user.setRole(Role.MEMBER);
user.setSubscriptionStatus(SubscriptionStatus.ACTIVE); // required for /api/member/** access
```

All integration tests are `@Transactional` — DB state rolls back after each test.

## Environment Variables

Required for production (injected via `docker-compose.yml`):

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

Swagger UI: `http://localhost:8080/swagger-ui/index.html`
