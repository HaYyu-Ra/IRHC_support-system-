# IRHC Database Design
**Database:** PostgreSQL (Supabase-managed)  
**Author:** Hayyu Ragea Chewaka

## Design principles
Use UUID primary keys, foreign keys, timestamps in UTC, explicit status values, database constraints and least-privilege RLS. Store the minimum personal information necessary. Keep clinical notes and message bodies out of general-purpose analytics. The SQL file at `web/supabase/schema.sql` is the repository's implementation baseline; this document describes the logical model and does not replace reviewing that migration against the deployed schema.

## Logical entities
| Entity | Purpose | Key relationships |
|---|---|---|
| profiles | Application identity, display name and account status linked to Auth user | One profile per Auth user |
| providers | Verified provider details and service information | Provider profile references profiles |
| provider_availability | Provider time slots and availability state | Many slots per provider |
| appointments | Member request, assigned provider, requested time and workflow status | Member and provider references |
| consultation_threads | Private consultation context and lifecycle | Linked to appointment/member/provider as appropriate |
| consultation_messages | Messages within a consultation thread | Many messages per thread; sender references profile |
| health_resources | Educational content, language, category, source and publication state | Author/reviewer references profiles |
| notifications | User-specific event and read state | Recipient references profile |
| audit_events | Security-relevant administrative and workflow actions | Actor references profile where available |

## Recommended fields
- **profiles:** id (Auth UUID), display_name, phone (only if needed), role, account_status, consent_version, consented_at, created_at, updated_at.
- **providers:** id, profile_id, professional_title, specialty, facility, service_area, verification_status, verified_at.
- **provider_availability:** id, provider_id, starts_at, ends_at, availability_status, created_at.
- **appointments:** id, member_id, provider_id, starts_at, status, request_category, member_note (optional and minimized), created_at, updated_at, cancellation_reason.
- **consultation_threads:** id, appointment_id, member_id, provider_id, status, opened_at, closed_at.
- **consultation_messages:** id, thread_id, sender_id, body, created_at, read_at. Message content is highly sensitive and requires strict participant-only access.
- **health_resources:** id, title, summary, body, language, category, source_reference, review_status, reviewed_by, reviewed_at, published_at, created_at, updated_at.
- **notifications:** id, recipient_id, notification_type, title, body, read_at, created_at.
- **audit_events:** id, actor_id, action, entity_type, entity_id, occurred_at, metadata_minimized. Never copy message bodies or unnecessary health details into metadata.

## Relationship model
- Auth user 1:1 profile.
- Profile 1:N appointments as member; provider profile 1:N appointments as provider.
- Provider 1:N availability slots.
- Appointment 0:1 or 1:N consultation threads according to institutional workflow.
- Thread 1:N messages.
- Profile 1:N notifications.
- Profile 1:N authored/reviewed resources.
- Profile 1:N audit events as actor.

## Integrity and access controls
1. Use foreign keys with deliberate deletion behavior; avoid cascading deletion of clinical/audit records without an approved retention policy.
2. Restrict appointment status transitions to allowed states: requested, confirmed, reschedule_requested, cancelled, completed, declined.
3. Validate appointment time ranges and prevent overlapping provider bookings through transactional constraints or server-side locking.
4. Enable RLS on every table containing member, provider, message, notification or audit information.
5. Members may read/update only their own profile and read only their own appointments, threads, messages and notifications.
6. Providers may read assigned appointments and threads; providers may send messages only as themselves within assigned open threads.
7. Public access is limited to published resources and approved provider-directory fields.
8. Reviewer and administrator permissions are granted by trusted staff provisioning, not public profile input.
9. Reporting access should use aggregate views and avoid exposing identifiable rows.
10. Test both allowed and denied cases with separate test accounts before deployment.

## Operational safeguards
Use migrations under version control, separate development/staging/production projects, scheduled encrypted backups, periodic restore tests, monitoring, database connection limits, secret rotation and documented incident response. Never commit service-role keys, production credentials, real patient data or unredacted exports.
