# Backend Requirements — Encrypted Agent Sync & Admin-to-Agent Communication

## Context

The Android app needs to receive admin operations (cash adjustments, pairings, approvals) both via API and SMS. All data exchanged between backend and agents must be **encrypted** using a per-agent key. This document describes exactly what the backend needs to support.

## 1. Admin-Agent Encryption Key (`adminSecret`)

Each agent gets a unique AES-256 key shared between the admin/backend and that specific agent.

### When to generate
- At **agent approval** time (`POST /api/v1/agents/{id}/approve`)
- Generate a 256-bit random key, hex-encoded (64 hex chars)
- Store it on the agent record in the database

### Where it's used
- Backend encrypts API responses meant for that agent
- Admin app uses it to encrypt SMS messages sent to that agent
- Agent stores it locally and uses it to decrypt both API and SMS data

### What changes in the approve endpoint
`POST /api/v1/agents/{id}/approve` should:
1. Generate the `adminSecret` (256-bit key, hex-encoded)
2. Store it on the agent record
3. Return it in the `AgentResponse` so the admin app has it

**Updated `AgentResponse`**:
```json
{
  "id": "uuid",
  "name": "Agent Name",
  "phone": "+235XXXXXXXX",
  "cityId": "uuid",
  "cityName": "N'Djamena",
  "role": "AGENT",
  "approvalStatus": "APPROVED",
  "initialCash": 500000,
  "adminSecret": "a3f2b8c1...64_hex_chars",
  "createdAt": "2026-09-10T12:00:00Z",
  "isActive": true
}
```

The `adminSecret` field should:
- Be included in responses to admin users only (not exposed to other agents)
- Be included when the agent fetches their own data (so they can store it locally)

## 2. New Agent-Specific Endpoints

These endpoints return data for the **authenticated agent** (identified by JWT). The response payloads must be **encrypted** with that agent's `adminSecret`.

### `GET /api/v1/agents/me/pairings`

Returns all active pairings involving the authenticated agent.

**Response** (before encryption):
```json
[
  {
    "id": "pairing-uuid",
    "agent1Id": "uuid",
    "agent1Name": "Agent A",
    "agent1City": "N'Djamena",
    "agent1Phone": "+235XXXXXXXX",
    "agent2Id": "uuid",
    "agent2Name": "Agent B",
    "agent2City": "Moundou",
    "agent2Phone": "+235YYYYYYYY",
    "sharedSecret": "hex-encoded-shared-secret",
    "isActive": true,
    "createdAt": "2026-09-07T22:32:53Z"
  }
]
```

**Important**: Include `agent1Phone` and `agent2Phone` — the agent needs the partner's phone number for SMS communication. These fields are not in the current admin `PairingResponse` and need to be added.

### `GET /api/v1/agents/me/adjustments`

Returns all cash adjustments for the authenticated agent.

**Response** (before encryption):
```json
[
  {
    "id": "adjustment-uuid",
    "agentId": "uuid",
    "amount": 50000,
    "reason": "Cash injection for operations",
    "performedByAdminId": "admin-uuid",
    "createdAt": "2026-09-10T14:30:00Z"
  }
]
```

### `GET /api/v1/agents/me/profile`

Returns the authenticated agent's own data (including `adminSecret` so they can decrypt future messages).

**Response**:
```json
{
  "id": "uuid",
  "name": "Agent Name",
  "phone": "+235XXXXXXXX",
  "cityId": "uuid",
  "cityName": "N'Djamena",
  "role": "AGENT",
  "approvalStatus": "APPROVED",
  "initialCash": 500000,
  "adminSecret": "hex-encoded-key",
  "companyId": "uuid",
  "createdAt": "2026-09-10T12:00:00Z",
  "isActive": true
}
```

## 3. Encryption Format for API Responses

All agent-specific endpoints (`/agents/me/*`) should return encrypted payloads.

### Encryption algorithm
- **AES-256-GCM** (same as SMS encryption)
- Key: the agent's `adminSecret` (hex-decoded to 32 bytes)
- IV: 12 random bytes, generated per response
- Output: Base64-encoded string of `IV + ciphertext + auth_tag`

### Response format
```json
{
  "encrypted": "base64-encoded-iv-ciphertext-tag"
}
```

The Android app will:
1. Base64-decode the `encrypted` field
2. Extract IV (first 12 bytes), ciphertext, and auth tag
3. Decrypt with AES-256-GCM using the agent's `adminSecret`
4. Parse the resulting JSON

### Why encrypt API responses?
Even though HTTPS encrypts in transit, we want end-to-end encryption so:
- Shared secrets (pairing keys) are never stored in plaintext on the server response layer
- A compromised server or proxy cannot read agent-specific financial data
- Same encryption logic works for both SMS and API, keeping the codebase unified

## 4. Changes to Existing Endpoints

### `POST /api/v1/agents/{id}/approve`
- Generate `adminSecret` (256-bit, hex)
- Store on agent record
- Return in response

### `GET /api/v1/pairings` (admin endpoint)
- Add `agent1Phone` and `agent2Phone` to `PairingResponse`
- Admin needs these to send SMS to agents when creating/deleting pairings

### `POST /api/v1/pairings` (create pairing)
- Still returns `Response 201` with empty body (or just `{"status": "created"}`)
- The admin app will handle sending SMS to both agents

### `DELETE /api/v1/pairings/{id}` (delete pairing)
- Before deleting, return the pairing data (agent IDs and phones) so the admin app can notify agents via SMS
- Or: change to return the deleted pairing info in the response body

### `POST /api/v1/agents/{id}/cash-adjustment`
- Still stores the adjustment on the backend
- Return the created adjustment in the response body (the admin app needs the `id` to include in the SMS)
- Response:
```json
{
  "id": "new-adjustment-uuid",
  "agentId": "uuid",
  "amount": 50000,
  "reason": "Cash injection",
  "performedByAdminId": "admin-uuid",
  "createdAt": "2026-09-10T14:30:00Z"
}
```

## 5. SMS Message Types (for reference)

The Android admin app will send these SMS messages to agents. The backend doesn't send SMS directly, but this is here for context on what data flows look like.

| Type | Format | Encrypted with |
|------|--------|---------------|
| `ADJ` | `ADJ\|adjustmentId\|amount\|reason\|adminId\|timestamp` | Agent's `adminSecret` |
| `PAIR` | `PAIR\|pairingId\|partnerAgentId\|partnerName\|partnerCity\|partnerPhone\|sharedSecret\|timestamp` | Agent's `adminSecret` |
| `UNPAIR` | `UNPAIR\|pairingId\|timestamp` | Agent's `adminSecret` |
| `RAPPR` | `RAPPR\|txnId\|timestamp` | Agent's `adminSecret` |
| `CAPPR` | `CAPPR\|txnId\|timestamp` | Agent's `adminSecret` |

## 6. Summary of Backend Tasks

1. **Add `adminSecret` column** to agents table (nullable, populated at approval)
2. **Generate key at approval** — `POST /agents/{id}/approve` generates and stores `adminSecret`
3. **Add phone fields to PairingResponse** — `agent1Phone`, `agent2Phone`
4. **Create `/agents/me/pairings`** — returns pairings for authenticated agent
5. **Create `/agents/me/adjustments`** — returns adjustments for authenticated agent
6. **Create `/agents/me/profile`** — returns agent's own data including `adminSecret`
7. **Encrypt `/agents/me/*` responses** — AES-256-GCM with agent's `adminSecret`
8. **Return created adjustment in response body** — `POST /agents/{id}/cash-adjustment` should return the new adjustment (not empty body)
9. **Return deleted pairing info** — `DELETE /pairings/{id}` should return pairing data before deletion (or change to POST with response)
