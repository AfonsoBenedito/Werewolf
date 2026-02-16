# Werewolf Web Interface

![Web Banner](file:///Users/afcoelho/.gemini/antigravity/brain/a8e834ba-93d9-4552-9f6e-b1fd647cc9f2/landing_page_1771244850991.png)

This is the modern web frontend for the Werewolf game. It features a premium, dark, glassmorphic aesthetic inspired by high-end gaming interfaces.

## ✨ Features

- **Online Mode**: Multiplayer gameplay using WebSockets. Create or join rooms using a Game ID.
- **Offline Mode**: A narrator-assisted mode for physical gatherings.
- **Premium Design**: Custom glassmorphic components, smooth transitions, and responsive layout.
- **Real-time Updates**: Instant game state synchronization.

## 📸 Interface Preview

````carousel
![Online Lobby](file:///Users/afcoelho/.gemini/antigravity/brain/a8e834ba-93d9-4552-9f6e-b1fd647cc9f2/lobby_page_1771244879318.png)
Online Lobby
<!-- slide -->
![Waiting Room](file:///Users/afcoelho/.gemini/antigravity/brain/a8e834ba-93d9-4552-9f6e-b1fd647cc9f2/waiting_room_page_1771244925001.png)
Waiting Room
````

## 🚀 Getting Started

### Prerequisites
- Node.js (v20+)
- npm

### Development Mode
To run the frontend independently:
```bash
cd web
npm install
npm run dev
```

### Full Stack (with Docker)
Usually, it's easier to run everything from the project root:
```bash
make run-web
```

## 📂 Project Structure
- `src/components`: Reusable UI components.
- `src/pages`: Main application pages (Landing, Lobby, Game).
- `src/hooks`: Custom React hooks for game logic and API interaction.
- `src/api`: API client definitions.
- `src/styles`: CSS design system and page-specific styles.
