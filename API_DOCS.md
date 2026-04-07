# Gotcha Cafe API Documentation

Base URL: `http://localhost:8080/api`

Interactive docs (Swagger UI): `http://localhost:8080/swagger-ui.html`

## Authentication

This API uses a **dual-token** authentication strategy:

- **Access token** (short-lived, 15 min) — sent by the frontend in the `Authorization` header for every API call
- **Refresh token** (long-lived, 7 days) — stored as an **HTTP-only cookie**, managed entirely by the browser and backend

All `/member/**` and `/admin/**` endpoints require a JWT access token:

```
Authorization: Bearer <access_token>
```

### How refresh tokens work

The refresh token is **never exposed to JavaScript**. The backend sets it as a `Set-Cookie` header, and the browser automatically stores and sends it. This protects against XSS attacks.

**Cookie properties:**
| Property | Value | Purpose |
|----------|-------|---------|
| `HttpOnly` | `true` | JavaScript cannot read the cookie |
| `Secure` | `true` | Only sent over HTTPS |
| `SameSite` | `None` | Allows cross-origin requests (frontend on different domain) |
| `Path` | `/api` | Cookie only sent to `/api` endpoints |
| `Max-Age` | `604800` (7 days) | Cookie expiration |

### Token rotation

Each call to `/refresh` invalidates the old refresh token and issues a new one. If a revoked token is reused (potential theft), the entire token family is revoked, logging out all sessions for that user.

A 30-second grace period allows concurrent requests that may use the same token before the rotation completes.

### JWT claims

The access token contains the following claims in its payload:

```json
{
  "sub": "user@email.com",
  "userId": 24,
  "role": "MEMBER",
  "iat": 1712422800,
  "exp": 1712423700
}
```

The frontend can decode the token to get user info without an extra API call:

```javascript
const payload = JSON.parse(atob(accessToken.split('.')[1]));
// payload.sub    → "user@email.com"
// payload.userId → 24
// payload.role   → "MEMBER"
```

### Frontend integration

The frontend must include `credentials: 'include'` (fetch) or `withCredentials: true` (axios) for the browser to send/receive cookies cross-origin:

```javascript
// Login
const res = await fetch('/api/login', {
  method: 'POST',
  credentials: 'include',  // required for Set-Cookie to be stored
  headers: { 'Content-Type': 'application/json' },
  body: JSON.stringify({ email, password })
});
const accessToken = await res.text();

// Refresh (when access token expires)
const res = await fetch('/api/refresh', {
  method: 'POST',
  credentials: 'include'  // browser auto-sends the refreshToken cookie
});
const accessToken = await res.text();

// Logout
await fetch('/api/logout', {
  method: 'POST',
  credentials: 'include',
  headers: { 'Authorization': `Bearer ${accessToken}` }
});
```

---

## Auth Endpoints

### POST `/register`
Register a new user account.

**Auth:** None

**Body:**
```json
{
  "userName": "string (3-20 chars)",
  "email": "valid email",
  "password": "string (min 8 chars, must contain uppercase, lowercase, digit, special char)"
}
```

**Response:** `200` — UserResponse
```json
{
  "userId": 1,
  "username": "string",
  "email": "string",
  "status": "ACTIVE",
  "gotchaCoins": 0,
  "profilePictureKey": null,
  "role": "MEMBER",
  "mood": null,
  "subscriptionStatus": null,
  "gotchaFavDrink": null
}
```

---

### POST `/login`
Login and receive an access token. The refresh token is set as an HTTP-only cookie.

**Auth:** None

**Body:**
```json
{
  "email": "string",
  "password": "string"
}
```

**Response:** `200` — Access token string

**Headers:**
```
Set-Cookie: refreshToken=<token>; Path=/api; HttpOnly; Secure; SameSite=None; Max-Age=604800
```

**Body:** Raw JWT access token string (contains `userId`, `role`, and `email` as claims)

---

### POST `/refresh`
Rotate the refresh token and get a new access token. The refresh token is read from the HTTP-only cookie (sent automatically by the browser) — no request body needed.

**Auth:** None (authenticated via refresh token cookie)

**Request:** No body required. The browser sends the `refreshToken` cookie automatically.

**Response:** `200` — Access token string

**Headers:**
```
Set-Cookie: refreshToken=<new_token>; Path=/api; HttpOnly; Secure; SameSite=None; Max-Age=604800
```

**Body:** Raw JWT access token string

**Errors:**
- `401` — Missing cookie, expired token, revoked token, or token reuse detected

---

### POST `/logout`
Invalidate the current access token and revoke the refresh token family. The refresh token cookie is cleared.

**Auth:** Bearer token (header `Authorization`)

**Request:** No body required. The refresh token is read from the cookie.

**Response Headers:**
```
Set-Cookie: refreshToken=; Path=/api; HttpOnly; Secure; SameSite=None; Max-Age=0
```

**Response:** `200` — "Logged out successfully"

---

### POST `/forgot-password`
Send a password reset email.

**Auth:** None

**Body:**
```json
{
  "email": "string"
}
```

**Response:** `200` — Success message

---

### POST `/reset-password`
Reset password using the token from the email.

**Auth:** None

**Body:**
```json
{
  "token": "string",
  "newPassword": "string"
}
```

**Response:** `200` — Success message

---

### GET `/verify-email?token={token}`
Verify email address after registration.

**Auth:** None

**Response:** `200` — Success message

---

## Admin Endpoints

> Requires `ADMIN` role.

### GET `/admin/users`
Get all users (paginated).

**Params:** `page`, `size`, `sort` (default: username,desc)

**Response:** `200` — `Page<UserResponse>`
```json
{
  "content": [
    {
      "userId": 1,
      "username": "string",
      "email": "string",
      "status": "ACTIVE|PENDING",
      "gotchaCoins": 0,
      "profilePictureKey": "string",
      "role": "ADMIN|MEMBER",
      "mood": "string",
      "subscriptionStatus": "ACTIVE|INACTIVE",
      "gotchaFavDrink": "string"
    }
  ],
  "totalElements": 0,
  "totalPages": 0
}
```

---

### GET `/admin/users/{id}`
Get a single user by ID.

**Response:** `200` — User object

---

### DELETE `/admin/users/{id}`
Delete a user by ID.

**Response:** `200` — "User deleted"

---

## Event Endpoints

### GET `/member/event`
Get all events (paginated).

**Auth:** Member (active subscription)

**Params:** `page`, `size`, `sort` (default: eventDate,desc)

**Response:** `200` — `Page<EventResponse>`
```json
{
  "content": [
    {
      "id": 1,
      "title": "string",
      "eventDate": "2025-01-01T00:00:00",
      "imgUrl": "string"
    }
  ]
}
```

---

### GET `/member/event/{id}`
Get a single event by ID.

**Auth:** Member (active subscription)

**Response:** `200` — Event object

---

### POST `/admin/event`
Create a new event.

**Auth:** Admin

**Content-Type:** `multipart/form-data`

**Parts:**
- `event` — JSON event data
- `imageFile` — Image file

**Response:** `200` — Event object

---

### PUT `/admin/event/{id}`
Update an event.

**Auth:** Admin

**Content-Type:** `multipart/form-data`

**Parts:**
- `event` — JSON event data
- `imageFile` — Image file

**Response:** `200` — Event object

---

### DELETE `/admin/event/{id}`
Delete an event.

**Auth:** Admin

**Response:** `200` — "Event deleted"

---

## Event RSVP Endpoints

### POST `/member/event/rsvp`
Submit an RSVP for an event.

**Auth:** Member (active subscription)

**Body:**
```json
{
  "eventId": 1,
  "rsvpName": "string",
  "rsvpEmail": "email",
  "guestNumber": 1
}
```

**Response:** `200` — Success message

---

### GET `/member/event/rsvp/me`
Get all RSVPs for the currently authenticated user (paginated). User identity is extracted from the JWT token — no user ID needed in the URL.

**Auth:** Member (active subscription)

**Params:** `page`, `size`, `sort` (default: rsvpId,desc)

**Response:** `200` — `Page<EventRSVP>`

---

### PUT `/member/event/rsvp/me`
Update a specific RSVP for the current user, identified by `eventId` in the request body.

**Auth:** Member (active subscription)

**Body:**
```json
{
  "eventId": 1,
  "rsvpName": "string",
  "rsvpEmail": "email",
  "guestNumber": 1
}
```

**Response:** `200` — Success message

---

### DELETE `/member/event/rsvp/me`
Delete all RSVPs for the currently authenticated user.

**Auth:** Member (active subscription)

**Response:** `200` — Success message

---

### GET `/admin/user/{user_id}/rsvp`
Get all RSVPs by a specific user (paginated).

**Auth:** Admin

**Response:** `200` — `Page<EventRSVP>`

---

### GET `/admin/event/{event_id}/rsvp`
Get all RSVPs for a specific event (paginated).

**Auth:** Admin

**Response:** `200` — `Page<EventRSVP>`

---

### GET `/admin/event/rsvp`
Get all RSVPs (paginated).

**Auth:** Admin

**Params:** `page`, `size`, `sort` (default: rsvpId,desc)

**Response:** `200` — `Page<EventRSVP>`

---

## Order Endpoints

### POST `/member/order`
Place a new order.

**Auth:** Member (active subscription)

**Body:**
```json
{
  "items": [
    {
      "productId": 1,
      "quantity": 2
    }
  ]
}
```

**Response:** `200` — Success message

---

### GET `/member/order`
Get current user's orders (paginated).

**Auth:** Member (active subscription)

**Response:** `200` — `Page<OrderResponse>`
```json
{
  "content": [
    {
      "orderId": 1,
      "orderCode": "ORD-20250101-ABC123",
      "status": "PENDING|COMPLETED|CANCELLED",
      "orderDate": "2025-01-01T00:00:00",
      "totalPrice": 1500,
      "items": [
        {
          "productName": "string",
          "quantity": 2,
          "totalPrice": 1500
        }
      ]
    }
  ]
}
```

---

### GET `/admin/order`
Get all orders with filters (paginated).

**Auth:** Admin

**Params:** `orderCode`, `status`, `minPrice`, `maxPrice`, `createDate`, `updateDate`, `page`, `size`

**Response:** `200` — `Page<OrderResponse>`

---

### GET `/admin/order/{orderId}`
Get a single order by ID.

**Auth:** Admin

**Response:** `200` — OrderResponse object

---

## Product Endpoints

### GET `/member/product`
Get all products with filters (paginated).

**Auth:** Member (active subscription)

**Params:** `keyword`, `brand`, `category`, `minPrice`, `maxPrice`, `productAvailable`, `page`, `size`

**Response:** `200` — `Page<ProductResponse>`
```json
{
  "content": [
    {
      "name": "string",
      "price": 500,
      "imageUrl": "string"
    }
  ]
}
```

---

### GET `/member/product/{productId}`
Get a single product by ID.

**Auth:** Member (active subscription)

**Response:** `200` — Product object

---

### POST `/admin/product`
Create a new product.

**Auth:** Admin

**Content-Type:** `multipart/form-data`

**Parts:**
- `productRequest` — JSON: `{ "name", "description", "brand", "price", "category", "productAvailable", "stockQuantity", "imageUrl" }`
- `productImage` — Image file

**Response:** `200` — Success message

---

### PUT `/admin/product/{productId}`
Update a product.

**Auth:** Admin

**Content-Type:** `multipart/form-data`

**Parts:**
- `productRequest` — JSON product data
- `productImage` — Image file

**Response:** `200` — Success message

---

### DELETE `/admin/product/{productId}`
Delete a product.

**Auth:** Admin

**Response:** `200` — Success message

---

## Profile Endpoints

### GET `/member/profile`
Get the current user's profile.

**Auth:** Member (active subscription)

**Response:** `200` — ProfileResponse
```json
{
  "username": "string",
  "email": "string",
  "gotchaCoins": 0,
  "profilePictureUrl": "string",
  "mood": "string",
  "subscriptionStatus": "ACTIVE",
  "gotchaFavDrink": "string",
  "gotchaFavDrinkPictureUrl": "string"
}
```

---

### PUT `/member/profile`
Update the current user's profile.

**Auth:** Member (active subscription)

**Content-Type:** `multipart/form-data`

**Parts:**
- `profile` — JSON: `{ "username", "mood", "gotchaFavDrink" }`
- `profileImage` — Image file

**Response:** `200` — ProfileResponse

---

### DELETE `/member/profile/`
Delete the current user's profile/account.

**Auth:** Member (active subscription)

**Response:** `200` — Success message

---

## Static Content Endpoints

### GET `/member/static-content`
Get all static content sections.

**Auth:** Member (active subscription)

**Response:** `200` — `List<StaticContent>`

---

### GET `/member/static-content/{sectionKey}`
Get a specific static content section.

**Auth:** Member (active subscription)

**Response:** `200` — StaticContent object

---

### PUT `/admin/static-content/{sectionKey}`
Update a static content section.

**Auth:** Admin

**Content-Type:** `multipart/form-data`

**Parts:**
- `content` — JSON StaticContent data
- `imageFile` — Image file

**Response:** `200` — StaticContent object

---

## Subscription Endpoints

### POST `/member/subscription/`
Subscribe (creates a Stripe payment intent).

**Auth:** Member or Admin

**Response:** `200`
```json
{
  "clientSecret": "stripe_client_secret_string"
}
```

---

### DELETE `/member/subscription/`
Cancel subscription.

**Auth:** Member or Admin

**Response:** `200` — Success message

---

## News Endpoint

### GET `/member/news`
Get the news page (events + static content combined).

**Auth:** Member (active subscription)

**Params:** `page`, `size`, `sort` (default: releaseDate,desc)

**Response:** `200` — NewsResponse
```json
{
  "event": { "content": [...], "totalElements": 0 },
  "staticContent": [...]
}
```

---

## Webhook Endpoint

### POST `/webhook/stripe`
Handle Stripe webhook events.

**Auth:** None (verified via `Stripe-Signature` header)

**Headers:** `Stripe-Signature`

**Body:** Raw Stripe event payload

**Response:** `200` — Success message

---

## Error Responses

All errors follow this format:

```json
{
  "timestamp": "2025-01-01T00:00:00",
  "status": 400,
  "errors": {
    "field": "error message"
  }
}
```

| Status | Meaning |
|--------|---------|
| 400 | Bad Request — validation errors |
| 401 | Unauthorized — invalid/missing JWT |
| 403 | Forbidden — insufficient permissions or inactive subscription |
| 404 | Not Found |
| 429 | Too Many Requests — rate limit exceeded |
| 500 | Internal Server Error |
