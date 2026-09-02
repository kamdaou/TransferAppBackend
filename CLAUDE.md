# TransferApp Backend — Claude Instructions

## Project Overview

Spring Boot + PostgreSQL backend for **TransferApp**, a multi-tenant SMS-based money transfer platform for transfer companies in Chad. This backend handles admin operations, authentication, company configuration, and data sync. The Android app (`~/AndroidStudioProjects/TransferApp`) is the companion client.

**Package:** `com.ktconsulting.transfer_app`
**Stack:** Spring Boot 4.1.1, Kotlin 2.3.21, PostgreSQL, JWT auth
**Status:** Fresh project — only the skeleton exists. Everything below needs to be built.

## Architecture

Standard Spring Boot layered architecture:

```
Controller (REST endpoints)
    ↓
Service (business logic)
    ↓
Repository (Spring Data JPA)
    ↓
PostgreSQL
```

### Package Structure (to create)

```
com.ktconsulting.transfer_app/
├── config/              # Security config, JWT config, CORS
├── controller/          # REST controllers grouped by role
├── dto/                 # Request/response DTOs
│   ├── request/
│   └── response/
├── entity/              # JPA entities
├── enum/                # Enums (UserRole, ApprovalStatus, TransactionStatus, etc.)
├── exception/           # Custom exceptions + global handler
├── repository/          # Spring Data JPA repositories
├── security/            # JWT filter, UserDetailsService, auth utilities
└── service/             # Business logic services
```

## Business Context

Transfer companies in Chad use agent networks. Agents collect cash from senders and use the app to notify receiving agents (via SMS) to pay out. This backend handles everything that requires internet: authentication, company setup, agent approval, admin operations.

### Roles

| Role | What they do |
|------|-------------|
| **Super Admin** (app owner) | Creates companies, manages company admins |
| **Company Admin** | Approves agents, manages pairings/commissions, approves reversals, adjusts cash |
| **Agent** | Registers, syncs transactions, requests reversals |

### Key Business Rules

- **Multi-tenant**: Each company has its own agents, cities, commission rates, pairings
- **Currency**: FCFA (Long, no decimals)
- **Cash accounting is INVERTED**: Agent sends transfer → cash INCREASES (collected from sender). Incoming transfer collected → cash DECREASES (paid to receiver)
- **Agent onboarding**: Self-register with company code → PENDING → admin approves (sets initial cash) → APPROVED
- **Agent pairing**: Admin-managed. Only paired agents can transfer between each other. Pairing generates a shared secret for SMS encryption
- **Commission**: Fee based on sending city + receiving city + amount bracket. Per-company configuration
- **Fee inclusion**: Sender chooses — fee paid separately (default) or deducted from transfer amount
- **High-value approval**: Company sets a `collectionApprovalThreshold`. Collections above it require admin approval
- **Reversal**: 3-step — agent requests → admin approves → agent confirms cash returned
- **Cash adjustments**: Admin-only inject/withdraw with audit trail

## Domain Models (JPA Entities to Create)

### Company
```
id: UUID
companyCode: String (unique, used by agents to register)
name: String
logoUrl: String?
primaryColor: String? (hex color for white-labeling)
contacts: String? (company contact info)
collectionApprovalThreshold: Long (FCFA amount above which collection needs admin approval)
isActive: Boolean
createdAt: Timestamp
updatedAt: Timestamp
```

### City
```
id: UUID
companyId: UUID (FK → Company)
name: String
isActive: Boolean
```

### Agent (also the user for auth)
```
id: UUID
companyId: UUID (FK → Company)
cityId: UUID (FK → City)
name: String
phone: String (unique per company)
pin: String (hashed)
role: UserRole (AGENT or COMPANY_ADMIN)
approvalStatus: ApprovalStatus (PENDING, APPROVED, REJECTED)
initialCash: Long (set by admin at approval, FCFA)
isActive: Boolean
createdAt: Timestamp
updatedAt: Timestamp
```

### AgentPairing
```
id: UUID
companyId: UUID (FK → Company)
agent1Id: UUID (FK → Agent)
agent2Id: UUID (FK → Agent)
sharedSecret: String (generated, used for SMS encryption)
isActive: Boolean
createdAt: Timestamp
```

### CommissionRate
```
id: UUID
companyId: UUID (FK → Company)
sendingCityId: UUID (FK → City)
receivingCityId: UUID (FK → City)
minAmount: Long
maxAmount: Long
fee: Long (FCFA)
```

### Transaction
```
id: UUID
companyId: UUID (FK → Company)
agentId: UUID (FK → Agent, the sending agent)
partnerId: UUID (FK → Agent, the receiving agent)
amount: Long (what receiver gets, FCFA)
fee: Long (commission, FCFA)
feeIncluded: Boolean (true = fee deducted from sender's stated amount)
senderCityId: UUID (FK → City)
receiverCityId: UUID (FK → City)
senderPhone: String
senderName: String
receiverPhone: String
receiverName: String
direction: TransactionDirection (OUTGOING, INCOMING — relative to agentId)
status: TransactionStatus
reversalReason: String?
createdAt: Timestamp
updatedAt: Timestamp
```

### CashAdjustment
```
id: UUID
agentId: UUID (FK → Agent)
companyId: UUID (FK → Company)
amount: Long (positive = inject, negative = withdraw)
reason: String
performedByAdminId: UUID (FK → Agent with COMPANY_ADMIN role)
createdAt: Timestamp
```

### TransferLimits (per company, can be stored in Company or separate table)
```
companyId: UUID (FK → Company)
minPerTransaction: Long
maxPerTransaction: Long
dailyCap: Long
```

## Enums

```kotlin
enum class UserRole { AGENT, COMPANY_ADMIN, SUPER_ADMIN }
enum class ApprovalStatus { PENDING, APPROVED, REJECTED }
enum class TransactionStatus {
    PENDING, SENT, CONFIRMED, COLLECTED,
    REVERSAL_REQUESTED, REVERSAL_APPROVED, REVERSED,
    COLLECTION_PENDING_APPROVAL
}
enum class TransactionDirection { OUTGOING, INCOMING }
```

## API Endpoints to Build

### Public (no auth)
| Method | Path | Description |
|--------|------|-------------|
| `GET` | `/api/config/{companyCode}` | Download company config (branding, cities, commission rates, limits) |

### Auth (no auth required)
| Method | Path | Description |
|--------|------|-------------|
| `POST` | `/api/auth/register` | Agent self-registration (name, phone, pin, company code, city) |
| `POST` | `/api/auth/login` | Login → returns JWT with role |

### Agent (JWT required, role=AGENT)
| Method | Path | Description |
|--------|------|-------------|
| `POST` | `/api/transactions/sync` | Push local transactions to server |
| `POST` | `/api/reversals` | Request a reversal (txnId + reason) |
| `POST` | `/api/reversals/{id}/confirm` | Confirm cash was returned (final step) |
| `GET` | `/api/agents/me` | Get own agent profile + approval status |

### Company Admin (JWT required, role=COMPANY_ADMIN)
| Method | Path | Description |
|--------|------|-------------|
| `GET` | `/api/agents/pending` | List agents pending approval |
| `POST` | `/api/agents/{id}/approve` | Approve agent + set initial cash |
| `POST` | `/api/agents/{id}/reject` | Reject agent |
| `GET` | `/api/agents` | List all agents in company |
| `GET` | `/api/agents/{id}/balance` | Compute agent's cash balance |
| `POST` | `/api/agents/{id}/cash-adjustment` | Inject/withdraw cash |
| `GET` | `/api/pairings` | List agent pairings |
| `POST` | `/api/pairings` | Create pairing (generates shared secret) |
| `DELETE` | `/api/pairings/{id}` | Deactivate pairing |
| `GET` | `/api/commissions` | List commission rates |
| `POST` | `/api/commissions` | Create/update commission rate |
| `PUT` | `/api/commissions/{id}` | Update commission rate |
| `DELETE` | `/api/commissions/{id}` | Delete commission rate |
| `GET` | `/api/reversals/pending` | List pending reversals |
| `POST` | `/api/reversals/{id}/approve` | Approve reversal |
| `POST` | `/api/reversals/{id}/reject` | Reject reversal |
| `GET` | `/api/collections/pending` | List high-value collections pending approval |
| `POST` | `/api/collections/{txnId}/approve` | Approve high-value collection |
| `POST` | `/api/collections/{txnId}/reject` | Reject high-value collection |
| `GET` | `/api/cities` | List cities |
| `POST` | `/api/cities` | Create city |
| `PUT` | `/api/cities/{id}` | Update city |

### Super Admin (JWT required, role=SUPER_ADMIN)
| Method | Path | Description |
|--------|------|-------------|
| `POST` | `/api/companies` | Create company |
| `GET` | `/api/companies` | List companies |
| `PUT` | `/api/companies/{id}` | Update company |

## Accounting Model (Balance Computation)

```
Agent Cash Balance =
    Initial Cash (set by admin at onboarding)
  + Sum(outgoing txn amount + fee) where status >= SENT     [cash collected from senders]
  - Sum(incoming txn amount) where status == COLLECTED        [cash paid to receivers]
  + Sum(cash adjustments)                                     [admin inject/withdraw]

Available Cash = Cash Balance - Pending Payouts
Pending Payouts = Sum(incoming txn amount) where status == CONFIRMED  [not yet collected]
```

The balance endpoint should compute this from the database, not store a running total.

## Security

- **JWT** for API authentication (issued at login, contains agentId, companyId, role)
- **PIN hashing**: BCrypt for agent PINs
- **Shared secrets**: Generated server-side when admin creates a pairing. Used by Android app for AES-256-GCM SMS encryption. The backend stores them and provides them to paired agents
- **Role-based access**: Use Spring Security with method-level `@PreAuthorize` or filter chain role checks
- **Multi-tenant isolation**: Every query must be scoped to the agent's companyId (extracted from JWT). An agent/admin must never see data from another company

## Dependencies to Add

The project currently only has `spring-boot-starter-webmvc`. Add:

```kotlin
// PostgreSQL + JPA
implementation("org.springframework.boot:spring-boot-starter-data-jpa")
runtimeOnly("org.postgresql:postgresql")

// Security + JWT
implementation("org.springframework.boot:spring-boot-starter-security")
implementation("io.jsonwebtoken:jjwt-api:0.12.6")
runtimeOnly("io.jsonwebtoken:jjwt-impl:0.12.6")
runtimeOnly("io.jsonwebtoken:jjwt-jackson:0.12.6")

// Validation
implementation("org.springframework.boot:spring-boot-starter-validation")
```

## application.properties (to configure)

```properties
spring.application.name=transferapp

# PostgreSQL
spring.datasource.url=jdbc:postgresql://localhost:5432/transferapp
spring.datasource.username=postgres
spring.datasource.password=postgres
spring.jpa.hibernate.ddl-auto=update
spring.jpa.show-sql=false
spring.jpa.properties.hibernate.dialect=org.hibernate.dialect.PostgreSQLDialect

# JWT
app.jwt.secret=<generate-a-secure-256-bit-key>
app.jwt.expiration-ms=86400000

# Server
server.port=8080
```

## Build Order

1. **Dependencies + config** — Add JPA, PostgreSQL, Security, JWT, Validation to `build.gradle.kts`. Configure `application.properties`
2. **Enums + Entities** — Create all JPA entities with proper relationships and constraints
3. **Repositories** — Spring Data JPA interfaces
4. **Security** — JWT generation/validation, Spring Security filter chain, role-based access
5. **Auth endpoints** — Register + Login
6. **Company config endpoint** — `GET /api/config/{companyCode}` (public, returns full config for white-labeling)
7. **Admin endpoints** — Agent approval, pairings, commissions, reversals, cash adjustments, balance
8. **Agent endpoints** — Transaction sync, reversal request/confirm
9. **Super admin endpoints** — Company CRUD
10. **Error handling** — Global exception handler with consistent error responses

## Rules

- All monetary values are `Long` (FCFA, no decimals)
- UUIDs for all entity IDs
- Every query scoped to companyId for multi-tenant isolation
- DTOs for all request/response bodies (never expose entities directly)
- Validate all inputs with Bean Validation (`@Valid`, `@NotBlank`, `@Min`, etc.)
- Use `@Transactional` on service methods that write
- Return proper HTTP status codes (201 for creation, 404 for not found, 403 for unauthorized, 409 for conflicts)
- Shared secrets are generated server-side (use `java.security.SecureRandom`, 32 bytes, hex-encoded)
- Commission rate lookup: find rate where sendingCityId + receivingCityId match AND amount is between minAmount and maxAmount
- The Android companion app is at `~/AndroidStudioProjects/TransferApp` — refer to its `CLAUDE.md` and `docs/` for full business context
