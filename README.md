# group-b
Nancy Zhang: huayizhang@brandeis.edu \
Chun Fung Wong: chunfungwong@brandeis.edu \
Ryan Jian: hnjian@brandeis.edu \
Abby Zhang: abbyzhw@brandeis.edu 

General Introduction:
This project is a simplified version of the Dominion Card game, using the ATG API 2.4.2 version. 
(https://www.javadoc.io/doc/io.github.brandeis-cosi-103a/atg-api/latest/edu/brandeis/cosi/atg/api/package-summary.html)

This documentation will briefly go through the essential classes and cover their functionalities.

# ATG Game Platform

This is a Spring Boot-based web application for simulating and managing card-based strategy games. It allows users to create, view, and delete active games between different types of AI players.

---

## 🚀 Features

- Create new games between AI players (BigMoney, RedEye, etc.)
- View real-time game states with modal-based UI
- Simulate game phases (Action, Buy, Money, Cleanup, etc.)
- Extendable game engine and player logic
- RESTful API support for external clients
- Web interface with game viewer modal

---

## 📁 Project Structure

```bash
src/main/java/edu/brandeis/cosi103a/groupb/
├── Decks              # Card deck handling (Draw, Discard, etc.)
├── Game               # Game engine and logic
├── Player             # Player implementations (AI, Network, Human)
├── Rating             # Tournament simulation and player ranking
├── Server
│   ├── controller     # REST API controllers
│   ├── handler        # Legacy HTTP server handlers
│   ├── model          # Request/response models
│   └── exception      # Exception handled
└── Application.java   # Spring Boot entrypoint
```

Note: everything in the Server + NetworkPlayer.java + reletive configurations in the pom.xml in the Player package is deliverables for milestone 3. Everything else is deliverables for milestone 2.

---

## 🛠️ Technologies Used

- Java 17
- Spring Boot 3+
- Maven
- RESTful API (Jackson for JSON serialization)
- JavaScript + HTML + CSS for frontend

---

## 🔧 How to Run Locally

1. Clone the repository:
```bash
git clone https://github.com/your-username/atg-game-platform.git
cd atg-game-platform
```

2. Start the Spring Boot server:
```bash
./mvnw spring-boot:run
```

> 🔌 By default, the server runs at: `http://localhost:8080/`

3. Visit the homepage:
```bash
http://localhost:8080/
```

You'll see the **ATG Game Platform** UI with options to create and manage games.

---

## 📄 API Endpoints

### Game APIs
- `GET /api/games` - Get all active games
- `POST /api/games` - Create a new game
- `DELETE /api/games/{id}` - Delete a game by ID
- `GET /api/games/{id}` - Get game metadata
- `GET /api/games/{id}/state` - Get current game state

### Player API
- `GET /api/players` - Get list of available player types

### Decision APIs
- `POST /decide` - Post player decision (used by NetworkPlayer)
- `POST /log-event` - Post event log from player

---

## 🧠 AI Players

- **BigMoneyPlayer**: Greedy player that prioritizes high-value cards
- **RedEyePlayer**: Strategic player that reacts to game state
- **FinalBossPlayer**: Advanced player that analyzes and responds to the game situation
- **NetworkPlayer**: Delegates decisions to a remote HTTP server
- **HumanPlayer**: Terminal-based interactive player (useful for debugging)

---

## ✨ UI Preview

The frontend is a simple HTML/CSS/JS page located in `homepage.html`. It loads dynamically using JavaScript and supports:

- Player type dropdowns
- Create game form
- Game viewer modal
- Refresh/delete actions

---

## 🤝 Team
This project was developed by Group B for COSI 103A Spring 2025.
Nancy Zhang: huayizhang@brandeis.edu \
Chun Fung Wong: chunfungwong@brandeis.edu \
Ryan Jian: hnjian@brandeis.edu \
Abby Zhang: abbyzhw@brandeis.edu 

---

## 📬 Contact
If you have any questions or want to contribute, feel free to reach out!

---

## 📜 License
Brandeis University, COSI 103A, Prof. Joseph Delfino.
COSI 103A ATG API 2.4.2 version. 
(https://www.javadoc.io/doc/io.github.brandeis-cosi-103a/atg-api/latest/edu/brandeis/cosi/atg/api/package-summary.html)

