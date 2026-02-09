# A Aldeia Adormece (Werewolf)

A console-based implementation of the classic social deduction game "Werewolf" (Mafia), written in Kotlin.

## How to Play

The game requires at least 4 players. Since it's a console game, players should take turns at the terminal ("hot-seat" style) to perform their secret night actions.

### Roles
- **Villager**: No special abilities. Tries to find the Werewolves during the day.
- **Werewolf**: Wakes up at night to eliminate a villager.
- **Seer**: Wakes up at night to inspect one player's role.
- **Medic**: Wakes up at night to protect one player from being eliminated.

### Game Phases
1. **Night**: Special roles (Wolf, Seer, Medic) wake up to perform actions.
2. **Day**: All players discuss and vote to eliminate a suspect.
3. **Win Condition**:
    - **Villagers Win**: All Werewolves are eliminated.
    - **Werewolves Win**: Werewolves equal or outnumber Villagers.

## How to Run

### Prerequisites
- JDK 17 or higher (or JDK 21 as configured in gradle).

### Running from Terminal
You can run the game directly using Gradle:

```bash
./gradlew run --console=plain
```

*Note: The `--console=plain` flag is recommended to ensure standard input/output handling works smoothly.*

### Building and Running Jar
Alternatively, you can build a jar file:

```bash
./gradlew build
java -jar build/libs/Werewolf-1.0-SNAPSHOT.jar
```
(Note: You may need to configure the jar task in `build.gradle.kts` to include dependencies/Main-Class for the jar to be executable directly, otherwise use `run` task).

## Project Structure
- `src/main/kotlin/Game.kt`: Core game loop and logic.
- `src/main/kotlin/roles/`: Role definitions.
- `src/main/kotlin/Player.kt`: Player model.
