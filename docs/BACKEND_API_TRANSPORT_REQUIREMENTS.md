# Backend Requirements: API Transport for TransferApp

## Context

TransferApp currently uses SMS as the transport layer for agent-to-agent communication and admin-to-agent notifications. We are switching to an API-based transport where:

1. **Agents send transactions via HTTP** to the backend, which relays them to the receiving agent
2. **Agents receive real-time updates via WebSocket** (incoming transactions, status changes, admin actions)
3. **The backend becomes the central broker** — no more direct agent-to-agent SMS

The existing API spec is at `docs/transferapp-api.json`. The endpoints below are **new** — they do not exist yet.

---

## 1. New REST Endpoints

### 1.1 `POST /api/v1/transactions`

Send a single transaction from the authenticated agent to their partner.

**Auth:** Bearer token (agent)

**Request body:**
```json
{
  "partnerId": "uuid",
  "amount": 50000,
  "feeIncluded": true,
  "senderCityId": "uuid",
  "receiverCityId": "uuid",
  "senderPhone": "+23566000000",
  "senderName": "Abdoulaye",
  "receiverPhone": "+23566111111",
  "receiverName": "Moussa"
}
```

**Response:** `TransactionResponse` (same schema as existing, with server-generated `id`, computed `fee`, `status = SENT`, `direction = OUTGOING`)

**Backend behavior:**
- Validate the pairing exists and is active between the authenticated agent and `partnerId`
- Compute the fee from commission rates based on `senderCityId` / `receiverCityId` / `amount`
- Persist the transaction with `direction=OUTGOING` for the sender and `direction=INCOMING` for the receiver
- Push a `NEW_TRANSACTION` WebSocket event to the partner agent (see Section 2)
- Return the created transaction

---

### 1.2 `POST /api/v1/transactions/{id}/collected`

Mark a transaction as collected by the receiving agent.

**Auth:** Bearer token (agent — must be the receiving agent for this transaction)

**Response:** Updated `TransactionResponse` with new status

**Backend behavior:**
- Validate the authenticated agent is the receiver of this transaction
- If `amount >= company.collectionApprovalThreshold`, set status to `COLLECTION_PENDING_APPROVAL` (admin must approve)
- Otherwise, set status to `COLLECTED`
- Push a `STATUS_CHANGED` WebSocket event to the sending agent
- If pending approval, also notify admin (optional — admin dashboard polls)

---

### 1.3 `POST /api/v1/transactions/{id}/confirm`

Confirm receipt of an incoming transaction (acknowledge it was received).

**Auth:** Bearer token (agent — must be the receiving agent)

**Response:** Updated `TransactionResponse` with `status = CONFIRMED`

**Backend behavior:**
- Set status from `SENT` to `CONFIRMED`
- Push a `STATUS_CHANGED` WebSocket event to the sending agent

---

### 1.4 `GET /api/v1/agents/me/transactions`

Fetch the authenticated agent's transactions (both directions).

**Auth:** Bearer token (agent)

**Query params (all optional):**
- `since` — ISO 8601 timestamp, only return transactions created after this time (for incremental sync)
- `status` — filter by status (comma-separated list)
- `limit` — max results (default 100)
- `offset` — pagination offset

**Response:**
```json
{
  "transactions": [TransactionResponse],
  "total": 247
}
```

**Notes:**
- Return both OUTGOING and INCOMING transactions for this agent
- Order by `createdAt` descending
- The `since` param is important for the mobile app's incremental sync

---

## 2. WebSocket Endpoint

### `GET /ws/agent` (upgrade to WebSocket)

**Auth:** The agent connects with their Bearer token. Options:
- Query param: `?token=<jwt>` (simplest for OkHttp)
- Or `Authorization` header during handshake

**Connection lifecycle:**
- On connect: register the agent's WebSocket session (keyed by `agentId`)
- On disconnect: remove the session; the mobile app will reconnect automatically
- Heartbeat: server sends `PING` every 30s, client responds with `PONG`. If no PONG received within 10s, close the connection.

### Event Format

All messages are JSON:

```json
{
  "type": "EVENT_TYPE",
  "payload": { ... }
}
```

### Event Types

| Type | Payload | When sent |
|------|---------|-----------|
| `NEW_TRANSACTION` | `TransactionResponse` | A partner sent a transaction to this agent |
| `STATUS_CHANGED` | `{ "transactionId": "uuid", "newStatus": "COLLECTED" }` | A transaction's status changed (collected, confirmed, reversed, etc.) |
| `REVERSAL_APPROVED` | `{ "transactionId": "uuid" }` | Admin approved a reversal request for this agent's transaction |
| `COLLECTION_APPROVED` | `{ "transactionId": "uuid" }` | Admin approved a collection that was pending approval |
| `PAIRING_CREATED` | `PairingResponse` | Admin created a new pairing involving this agent |
| `PAIRING_DELETED` | `{ "pairingId": "uuid" }` | Admin deactivated a pairing involving this agent |
| `CASH_ADJUSTMENT` | `CashAdjustmentResponse` | Admin made a cash adjustment for this agent |

### Delivery Guarantees

- **Best effort, not guaranteed.** If the agent is offline when an event fires, they will pick it up on next sync (via `GET /agents/me/transactions?since=...` and the existing sync endpoints).
- The mobile app will do a full sync on reconnect, so missed WebSocket events are harmless.

---

## 3. Modifications to Existing Endpoints

### 3.1 `POST /api/v1/reversals`

Currently exists. **Add:** after creating the reversal, push a `STATUS_CHANGED` WebSocket event to the partner with `newStatus: REVERSAL_REQUESTED`.

### 3.2 `POST /api/v1/reversals/{id}/confirm`

Currently exists. **Add:** push `STATUS_CHANGED` with `newStatus: REVERSED` to the partner.

### 3.3 `POST /api/v1/reversals/{id}/approve`

Currently exists. **Add:** push `REVERSAL_APPROVED` WebSocket event to both agents involved.

### 3.4 `POST /api/v1/collections/{txnId}/approve`

Currently exists. **Add:** push `COLLECTION_APPROVED` WebSocket event to both agents involved.

### 3.5 `POST /api/v1/agents/{id}/cash-adjustment`

Currently exists. **Add:** push `CASH_ADJUSTMENT` WebSocket event to the target agent.

### 3.6 `POST /api/v1/pairings`

Currently exists. **Add:** push `PAIRING_CREATED` WebSocket event to both agents in the pairing.

### 3.7 `DELETE /api/v1/pairings/{id}`

Currently exists. **Add:** push `PAIRING_DELETED` WebSocket event to both agents in the pairing.

---

## 4. Summary

| # | Endpoint | Method | Status |
|---|----------|--------|--------|
| 1 | `/api/v1/transactions` | POST | **NEW** |
| 2 | `/api/v1/transactions/{id}/collected` | POST | **NEW** |
| 3 | `/api/v1/transactions/{id}/confirm` | POST | **NEW** |
| 4 | `/api/v1/agents/me/transactions` | GET | **NEW** |
| 5 | `/ws/agent` | WebSocket | **NEW** |
| 6 | `/api/v1/reversals` | POST | Existing — add WS push |
| 7 | `/api/v1/reversals/{id}/confirm` | POST | Existing — add WS push |
| 8 | `/api/v1/reversals/{id}/approve` | POST | Existing — add WS push |
| 9 | `/api/v1/collections/{txnId}/approve` | POST | Existing — add WS push |
| 10 | `/api/v1/agents/{id}/cash-adjustment` | POST | Existing — add WS push |
| 11 | `/api/v1/pairings` | POST | Existing — add WS push |
| 12 | `/api/v1/pairings/{id}` | DELETE | Existing — add WS push |

**Priority order:** Endpoints 1, 4, 5 are critical (send transaction, fetch transactions, real-time). Endpoints 2-3 are next. The WS pushes on existing endpoints (6-12) can be added incrementally.
