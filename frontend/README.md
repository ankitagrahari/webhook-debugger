# HookSpy Frontend

React 18 + Vite + TailwindCSS frontend for HookSpy.

## Prerequisites

- Node 18+
- ui-service running on `:8082`

## Setup

```bash
cd frontend
npm install
npm run dev
```

Open http://localhost:5173 — Vite proxies `/api/*` to `http://localhost:8082`.

## File structure

```
src/
├── main.jsx                 # Entry point, router, QueryClient, AuthProvider
├── index.css                # Tailwind directives + global styles + component classes
├── context/
│   └── AuthContext.jsx      # useAuth() — user state, login, logout, register
├── components/
│   ├── ProtectedRoute.jsx   # Redirects to /login if not authenticated
│   ├── Sidebar.jsx          # Endpoint list, create, delete, logout
│   ├── UrlBar.jsx           # Capture URL + copy + live indicator + tier badge
│   ├── RequestList.jsx      # Webhook grid + SSE live updates
│   └── RequestDetail.jsx    # Body/Headers/Query tabs + Replay + cURL
├── pages/
│   ├── LoginPage.jsx
│   ├── RegisterPage.jsx     # Tier selector
│   ├── HomePage.jsx         # Redirects to first endpoint or empty state
│   ├── DashboardPage.jsx    # 3-panel layout
│   └── UpgradePage.jsx      # CashFree checkout
└── lib/
    ├── api.js               # Axios instance (session cookies, 401 redirect)
    ├── utils.js             # cn() helper
    └── timeUtils.js         # formatDistanceToNow, formatTimestamp
```

## Production build

```bash
npm run build
# Outputs to ../ui-service/src/main/resources/static/
# Spring Boot serves React at / and API at /api/*
```

## Notes

- Auth uses HTTP sessions (Spring Security). Cookies sent via `withCredentials: true`.
- SSE (`EventSource`) connects to `/api/endpoints/{slug}/requests/stream`.
  Requires `withCredentials: true` — handled in `RequestList.jsx`.
- Tailwind custom colours/animations are in `tailwind.config.js`.
  Method badge classes (`badge-GET`, `badge-POST` etc.) are in `index.css`.
