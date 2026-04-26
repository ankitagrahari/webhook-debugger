| Status |                        Services                        |                                Notes                                 | 
|:------:|:------------------------------------------------------:|:--------------------------------------------------------------------:|
|   ✅    |                    capture-service                     |                        WebFlux + Kafka Binder                        |
|   ✅    |                   processor-service                    |                     Kafka consumer + PostgreSQL                      |
|   ✅    |                       ui-service                       |              Dashboard, replay, cURL, auth (in-memory)               | 
|   ✅    |                       UI polish                        |                             Light theme                              |
|   ✅    |                    Tier enforcement                    |                   Enforce Free, Pro and Max tiers                    |
|   ✅    |                          Auth                          |                           DB-backed users                            |
|   ✅    |                      Distribution                      | Docker Compose full stack, landing page, domain setup, Show HN prep. |
|   ✅    |                      Landing page                      |              Navigating to other pages need domain URL               |
|   ✅    |                    Dark mode toggle                    |                             NOT WORKING                              |
|   ✅    |                 Razorpay upgrade flow                  |                          No API keys added                           |
|   ✅    |                   Slack alerts (Pro)                   |                          No API keys added                           |
|   ⬜    | Razorpay signature verification (production hardening) |                                                                      |
|   ⬜    |            Email alerts via Resend/SendGrid            |                                                                      |
|   ⬜    |           Persist dark mode preference to DB           |                                                                      |
|   ⬜    |                     Show HN launch                     |                                                                      |

---
### Tier Enforcement Rules

|                    Rule                     |              Where enforced              | 
|:-------------------------------------------:|:----------------------------------------:|
|           Max 1 endpoint on Free            |     EndpointService.createEndpoint()     |
|           Max 100 req/day on Free           | capture-service — reject at capture time | 
|           Replay blocked on Free            |   DashboardView — hide/disable button    |
| History limited to 1 day Free / 30 days Pro |      WebhookRequestRepository query      |

---
