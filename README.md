# Werewolf (A Aldeia Adormece)

![Project Banner](file:///Users/afcoelho/.gemini/antigravity/brain/a8e834ba-93d9-4552-9f6e-b1fd647cc9f2/landing_page_1771244850991.png)

A modern, multi-platform implementation of the classic social deduction game **Werewolf** (also known as Mafia). This project features a robust Kotlin/Spring Boot backend, a premium React web interface, and a classic "hot-seat" console version.

## 🌌 Project Overview

This repository contains a full-stack implementation of Werewolf, designed with a focus on premium aesthetics and smooth gameplay across different platforms.

### 🎮 Available Versions
- **Web App**: A beautiful, glassmorphic React interface for both online multiplayer and offline narrator-led games.
- **Console Version**: A classic CLI implementation for quick, hot-seat sessions at the terminal.

## 🛠️ Tech Stack
- **Backend**: Kotlin, Spring Boot, Spring WebFlux/Web, Redis (for game state).
- **Frontend**: React, TypeScript, Vite, CSS (Glassmorphism).
- **Infrastructure**: Docker, Docker Compose, Gradle.

## 🚀 How to Run

The easiest way to run any part of the project is through the root `Makefile`.

### 🌐 Web Version (Full Stack)
To launch the entire web ecosystem (Backend, Frontend, and Redis) using Docker:
```bash
make run-web
```
*Access the UI at `http://localhost:3000`*

### 💻 Console Version
To play the traditional terminal-based game:
```bash
make run-console
```

### 🧪 Running Tests
```bash
make run-all-tests
```

## 📸 Screenshots

### Web Interface
````carousel
![Landing Page](file:///Users/afcoelho/.gemini/antigravity/brain/a8e834ba-93d9-4552-9f6e-b1fd647cc9f2/landing_page_1771244850991.png)
Landing Page
<!-- slide -->
![Online Lobby](file:///Users/afcoelho/.gemini/antigravity/brain/a8e834ba-93d9-4552-9f6e-b1fd647cc9f2/lobby_page_1771244879318.png)
Online Lobby
<!-- slide -->
![Waiting Room](file:///Users/afcoelho/.gemini/antigravity/brain/a8e834ba-93d9-4552-9f6e-b1fd647cc9f2/waiting_room_page_1771244925001.png)
Waiting Room
````

### Console Version
![Console Mockup](file:///Users/afcoelho/.gemini/antigravity/brain/a8e834ba-93d9-4552-9f6e-b1fd647cc9f2/console_mockup_1771244991052.png)
Console Gameplay

---
*Created by Afonso Benedito*
