# IRHC Prototype and Functional Modules
**Author:** Hayyu Ragea Chewaka  
**Project:** Integrated Reproductive Health Awareness and Consultation System

## Prototype screens
1. **Public landing page:** project purpose, service explanation, emergency disclaimer and navigation.
2. **Education library:** categories, searchable approved resources, detail view, source and review date.
3. **Sign-in and registration:** community account entry, consent notice and account recovery.
4. **Community dashboard:** shortcuts to resources, appointments, notifications and consultation history.
5. **Provider directory:** verified provider profile, service area, specialty and availability.
6. **Appointment request:** provider selection, preferred date/time, reason category, consent and request status.
7. **Consultation inbox:** authorized conversation list, thread view and message composer.
8. **Provider workspace:** availability management, assigned requests and consultation queue.
9. **Content management:** draft, review, approval, publication and withdrawal states.
10. **Management dashboard:** aggregate counts and trends, with suppression of small groups where re-identification is possible.
11. **Administration:** staff provisioning, role assignment, configuration and audit review.

## Functional modules and boundaries
| Module | Main capabilities | Access |
|---|---|---|
| Identity and access | Registration, sign-in, recovery, session control, role enforcement | All authenticated users; staff provisioning restricted |
| Community profile | Manage own profile, consent and communication preferences | Member owns own record |
| Education and awareness | Browse, search and read reviewed resources | Public read; reviewer manages |
| Provider directory | View verified profiles and service availability | Public read; provider/staff maintain |
| Appointment management | Request, accept, reschedule, cancel and track appointments | Member owns request; assigned provider handles |
| Consultation messaging | Private thread and message exchange | Only thread participants and authorized oversight |
| Notifications | In-app status updates; optional SMS/email integration | Recipient only |
| Content governance | Drafting, clinical review, approval, publication and withdrawal | Reviewer/manager |
| Reporting | Aggregate service and usage statistics | Authorized managers |
| Administration and audit | Staff provisioning, least-privilege roles and audit review | Authorized administrators |

## Prototype acceptance checklist
- Public users can view only published educational resources.
- A member can see and update only their own profile.
- A member cannot read another member's appointments or consultation messages.
- A provider can access only appointments and threads assigned to them.
- Only authorized reviewers can publish or withdraw content.
- Reports expose aggregate information and avoid identifying individuals.
- Privileged roles cannot be selected during public self-registration.
- Every protected workflow has a server/database authorization check.
- Layout works on mobile, tablet and desktop and supports keyboard use.
- Empty, loading, validation and failure states are clear.
- All demo records are synthetic and clearly identified as examples.

## Prototype limitations
The current web release is a demonstrator. Browser-local appointment state is not shared with providers or persisted in a central database. Demo messages, providers and reports are illustrative. A screen or menu is not evidence that its corresponding secure backend workflow is implemented. Real consultations, patient records, diagnosis, prescribing and emergency triage are out of scope.
