# Werewolf Console Edition

![Console Banner](file:///Users/afcoelho/.gemini/antigravity/brain/a8e834ba-93d9-4552-9f6e-b1fd647cc9f2/console_mockup_1771244991052.png)

A classic terminal-based implementation of Werewolf. Perfect for quick sessions or environments without a web browser.

## 🎭 How it Works

The console version uses a **hot-seat** multiplayer mechanic. One terminal is used, and players take turns coming to the keyboard to perform their secret actions during the Night phase.

### 📜 Gameplay Flow
1. **Setup**: Choose the number of players (minimum 4) and enter their names.
2. **Night Phase**: The terminal prompts specific roles (Werewolf, Seer, Medic) to wake up. Other players should look away!
3. **Day Phase**: The terminal displays who was eliminated, and all players discuss who to vote out.

## 🚀 How to Run

From the main project directory, run:

```bash
make run-console
```

Alternatively, using Gradle directly:
```bash
./gradlew -q runConsole --console=plain
```

## 📸 Screenshot

![Gameplay](file:///Users/afcoelho/.gemini/antigravity/brain/a8e834ba-93d9-4552-9f6e-b1fd647cc9f2/console_mockup_1771244991052.png)

## 📂 Code Location
The console logic is mainly contained in:
- `src/main/kotlin/com/afonsobenedito/werewolf/console/`
- Core game logic is shared in `src/main/kotlin/com/afonsobenedito/werewolf/core/`
