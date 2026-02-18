<h1 align="center">Werewolf — Console Edition</h1>

<p align="center">
  <em>A terminal-based Werewolf game using hot-seat multiplayer.</em>
</p>

<p align="center">
  <img src="https://img.shields.io/badge/Kotlin-1.9-7F52FF?logo=kotlin&logoColor=white" alt="Kotlin" />
  <img src="https://img.shields.io/badge/JDK-17-ED8B00?logo=openjdk&logoColor=white" alt="JDK 17" />
</p>

---

## How It Works

The console version uses a **hot-seat** mechanic — all players share one terminal and take turns at the keyboard. During the Night phase, each role is prompted privately while other players look away. The screen is cleared between turns to keep information secret.

```
┌─────────────────────────────────────────┐
│           TERMINAL (shared)             │
│                                         │
│  Night: "Werewolf Alice, wake up!"      │
│  > Select a player to eliminate:        │
│    1. Bob                               │
│    2. Charlie                           │
│    0. Skip Vote                         │
│  Select number: _                       │
│                                         │
│  [Press Enter → screen clears]          │
│                                         │
│  Night: "Seer Bob, wake up!"            │
│  > Choose a player to inspect:          │
│    ...                                  │
└─────────────────────────────────────────┘
```

---

## Quick Start

### Prerequisites

- **JDK 17**
- **Gradle** (or use the included `gradlew` wrapper)

### Run

From the **project root**:

```bash
make run-console
```

Or using Gradle directly:

```bash
./gradlew -q runConsole --console=plain
```

---

## Gameplay Flow

### 1. Setup

The game prompts for the number of players (minimum 4) and each player's name. Roles are assigned randomly:

| Player Count | Werewolves | Seer | Medic | Villagers |
|---|---|---|---|---|
| 4 | 1 | 1 | 1 | 1 |
| 5 | 1 | 1 | 1 | 2 |
| 6+ | 2 | 1 | 1 | remaining |

### 2. Night Phase

Each role wakes up in order and performs their action:

| Turn Order | Role | Action |
|---|---|---|
| 1 | **Werewolves** | Choose a villager to kill. If there are 2 wolves, they must reach consensus. |
| 2 | **Seer** | Inspect one player — learn if they are a regular Villager or not. |
| 3 | **Medic** | Protect one player from the werewolf attack. |

If a role has been eliminated, their turn is silently skipped (with a brief delay to avoid revealing information).

### 3. Morning Report

The terminal announces who died overnight (or that it was a peaceful night if the Medic saved the target).

### 4. Day Phase

- **Discussion** — Players debate who they suspect.
- **Voting** — Each player votes to eliminate someone (or skips). Majority rules; ties result in no elimination.

### 5. Win Condition Check

After each night and each day vote, the game checks:
- **Villagers win** if all werewolves are eliminated
- **Werewolves win** if they equal or outnumber the villagers

---

## Architecture

The console version is structured into three layers:

```
ConsoleApplication.kt          # Entry point
       │
       v
ConsoleGameRunner              # Game loop: night → day → win check
       │
       ├── WolfTurnHandler     # Wolf consensus & kill selection
       ├── SeerTurnHandler     # Player inspection
       └── MedicTurnHandler    # Player protection
       │
       v
GameInteraction (interface)    # I/O abstraction
       │
       v
ConsoleGameInteraction         # Real terminal I/O (stdin/stdout)
```

### Code Structure

```
src/main/kotlin/.../werewolf/
├── console/
│   ├── ConsoleApplication.kt          # main() entry point
│   ├── runner/
│   │   └── ConsoleGameRunner.kt       # Game loop orchestration
│   ├── controller/
│   │   ├── TurnHandler.kt             # Abstract base (wake/sleep/silence)
│   │   ├── WolfTurnHandler.kt         # Wolf consensus logic
│   │   ├── SeerTurnHandler.kt         # Seer inspection logic
│   │   └── MedicTurnHandler.kt        # Medic protection logic
│   └── interaction/
│       ├── GameInteraction.kt          # I/O interface
│       └── ConsoleGameInteraction.kt   # Terminal implementation
└── core/                               # Shared game engine (Game.kt, roles, etc.)
```

### Testability

The `GameInteraction` interface allows the entire game loop to be tested without real terminal I/O. Tests use a `FakeGameInteraction` that queues scripted player selections and captures announcements for assertions.

```kotlin
val interaction = FakeGameInteraction(players)
interaction.queuePlayerSelection(victim)   // Script the wolf's choice
interaction.queuePlayerSelection(wolf)     // Script the seer's choice

runner.run()

assertTrue(interaction.announcements.any { it.contains("WEREWOLVES WIN") })
```

---

## Test Coverage

The console tests cover:

| Area | Tests |
|---|---|
| **WolfTurnHandler** | Single/multi wolf targeting, consensus disagreement & retry, abstention, no active wolves |
| **SeerTurnHandler** | Inspect wolf/villager/medic, skip, no active seer, return value |
| **MedicTurnHandler** | Heal dead player, skip, no active medic, return value |
| **ConsoleGameRunner** | Werewolf win (night kill), villager win (vote), tie votes, wolf abstention, multi-round games, morning/voting announcements |

Run tests:

```bash
make unit-tests-kotlin
```
