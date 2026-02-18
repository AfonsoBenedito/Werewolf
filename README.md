<p align="center">
  <img src="web/public/favicon.png" alt="Werewolf Logo" width="120" />
</p>

<h1 align="center">Werewolf — A Aldeia Adormece</h1>

<p align="center">
  <em>A modern, multi-platform implementation of the classic social deduction game.</em>
</p>

<p align="center">
  <img src="https://img.shields.io/badge/Kotlin-1.9-7F52FF?logo=kotlin&logoColor=white" alt="Kotlin" />
  <img src="https://img.shields.io/badge/Spring_Boot-3.2-6DB33F?logo=springboot&logoColor=white" alt="Spring Boot" />
  <img src="https://img.shields.io/badge/React-19-61DAFB?logo=react&logoColor=black" alt="React" />
  <img src="https://img.shields.io/badge/TypeScript-5.9-3178C6?logo=typescript&logoColor=white" alt="TypeScript" />
  <img src="https://img.shields.io/badge/Docker-Compose-2496ED?logo=docker&logoColor=white" alt="Docker" />
</p>

---

## Overview

Werewolf is a full-stack social deduction game where villagers must identify and eliminate hidden werewolves before being outnumbered. This project ships two ways to play:

| Mode | Description | How to Launch |
|------|-------------|---------------|
| **Web App** | Glassmorphic React UI with real-time multiplayer (WebSockets) and an offline narrator mode | `make run-web` |
| **Console** | Hot-seat CLI version for quick terminal sessions | `make run-console` |

Both modes share the same **core game engine** written in Kotlin — roles, phases, win conditions, and night actions are defined once and reused everywhere.

---

## Game Roles

| Role | Team | Night Ability |
|------|------|---------------|
| **Werewolf** | Werewolves | Choose a villager to eliminate. Multiple wolves must reach consensus. |
| **Seer** | Villagers | Inspect one player to learn if they are a regular Villager or not. |
| **Medic** | Villagers | Protect one player from the werewolf attack. |
| **Villager** | Villagers | No special ability. Use your wits during the day. |

## Game Flow

```
Night Phase                    Day Phase
   |                              |
   v                              v
Werewolves  ---->  Seer  ---->  Medic  ---->  Morning Report  ---->  Discussion  ---->  Voting  ---->  Results
  (kill)         (peek)        (heal)        (who died?)           (debate!)        (eliminate)     (check win)
                                                                                                       |
                                                                                               Next Night / Game Over
```

**Win conditions:**
- **Villagers win** when all werewolves are eliminated
- **Werewolves win** when they equal or outnumber the remaining villagers

---

## Tech Stack

### Backend
| Technology | Purpose |
|---|---|
| **Kotlin 1.9** | Primary language |
| **Spring Boot 3.2** | Web framework & dependency injection |
| **Spring WebSocket** | Real-time game state sync via STOMP |
| **Redis** | Persistent game state storage |
| **JUnit 5** | Unit testing |

### Frontend
| Technology | Purpose |
|---|---|
| **React 19** | UI framework |
| **TypeScript 5.9** | Type safety |
| **Vite 7** | Build tool & dev server |
| **STOMP.js / SockJS** | WebSocket client |
| **Lucide React** | Icon library |

### Infrastructure
| Technology | Purpose |
|---|---|
| **Docker Compose** | Local multi-service orchestration |
| **Nginx** | Frontend reverse proxy with API/WebSocket routing |
| **Google Cloud Run** | Production deployment |
| **GitHub Actions** | CI pipeline & automated deploys |

---

## Quick Start

### Prerequisites

- **Docker** & **Docker Compose** (for the web version)
- **JDK 17** (for the console version or running Gradle directly)
- **Node.js 20+** (for frontend development)
- **Make** (all commands use the root Makefile)

### Web Version (Full Stack)

Launches the backend, frontend, and Redis in Docker:

```bash
make run-web
```

> Open [http://localhost:3000](http://localhost:3000) in your browser.

### Console Version

Plays directly in your terminal using a hot-seat mechanic:

```bash
make run-console
```

---

## Project Structure

```
Werewolf/
├── src/main/kotlin/.../werewolf/
│   ├── core/                  # Shared game engine
│   │   ├── Game.kt            # Game state, phases, actions
│   │   ├── model/             # Player, Roles (Wolf, Seer, Medic, Villager)
│   │   └── model/strategy/    # Wolf consensus strategy
│   ├── console/               # CLI version (see src/README.md)
│   │   ├── runner/            # Game loop
│   │   ├── controller/        # Turn handlers per role
│   │   └── interaction/       # I/O abstraction
│   └── web/                   # Spring Boot web version (see web/README.md)
│       ├── api/               # REST + WebSocket controllers
│       ├── service/           # Game orchestration
│       ├── repository/        # Redis persistence
│       └── config/            # WebSocket & SPA config
├── web/                       # React frontend
│   ├── src/pages/             # Landing, Lobby, Online/Offline Game
│   ├── src/components/        # Reusable UI components
│   ├── src/hooks/             # Game logic hooks
│   └── public/                # Static assets (favicon, background)
├── Makefile                   # All project commands
├── docker/                    # Docker configuration
│   ├── docker-compose.yml     # Local Docker orchestration
│   ├── Dockerfile             # Production single-container build
│   ├── Dockerfile.backend     # Dev backend container
│   ├── Dockerfile.frontend    # Dev frontend container (Nginx)
│   ├── nginx.conf             # Nginx reverse proxy config
│   └── start.sh               # Production container entrypoint
└── .github/workflows/         # CI & Cloud Run deployment
```

---

## Makefile Commands

All project operations are available through the root `Makefile`:

### Running

| Command | Description |
|---|---|
| `make run-web` | Start the full web stack (backend + frontend + Redis) via Docker Compose |
| `make stop-web` | Stop all Docker Compose services |
| `make run-console` | Launch the terminal-based game |
| `make run-frontend` | Start only the Vite dev server (`localhost:5173`) |
| `make run-backend` | Start only the backend + Redis via Docker Compose |
| `make stop-backend` | Stop the backend and Redis containers |

### Testing & Code Quality

| Command | Description |
|---|---|
| `make unit-tests-kotlin` | Run Kotlin/JUnit backend tests |
| `make unit-tests-web` | Run frontend tests |
| `make code-check-kotlin` | Run Kotlin static analysis |
| `make code-check-web` | Run frontend linting |
| `make code-fix-kotlin` | Auto-fix Kotlin code style |
| `make code-fix-web` | Auto-fix frontend code style |
| `make run-all-tests` | Run all tests and code checks |

---

## CI/CD

The project uses **GitHub Actions** with two workflows:

- **CI** (`ci.yml`) — Runs on pull requests to `main`. Executes Kotlin tests, web tests, and code quality checks.
- **Deploy** (`deploy.yml`) — Runs on push to `main`. Builds a production Docker image and deploys to **Google Cloud Run** (europe-southwest1).

---

## Docker Architecture

**Development** (`docker/docker-compose.yml`) runs four services:

```
┌─────────────┐     ┌─────────────┐     ┌─────────────┐     ┌──────────────────┐
│  Frontend   │────>│   Backend   │────>│    Redis    │     │ Redis Commander  │
│  :3000      │     │  :8080      │     │  :6379      │     │  :8081           │
│  (Nginx)    │     │ (Spring)    │     │  (Alpine)   │     │  (Web UI)        │
└─────────────┘     └─────────────┘     └─────────────┘     └──────────────────┘
```

**Production** (`docker/Dockerfile`) builds a single container with frontend assets bundled into the Spring Boot JAR, plus an embedded Redis instance.

---

<p align="center">
  Created by <strong>Afonso Benedito</strong>
</p>
