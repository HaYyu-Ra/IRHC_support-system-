# IRHC — Integrated Reproductive Health Awareness and Consultation System

**Author: Hayyu Ragea Chewaka**  
**Project modernization: November 2026**

IRHC modernizes the original reproductive-health awareness and consultation project into a responsive web platform while retaining the Android Studio/Kotlin companion application.

## Repository structure
- `web/` — responsive web demonstration
- `web/supabase/schema.sql` — PostgreSQL data model and row-level security baseline
- `app/` — Android application source
- `docs/SOFTWARE_APPLICATION_DESIGN.md` — full-stack architecture, workflows, security and delivery plan
- `docs/PROTOTYPE_AND_MODULES.md` — prototype screens, functional modules and acceptance checklist
- `docs/DATABASE_DESIGN.md` — logical data model, relationships and access-control requirements
- `IRHC_Modernized_Project_Specification.md` — modernized project specification
- `web/README.md` — web setup and deployment guidance

## Technology direction
- Web: HTML, CSS and JavaScript
- Android: Kotlin / Android Studio
- Backend and identity: Supabase Auth and PostgreSQL
- Hosting: Render
- Source control: GitHub

## Demo
https://irhc-platform.onrender.com

## Important safety notice
The web release is a demonstrator. Sample providers, messages and reports are illustrative; appointment requests are browser-local until a backend is configured. Do not use with real patient data. The SQL schema is a starting point, not proof that production RLS has been tested. Production use requires backend integration, authorization and security testing, qualified clinical content review, provider verification, privacy/legal approval, institutional ownership, backup/restore testing and operational sign-off. IRHC is not an emergency service, diagnostic tool or prescribing system.
