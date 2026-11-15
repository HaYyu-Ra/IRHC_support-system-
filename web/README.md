# IRHC Web Application

**Project:** Integrated Reproductive Health Awareness and Consultation System  
**Author:** Hayyu Ragea Chewaka

A responsive browser application for reproductive-health awareness, appointment requests and provider communication. The web client is in `web/`; the companion Android/Kotlin project remains in `app/`.

## Implemented web modules

- Responsive dashboard and account navigation
- Supabase email/password registration and sign-in (when configured)
- Appointment request form, appointment history and status display
- Consultation thread/message persistence for authenticated users
- Provider directory backed by Supabase when configured
- Searchable reproductive-health learning library
- Notifications view, staff-only navigation, aggregate reports and CSV export
- Local demo mode with browser storage when no Supabase credentials are configured

The learning resources and provider examples included in demo mode are illustrative. They are not approved clinical guidance or verified provider listings.

## Run locally

From the repository root:

```bash
cd web
python -m http.server 8080
```

Open `http://localhost:8080`. Without backend configuration the app runs in demo mode and saves demo appointment/message data in that browser.

## Connect Supabase

1. Create a Supabase project.
2. Open the Supabase SQL Editor and run `web/supabase/schema.sql`.
3. In Supabase Authentication, configure email/password sign-in and the appropriate email confirmation policy.
4. Set the project URL and **publishable/anon key** in `web/config.js`:

```js
window.IRHC_CONFIG = {
  supabaseUrl: "https://YOUR_PROJECT_REF.supabase.co",
  supabaseAnonKey: "YOUR_SUPABASE_PUBLISHABLE_OR_ANON_KEY"
};
```

5. Add provider profiles and provider rows using a trusted administrative process. Assign staff roles only through a trusted server-side workflow.
6. Test registration, email confirmation, sign-in, appointment requests, consultation messages and row-level security using separate community and staff test accounts.

**Never put a Supabase `service_role` key, database password or other secret in `config.js` or any browser bundle.** The browser key is public by design; security depends on correct RLS policies and server-side controls.

## Deploy on Render

Create a Render Static Site connected to this repository:

- Branch: `main`
- Root directory: `web`
- Build command: `echo "IRHC static web app"`
- Publish directory: `.`

After configuring Supabase, ensure `config.js` contains only the public project URL and publishable/anon key. Enable HTTPS and configure the correct authentication redirect URLs in Supabase.

## Production-readiness checklist

Before any real patient information is entered, the project requires:

- A qualified clinical review and approval of all educational content and care workflows
- Privacy/legal review, institutional authorization, informed-consent and retention policies
- Provider credential verification and operational ownership
- Security review of every RLS policy, role transition and privileged operation
- Testing of concurrent appointment booking, availability, cancellations and staff workflows
- Secure consultation messaging, abuse controls, audit logging and notification delivery
- Backup/restore testing, monitoring, incident response and accessibility testing

The current web app is a development release, not a certified clinical system or emergency service. Do not enter real patient data until the above reviews and tests are complete.
