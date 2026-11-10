# Plan: after all modules are done

Do these steps **after every module (Auth … Asset) is built**, before the system goes online.
Added 10 Nov 2026. Order: Part 1 → Part 2 → Part 3. About 3–4 days of work in total.

| Part | What | Time |
|---|---|---|
| 1 | Rate limiting in Spring (auth endpoints) | 1–2 days |
| 2 | Production config (Swagger off, secrets, HTTPS) | 1 day |
| 3 | Nginx in front of Spring (general limit + HTTPS) | 1 day, at deploy |

---

## Part 1. Rate limiting in Spring (Bucket4j)

### 1.1 What is rate limiting? (the bucket idea)

Each user (IP / phone / username) has a **bucket with tokens**, e.g. 5 tokens.

- Every request takes 1 token.
- The bucket gets new tokens over time, e.g. 5 per minute.
- Bucket empty → the request is refused with **429 Too Many Requests**; the controller never runs.

```
minute 0:  [●●●●●]  5 tries left
5 logins:  [     ]  6th login → 429
minute 1:  [●●●●●]  full again
```

### 1.2 Which endpoints and limits

| Endpoint | Key (who is counted) | Limit | Why |
|---|---|---|---|
| `POST /api/v1/auth/login` | IP | 5 / minute | Password guessing on many accounts |
| `POST /api/v1/auth/otp/send` | phone | 3 / 10 minutes | Each SMS costs money |
| `POST /api/v1/auth/otp/login` | IP | 5 / minute | OTP guessing |
| `POST /api/v1/auth/password/forgot` | username | 3 / 10 minutes | Email spam |
| `POST /api/v1/auth/verify-email/resend` | email | 3 / 10 minutes | Email spam |
| `POST /api/v1/auth/register` | IP | 5 / hour | Fake accounts |

Put the numbers in `application.yml` (`ums.rate-limit.*`), not in Java, so they can change per environment.

### 1.3 Files to add

| File | Package | Job |
|---|---|---|
| `build.gradle` | — | `implementation 'com.bucket4j:bucket4j_jdk17-core:<version>'` |
| `RateLimitProperties` | `com.ums.config` | Reads `ums.rate-limit.*` from YAML |
| `RateLimitFilter` | `com.ums.security.ratelimit` | Runs before the controller: find the key, take a token, or answer 429 |
| `messages.properties` / `_km` | `i18n` | `error.too_many_requests=Too many requests. Please wait {0} seconds and try again` |
| `RateLimitApiTest` | test | 6th login in one minute → 429 |

### 1.4 How the filter works (steps)

1. Is the URL one of the limited ones (table 1.2)? No → let it pass.
2. Find the key: IP from `X-Forwarded-For` (behind Nginx) or `request.getRemoteAddr()`;
   phone / username / email from the request body.
3. Get the bucket for that key (kept in a `ConcurrentHashMap`, or Caffeine cache so old keys expire).
4. `bucket.tryConsume(1)`: true → continue; false → write `ApiResponse.error(...)` JSON with status 429 and the
   header `Retry-After: <seconds>`.

Sketch (only the idea, write the real code with comments):

```java
Bucket bucket = buckets.computeIfAbsent(key, k -> Bucket.builder()
        .addLimit(limit -> limit.capacity(5).refillGreedy(5, Duration.ofMinutes(1)))
        .build());
if (!bucket.tryConsume(1)) {
    response.setStatus(429);   // + ApiResponse JSON + Retry-After header
    return;
}
chain.doFilter(request, response);
```

Register it in `SecurityConfig` before the JWT filter (`http.addFilterBefore(rateLimitFilter, ...)`).

### 1.5 Note: one server or many?

- One server: buckets in memory (`ConcurrentHashMap` / Caffeine) are enough.
- Many servers later: memory buckets don't share counts → use Bucket4j with Redis.

### 1.6 Tests

| Test | Expect |
|---|---|
| 5 logins from one IP | 5 normal answers (200 or 401) |
| 6th login from the same IP | 429 + `Retry-After` |
| 6th login from **another** IP | normal answer |
| 4th OTP send to the same phone in 10 minutes | 429 |

---

## Part 2. Production config (Spring)

| Step | What | How |
|---|---|---|
| 2a | Swagger closed in production | `application-prod.yml`: `springdoc.api-docs.enabled: false` and `springdoc.swagger-ui.enabled: false` |
| 2b | Secrets only from environment variables | Already done: `${DB_PASSWORD}`, `${JWT_PRIVATE_KEY}`, `${MINIO_SECRET_KEY}`… Check nothing real is in Git; `keys/*.pem` is already in `.gitignore` |
| 2c | New JWT keys for production | Never reuse the dev keys |
| 2d | Error answers show no stack trace | `server.error.include-stacktrace: never` (GlobalExceptionHandler already answers `error.internal`) |
| 2e | CORS only for the real web address | e.g. `https://admin.ums.edu.kh`, not `*` |

---

## Part 3. Nginx in front of Spring (at deploy)

```
Phone / browser ──HTTPS──▶ Nginx ──HTTP──▶ Spring Boot :8080 (not open to the internet)
```

| Step | What | Nginx |
|---|---|---|
| 3a | HTTPS certificate (free) | Let's Encrypt (`certbot`) |
| 3b | HTTP → HTTPS | `return 301 https://$host$request_uri;` |
| 3c | General limit for all APIs | `limit_req_zone $binary_remote_addr zone=api:10m rate=20r/s;` + `limit_req zone=api burst=40;` |
| 3d | Send the real IP to Spring | `proxy_set_header X-Forwarded-For $proxy_add_x_forwarded_for;` (the Spring filter reads it) |
| 3e | Max upload size | `client_max_body_size 10m;` (same as the file rule) |

Spring needs `server.forward-headers-strategy: native` so it trusts the IP from Nginx.

---

## Security rule to keep while building modules

A GET without `@RequirePermission` (Logged-in) is safe **only when it returns no personal data**
(e.g. batches, programs, rooms). If a response gets names, phones or IDs of people, it needs a permission
such as `STUDENT_VIEW`. Every POST / PUT / DELETE always needs `@RequirePermission`.
