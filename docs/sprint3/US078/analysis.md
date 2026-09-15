# US078 — Analysis

## Goal

Air Transport Company Collaborators access fleet and pilot operations remotely via a dedicated TCP client (no direct database access).

## Scope (Sprint 3)

| US | Feature | Server support |
|----|---------|----------------|
| US070 | Register aircraft | Opcode 21 |
| US071 | Decommission aircraft | Opcodes 22, 23 |
| US072 | List fleet (filters) | Opcodes 24–26 |
| US075 | Add pilot (new + existing user) | Opcodes 30–31, 33–34 |
| US076 | List pilots | Opcode 32 |
| US073 | Create route | Opcode 27 |
| US074 | Deactivate route | Opcodes 28, 39 (list active routes) |
| US077 | Remove pilot | Opcode 29 |

## Architecture

- **Client:** `aisafe.rcomp.tcpclient` → `RemoteClientApp`
- **Protocol:** `aisafe.rcomp.protocol` (shared framing)
- **Server:** `aisafe.rcomp.server` → `ClientHandler` + `AtccCommandHandler`
- **Domain:** existing `aisafe.core` controllers; AuthZ via `AuthzRegistry` session after login

## Actors

- Authenticated user with role `AIR_TRANSPORT_COMPANY_COLLABORATOR`
- Bootstrap test users: `atcc1` / `password123` (company TP)
