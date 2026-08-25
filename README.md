# SettleUp - Shared Expense Tracking REST API

A standalone Spring Boot REST API for **SettleUp**, a shared-expense tracking application for groups of friends (roommates/trip groups). It manages users, groups, split expenses, dynamic member balances, settlement payment flows, greedy minimum-transaction settlement plans, audit logging, and dual-layer Role-Based Access Control (System-wide + Group-level).

---

## 🚀 Tech Stack

- **Language & Framework**: Java 17, Spring Boot 3.2.4
- **Build Tool**: Gradle (`build.gradle`, `settings.gradle`)
- **Security**: Spring Security (Stateless JWT Authentication with JJWT 0.12.5)
- **Database**: MySQL 8 (Local/Docker), H2 (In-memory Test Profile)
- **Database Migrations**: Flyway Core + Flyway MySQL
- **ORM & Data**: Spring Data JPA & Hibernate (`ddl-auto: validate`)
- **API Documentation**: Springdoc OpenAPI / Swagger UI (`/swagger-ui.html`)
- **Build & Utilities**: Gradle, Lombok, Jakarta Bean Validation
- **Testing**: JUnit 5, Mockito

---

## 🛠️ Setup & Running Locally

### Option 1: Docker Compose (Recommended)
Spins up both MySQL 8 and the SettleUp Spring Boot backend API automatically using Gradle multi-stage build:

```bash
docker-compose up --build
```

- **API Base URL**: `http://localhost:8080`
- **Swagger UI Documentation**: `http://localhost:8080/swagger-ui.html`
- **OpenAPI JSON Docs**: `http://localhost:8080/v3/api-docs`

### Option 2: Local Gradle Execution
Make sure a local MySQL instance is running on port `3306` with database `settleup` created, or update database properties in `src/main/resources/application.yml`.

Run database migrations and launch the application:
```bash
./gradlew bootRun
```

To execute unit tests:
```bash
./gradlew test
```

To build executable JAR:
```bash
./gradlew bootJar
```

---

## 🔑 Initial Bootstrap Admin Credentials

A default system administrator is automatically seeded via Flyway migration `V2__seed_admin.sql`:

- **Email**: `admin@settleup.com`
- **Password**: `Admin@123456`
- **Role**: `ADMIN`

> **Note**: Public self-registration (`POST /api/auth/register`) only creates standard `USER` accounts. Administrative role assignment must be performed by an existing `ADMIN` via `PUT /api/admin/users/{id}/role`.

---

## 🌐 API Endpoint Summary

### Auth (`/api/auth`)
- `POST /api/auth/register` — Self-register a new `USER`
- `POST /api/auth/login` — Login with email/password to receive JWT access token (~15m) & refresh token (~7d)
- `POST /api/auth/refresh` — Refresh access token using a valid refresh token

### Users (`/api/users` & `/api/admin/users`)
- `GET /api/users/me` — View current authenticated profile
- `PUT /api/users/me` — Update name or password
- `GET /api/admin/users` — *(ADMIN Only)* List all users system-wide
- `PUT /api/admin/users/{id}/role` — *(ADMIN Only)* Update user role (`USER` vs `ADMIN`)
- `PUT /api/admin/users/{id}/status` — *(ADMIN Only)* Activate/deactivate user account

### Groups (`/api/groups` & `/api/admin/groups`)
- `POST /api/groups` — Create a new group (creator becomes `OWNER`)
- `GET /api/groups` — List groups caller belongs to
- `GET /api/admin/groups` — *(ADMIN Only)* List all groups system-wide
- `GET /api/groups/{id}` — Get group details and member list
- `POST /api/groups/{id}/members` — Add member to group by email
- `DELETE /api/groups/{id}/members/{userId}` — Remove member or leave group (Blocked if net balance ≠ 0)
- `PUT /api/groups/{id}/archive` — *(Group OWNER or ADMIN Only)* Archive group

### Expenses (`/api/groups/{id}/expenses` & `/api/expenses`)
- `POST /api/groups/{id}/expenses` — Add expense with splits (`EQUAL`, `EXACT`, `PERCENTAGE`, `SHARES`)
- `GET /api/groups/{id}/expenses` — Get paginated, filterable expenses (date range, category, payer)
- `PUT /api/expenses/{id}` — Edit expense, recompute splits, log audit delta
- `DELETE /api/expenses/{id}` — Soft delete expense, recompute balances, log audit delta

### Balances & Settlements (`/api/groups/{id}`)
- `GET /api/groups/{id}/balances` — Compute dynamic net balance for all group members
- `GET /api/groups/{id}/settlement-plan` — Get greedy minimum-transaction payoff plan
- `POST /api/groups/{id}/settlements` — Record a debt payment (Status: `PENDING`)
- `PUT /api/settlements/{id}/confirm` — Receiver confirms payment (Status: `CONFIRMED`, adjusts net balances)

### Admin & Auditing (`/api/admin`)
- `GET /api/admin/expenses` — *(ADMIN Only)* System-wide paginated expense overview
- `GET /api/admin/audit-log` — *(ADMIN Only)* View system audit trail with JSON deltas

---

## 📐 Business Rules & Architecture Details

1. **Monetary Precision**: Every monetary calculation uses Java `BigDecimal` with 4 decimal places internally and 2 decimal places (`HALF_UP`) for user responses.
2. **Split Calculation & Rounding Leftovers**:
   - `EQUAL`: Total amount is divided equally among participants. Leftover remainder (e.g. $0.01 on $100 / 3) is assigned deterministically to the payer.
   - `EXACT`: Sum of exact split amounts must equal expense total within $0.01 tolerance.
   - `PERCENTAGE`: Sum of percentages must equal 100%. Leftover rounding cents are adjusted on the first participant.
   - `SHARES`: Proportional allocation based on share units. Rounding cents adjusted on first participant.
3. **Dynamic Net Balances**:
   - Balances are computed on demand: `netBalance = totalPaid - totalOwed + settlementsSent - settlementsReceived`.
   - Only `CONFIRMED` settlements alter member net balances.
4. **Member Removal Guard**:
   - Removing a member or leaving a group is blocked if `netBalance != 0`. Memberships are soft-deleted (`status = LEFT`, `leftAt = timestamp`) so historical expenses resolve accurately.
5. **Soft Deletion & Audit Logging**:
   - Expense deletion marks `isDeleted = true`.
   - Editing/deleting expenses or changing user roles/status logs JSON snapshots to `audit_logs`.

---

## 🧮 Settlement Algorithm & Complexity Trade-off

The `SettlementService.computeSettlementPlan(groupId)` method implements a **Greedy Heap-Based Algorithm**:

1. Calculate net balances for active group members.
2. Filter out members with zero balance.
3. Separate members into **Creditors** (positive balance) and **Debtors** (negative balance).
4. Maintain two Max-Heaps (priority queues): one for Creditors ordered by credit amount, one for Debtors ordered by debt amount.
5. Repeatedly pair the largest Creditor with the largest Debtor, settle `min(creditorBalance, debtorDebt)`, record the transaction, update balances, and re-insert into heaps if a balance remains.

---

## 🧪 Testing

To run JUnit 5 unit tests:
```bash
./gradlew test
```
