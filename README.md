# IRHC — Integrated Reproductive Health Awareness & Consultation System

Modernization of the original HiLCoE project into a responsive web platform while retaining the existing Android Studio/Kotlin companion application.

- `app/` — Android application source
- `web/` — responsive web demonstration
- `web/supabase/schema.sql` — PostgreSQL data model and row-level security baseline
- `IRHC_Modernized_Project_Specification.md` — updated project specification

See `web/README.md` for setup and deployment guidance.

**Important:** The web release is a demonstrator. Sample providers, messages and reports are illustrative; appointment requests are browser-local until a backend is configured. Do not use with real patient data. Production use requires clinical, privacy/legal and security approval.