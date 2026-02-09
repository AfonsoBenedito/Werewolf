# Werewolf Web Version

This project adds a web interface to the Werewolf game, featuring both Offline (Narrator-led) and Online (Multiplayer) modes.

## Tech Stack
- **Backend**: Kotlin + Spring Boot
- **Frontend**: React + Vite
- **Containerization**: Docker + Docker Compose

## modules
- `src/main/kotlin`: Backend logic and API.
- `web/`: Frontend React application.

## Running Locally
1. Ensure Docker is installed.
2. Run `docker-compose up --build`.
3. Access the frontend at `http://localhost:3000`.
4. API is available at `http://localhost:8080`.
