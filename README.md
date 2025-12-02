# SmartAir – Overview and Architecture

## What this app does
- Tracks asthma management for parents, children, and providers.
- Supports child accounts (username/password stored in Firestore), parent Firebase Auth accounts, and provider Firebase Auth accounts.
- Core features: symptom check-ins, PEF logging with zone guidance, medicine logs (rescue/controller), triage sessions, inventory, badges, sharing controls, invite-based provider access, provider dashboards/reports.

## Key flows
- **Auth**: Parents/providers use Firebase Auth email/password. Children log in via username lookup in Firestore (`usernames/{username}` → `users/{parent}/children/{child}` with stored password).
- **Child data model** (Firestore):
  - Parent space: `users/{parentId}/children/{childId}` (username, password, name, dob, notes, role, timestamps).
  - Child profile: `users/{childId}` (mirrors child data, latestZone*, personalBest, etc.).
  - Username map: `usernames/{username}` → childUid, parentId, email/password (plaintext – security caveat).
  - Child collections: `medicine_logs`, `pef_entries`, `prepost_checks`, `triage_sessions`, `inventory`, `zone_history`, `settings/sharing`.

- **Sharing & invites**:
  - Parent toggles sharing per child at `users/{parent}/children/{child}/settings/sharing`.
  - Provider-facing copy kept at `providers/{provider}/linkedChildren/{child}/settings/sharing`.
  - Invite codes: `inviteCodes/{code}` with parentId, childId, expires, used.
  - Redeem flow (`ShareLogActivity`) persists link: `providers/{provider}/linkedChildren/{child}` and `parents/{parent}/children/{child}/linkedProviders/{provider}`; copies sharing settings.
  - Linked children list: `ProviderLinkedChildrenActivity` reads `providers/{provider}/linkedChildren` to reopen without retyping code.

- **Provider gating**:
  - `DashboardProvidersActivity` reads sharing settings and hides sections (rescue/controller/symptom/triage/PEF/chart) when toggles are off.
  - Alerts: red zone today, rapid rescue (≥3 in 3h), worse-after-dose (“after” + “worse” today), triage escalation (latest triage RED/emergency), inventory low/expired.
  - PDF export builds a simple report (tiles only) to app downloads directory.

- **Child flows**:
  - PEF logging with pre/post tags, zone calculation using `personalBest`.
  - Personal Best set by parent via `SetPersonalBestDialog`; syncs to `users/{child}` and `users/{parent}/children/{child}`.
  - Triage sessions logged under `users/{child}/triage_sessions`.
  - Medicine logs under `users/{child}/medicine_logs`.
  - Symptom check-ins under `symptomCheckIns/{child}/daily`.
  - Badges: `users/{uid}/badges/{badgeId}`.

## Firestore collections (primary)
- `users/{uid}` – parent or child profile.
- `users/{parentId}/children/{childId}` – child under parent.
- `users/{childId}/pef_entries`, `medicine_logs`, `prepost_checks`, `triage_sessions`, `inventory`, `zone_history`, `settings/sharing`.
- `symptomCheckIns/{childId}/daily` – symptom check-ins.
- `inviteCodes/{code}` – invite metadata.
- `providers/{providerId}/linkedChildren/{childId}` (+ `settings/sharing`).
- `parents/{parentId}/children/{childId}/linkedProviders/{providerId}`.
- `usernames/{username}` – maps child username → childUid/parentId (contains password, see risks).

## Security and privacy caveats
- Child passwords are stored in Firestore in plaintext in multiple places; this is a major security risk. Consider moving children to Firebase Auth or hashing/removing passwords.
- Firestore rules are not locked down to enforce sharing toggles; add rules to gate provider reads per sharing flags.
- Inventory/health data stored without encryption; use caution on client storage.

## Notable UX screens/components
- Parent: child management, sharing toggles, PB set dialog, inventory, symptom check-in, triage, badges.
- Child: PEF entry with zone card, triage, symptom check-in, badges.
- Provider: Invite redeem, linked-children list (name/username), dashboard (tiles, trend, alerts), PDF export.

## Known gaps
- PDF export includes only summary tiles (no charts or detailed series).
- Sharing enforcement in UI is only implemented on provider dashboard; other provider views should also gate queries.
- Firestore rules need to be added to enforce sharing toggles and restrict provider scope.
- Password storage needs remediation.

## Build / environment
- Android app module `app`.
- Firebase Firestore and Auth required.
- Compile/target SDK 36, Java 11.
