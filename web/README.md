# IRHC Web Platform
Responsive web demonstration based on the original HiLCoE Integrated Reproductive Health Awareness and Consultation System paper. The repository retains the existing Android Studio/Kotlin project and adds this web client in /web.

## Features
- Responsive community dashboard and health education library
- Appointment request UI with browser local-storage persistence
- Consultation messaging demonstration
- Notifications, provider directory, sample reports and administration screens
- PostgreSQL/Supabase schema baseline in supabase/schema.sql

## Run locally
Open index.html in a browser or serve this directory with a static server: python -m http.server 8080

## Backend
Create a Supabase project, review/apply supabase/schema.sql, configure managed authentication, and implement authenticated CRUD and server-side privileged workflows. Enable and test RLS using multiple accounts. Never expose the service_role key in browser code.

## Deploy
Render static site: branch main, build command 'echo static IRHC', publish directory 'web'.

## Safety
This is a demonstrator, not a clinical or emergency service. Sample content, providers, messages and metrics are illustrative. Do not enter real patient data. Production use requires clinical, privacy/legal and security review, institutional approval, backup/restore validation and operational sign-off.