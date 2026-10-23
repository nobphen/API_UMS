# University Management System (UMS)

Spring Boot 4.1 · Java 21 · PostgreSQL · package by feature.

## Run locally

```bash
docker compose up -d          # PostgreSQL, Redis, MinIO
cp .env.example .env
./gradlew bootRun              # profile "dev" by default (gradlew.bat on Windows)
```

Flyway applies `db/migration` on startup; the `dev` profile also loads `db/testdata`.
API docs: http://localhost:8080/swagger-ui.html

## Layout

Each module (and each sub-feature of a large module) has four folders:

- `api/` – controllers and request/response DTOs
- `application/` – service interfaces, `impl/` service implementations, MapStruct mappers
- `domain/` – JPA entities (one per table)
- `infrastructure/` – Spring Data repositories

See `PROJECT_STRUCTURE.md` for the full tree.

## Profiles

`dev`, `sit`, `uat`, `preprod`, `prod` – settings come from environment variables (see `.env.example`).

Run locally, each profile falls back to its own database in the docker-compose Postgres (user `ums` / `ums`):
`dev` → `ums`, `sit` → `ums_sit`, `uat` → `ums_uat`, `preprod` → `ums_preprod`.
`prod` has no defaults, so it fails to start unless `DB_URL`, `DB_USERNAME` and `DB_PASSWORD` are set.
In IntelliJ, pick the matching `UMS [<profile>]` run configuration.

The dev profile also loads `db/testdata`: a super admin (`admin` / `Admin@12345`) and one program with a batch.

## Accounts and login

Only applicants sign up themselves (`POST /api/v1/auth/register`). Students are added by the office:

1. `POST /api/v1/students` creates the student and a login (username = student code) and returns a **temporary password once**.
2. At first login the token response has `mustChangePassword: true`; until `POST /api/v1/auth/password/change`,
   every other endpoint except `/auth/me` and `/auth/logout` answers 403.
3. Forgotten password: `POST /auth/password/forgot` sends a 6-digit code to the account's email (10 min, 5 tries),
   then `POST /auth/password/reset`. Set `SPRING_MAIL_*` (see `.env.example`); in dev the code is written to the log.

Study levels: Foundation, Certificate, Diploma, Associate, Bachelor, Master, PhD. One person and one login can hold
several student records, one per program (Bachelor → Master → PhD, or a double degree):
`POST /api/v1/students/{id}/programs` adds a program to an existing student, and `GET /api/v1/students/me`
lists the logged-in user's programs.

Addresses: every person can have a BIRTH_PLACE, PERMANENT and CURRENT address (`person_addresses`). In Cambodia the app
sends only the smallest area chosen (`areaId`, usually the village) and the server fills the commune, district and
province; abroad it sends `country` + `addressLine`. Areas come from `geo_areas` (25 provinces, 210 districts,
1,652 communes, 14,578 villages with official codes), seeded by `V4__seed_geo_areas.sql` from the Pumi gazetteer
(github.com/dwilkie/pumi, MIT). Dropdowns: `GET /api/v1/geo-areas` (provinces), `?parentId=` (areas inside one),
`&q=` (search by Khmer or English name), `GET /api/v1/geo-areas/{id}/path`. Admins with GEO_AREA_MANAGE add areas
(`POST`) and rename or deactivate them (`PUT /{id}`). `GET|PUT /api/v1/persons/{id}/addresses` reads or replaces
a person's addresses. Read-only views `provinces`, `districts`, `communes`, `villages` show each level with the
names above it (for DBeaver and reports), and a trigger keeps the tree consistent (parent level, code prefix).

When the Ministry of Interior updates the areas, run `./gradlew geoSync` (latest Pumi data) or
`./gradlew geoSync -Psource=path/to/file.csv` (columns `code,name_kh,name_en,unit_kh,unit_en`). It compares the
official list with `geo_areas` (`DB_URL`/`DB_USERNAME`/`DB_PASSWORD`, default local dev) and writes the next
`V{n}__update_geo_areas_{date}.sql` with only the new, renamed, restored and abolished areas, and reports how many
addresses use each abolished one. Abolished areas are switched off, never deleted. `-PdryRun` only reports.
Commit the file and deploy: Flyway applies it once on every server. Never edit `V4__seed_geo_areas.sql` after deployment.

Countries: `countries` holds all 249 ISO 3166-1 countries (Khmer + English names and nationalities), seeded by
`V5__seed_countries.sql` from country-list (github.com/umpirsky/country-list, MIT, Unicode CLDR).
`GET /api/v1/countries` (Cambodia first; `?withNationality=true` for the Nationality list, `&q=` to search).
When country names change (e.g. Turkey → Türkiye) or a country is added, run `./gradlew countrySync` (latest Unicode
CLDR names) or `./gradlew countrySync -Psource=countries.csv` (`code,name_en,name_kh[,nationality_en,nationality_kh]`).
Like `geoSync`, it writes the next `V{n}__update_countries_{date}.sql` with only the differences, keeps the existing
nationalities (CLDR has none), warns when a new country needs them, and never deletes a country. `-PdryRun` only reports.
Persons send `nationalityCode` (empty = KH); addresses send `countryCode` (empty = KH). Only KH addresses may use
the province → village lists (checked by the service and by a database CHECK); others need `addressLine`.

Devices (`/api/v1/user-sessions`): `GET /me`, `DELETE /me/{id}`, `POST /me/terminate-others`;
admins: `GET /users/{userId}`, `POST /users/{userId}/terminate-all`.
Admin account actions (`/api/v1/users/{id}`): `reset-password`, `lock`, `unlock`.

Roles are dynamic: admins make them at run time from the fixed permission list.
`GET /api/v1/permissions`; `GET|POST /api/v1/roles`, `GET|PUT|DELETE /api/v1/roles/{id}`;
`GET|POST /api/v1/users/{id}/roles`, `DELETE /api/v1/users/{id}/roles/{userRoleId}` (scope: GLOBAL, FACULTY,
DEPARTMENT, PROGRAM or CLASS_SECTION, with optional dates). Nobody can give a permission they don't hold, only a
SUPER_ADMIN can give SUPER_ADMIN, and the last SUPER_ADMIN can't be removed. Users get changes at their next token
refresh (within 15 minutes).
A nightly job ends expired sessions and sessions unused for longer than the user's `session_ttl_days`.

## JWT keys (RSA)

`config/JwtConfig` loads one RSA key pair from PEM files: the private key signs tokens, the public key verifies them.
Access and refresh tokens share the key and are told apart by their `typ` claim.
Keys are git-ignored, so generate a pair once for local development:

```bash
mkdir -p src/main/resources/keys
openssl genpkey -algorithm RSA -pkeyopt rsa_keygen_bits:2048 -out src/main/resources/keys/private.pem
openssl pkey -in src/main/resources/keys/private.pem -pubout -out src/main/resources/keys/public.pem
```

On servers, keep the keys outside the jar: `JWT_PRIVATE_KEY=file:/etc/ums/keys/private.pem`, `JWT_PUBLIC_KEY=file:/etc/ums/keys/public.pem`.
