# IRHC Software Application Design
**Project:** Integrated Reproductive Health Awareness and Consultation System  
**Author:** Hayyu Ragea Chewaka  
**Version:** 1.0 — November 2026

## 1. Purpose and scope
IRHC is a digital information and service-coordination platform for reproductive-health education and access to qualified consultation. It supports educational content, community accounts, provider discovery, appointment requests, secure consultation workflows and operational reporting. It is not an emergency response service, diagnostic system, prescribing tool or substitute for professional care.

## 2. Stakeholders and actors
- **Guest:** reads public, approved educational resources and provider information.
- **Community member:** manages own profile, requests appointments and accesses own consultation conversations.
- **Healthcare professional:** maintains professional profile and availability, handles assigned appointments and consultations.
- **Content reviewer/manager:** reviews and publishes educational resources and views permitted aggregate operational reports.
- **System administrator:** provisions staff roles, configures the platform and reviews security/audit events. Administrative privileges are never self-assigned through public registration.

## 3. Logical architecture
1. **Presentation layer:** responsive browser application in `web/`; Android companion application in `app/`.
2. **Identity and access layer:** Supabase Auth or an equivalent managed identity provider. Privileged accounts require MFA.
3. **Application/data layer:** PostgreSQL managed through Supabase; database constraints and row-level security (RLS) enforce ownership and role boundaries.
4. **Operations layer:** Render-hosted web assets, HTTPS, environment configuration, monitoring, backups and restore procedures.
5. **External integrations (future/optional):** SMS/email notifications and approved health-system integrations. These must be configured with consent, vendor review and data-processing agreements.

## 4. Core application workflows
### Community member
Register/sign in → review consent and privacy notice → browse approved resources → find a provider → submit an appointment request → receive status updates → exchange messages in an authorized consultation thread → view own history.

### Healthcare professional
Sign in with verified staff account → manage profile/availability → review assigned appointment requests → accept, reschedule or decline with a reason → respond within assigned consultation threads → close the consultation according to institutional policy.

### Content reviewer
Create draft resource → submit for review → review accuracy, source, language and publication date → approve or reject → publish approved content → periodically review/withdraw outdated content.

### Administrator
Provision verified staff account → assign minimum necessary role → review audit/security events → manage configuration and retention policy → coordinate backup and incident-response procedures.

## 5. Security and privacy design
- Enforce TLS in transit and managed encryption at rest.
- Keep service-role keys and other secrets on trusted server-side infrastructure only; never place them in browser bundles or source control.
- Use RLS policies for every table containing personal, appointment or consultation data.
- Validate authorization on the server/database; hiding a button is not access control.
- Apply MFA to privileged roles, rate limiting, secure session handling and account recovery controls.
- Collect only data necessary for a defined purpose; obtain informed consent and publish retention/deletion rules.
- Keep audit events for privileged changes and sensitive workflow access without logging message bodies or unnecessary health details.
- Use synthetic data in development, demos and automated tests.
- Define breach reporting, backup encryption, restore testing and access-revocation procedures before launch.

## 6. Quality attributes
Responsive layout, keyboard navigation, readable contrast, clear error states, accessible labels, pagination, input validation, database constraints, structured logs, monitoring and documented recovery procedures.

## 7. Delivery phases
1. **Foundation:** confirm institutional owner, clinical governance, user research, threat model and requirements.
2. **Prototype:** responsive screens, sample content and synthetic records; demonstrate key user journeys.
3. **Backend integration:** managed authentication, PostgreSQL schema, RLS, server-side validation and notification provider.
4. **Verification:** automated tests, role/ownership tests, accessibility review, penetration testing and backup restore exercise.
5. **Controlled pilot:** use synthetic or specifically approved minimum-risk data under institutional oversight.
6. **Production approval:** written clinical, legal/privacy, security and operational sign-off; support and incident-response readiness.

## 8. Current implementation status
The repository contains a browser-based demonstration, Android project source and a Supabase SQL schema baseline. The web demo currently uses illustrative content and browser-local appointment behavior; it must not be treated as a fully integrated production service. Supabase credentials, Auth, database migrations, RLS verification, server-side workflows, notification delivery, Android integration tests and institutional approvals must be completed and tested before real users or real patient information are onboarded.
