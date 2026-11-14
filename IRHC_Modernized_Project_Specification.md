# Design and Implementation of an Integrated Reproductive Health Awareness and Consultation Platform (IRHC)

**Modernized technical project specification — November 2026**

**Original project:** HiLCoE School of Computer Science and Technology, November 2020. **Original author shown in source:** Damto Ragea Chewaka.

## Abstract
This modernization carries forward the original project's goal of improving access to reproductive-health awareness and consultation through digital services. The original paper identified paper-based workflows, fragmented information, limited confidential communication, distance barriers and the cost of repeated awareness/training. The updated platform includes public education, community registration, provider discovery, appointment requests, protected consultation messaging, notifications, content review, staff administration and aggregate reporting. The proposed data layer uses PostgreSQL with row-level security and managed authentication. The included web client is a functional demonstration; cloud persistence and clinical workflows require backend configuration and institutional approval.

## 1. Objectives
- Provide reviewed reproductive-health education in text and accessible media.
- Enable community account and profile management.
- Support provider discovery, availability and appointment requests.
- Support confidential, access-controlled consultation threads.
- Enable provider scheduling, content drafting and appointment workflows.
- Provide managers aggregate reports and administrators controlled account management.
- Support responsive web and Android clients.

## 2. User roles
Guest, community member, healthcare professional, manager/reviewer and administrator. Privileged roles must be provisioned through a trusted workflow, not self-selected at public registration.

## 3. Functional requirements
Public education library; managed authentication; role/ownership enforcement; profile management; provider directory and availability; appointment request/confirmation/cancellation; private consultation messages; notifications; content drafting and review; aggregate reporting; staff administration; auditability; mobile-responsive layouts.

## 4. Non-functional requirements
Security (TLS, MFA for privileged users, least privilege, RLS, protected secrets, audit trails); privacy (consent, data minimization, retention/deletion); availability (monitoring, backups, restore tests); performance (pagination and optimized assets); accessibility (plain language, contrast, keyboard support, captions); maintainability and resilience.

## 5. Architecture
Presentation: responsive web client and Android companion. Identity: managed authentication. Data: PostgreSQL tables for profiles, providers, availability, appointments, consultation threads/messages, health resources, notifications and audit events. Operations: HTTPS hosting, environment secrets, logs, backups and monitoring. Browser code must never contain a service-role key.

## 6. Testing and acceptance
Verify public access to published content; member isolation; cross-user appointment and consultation denial; provider/staff permissions; content approval; aggregate manager reporting; privileged admin workflow; responsive layouts; honest error states; and tested backup restoration.

## 7. Deployment
Provision approved Supabase project; configure Auth; review/apply schema and test RLS; configure client URL and anon key; deploy static web assets to Render; configure HTTPS/domain/monitoring; complete clinical, privacy/legal, accessibility and security reviews; pilot with synthetic data; obtain sign-off before production.

## 8. Safety and limitations
The system is not an emergency service, medical device, diagnostic tool or prescribing system. The demo uses browser-local appointment data and illustrative content/metrics. Do not enter real patient data. Production use requires qualified clinical content review, provider verification, informed consent, institutional ownership, privacy/legal review, independent security testing, data retention and incident response processes.

## 9. Future work
Oromo and Amharic localization with clinical translation review; accessible audio/video learning; SMS reminders; live video integration; offline-first mobile synchronization; approved health-system interoperability; advanced de-identified analytics and formal usability evaluation.