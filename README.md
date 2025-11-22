# NTU Add-Drop Automator v3

This project is an automation tool designed for Nanyang Technological University (Singapore), benefiting over 30,000 students. It utilizes a Redis cache database database to store all session swap information (with credentials encrypted and stored for no longer than 2 hours), a Spring Boot application to create a RESTful API for the backend, and a ReactJS frontend for intuitive user interaction.

## Visit The Site

Feel free to check out the [project here!](https://ntu-add-drop-automator.vercel.app/)

<img width="1301" alt="NTU Add-Drop Automator Home Page" src="https://github.com/josshhz11/ntu-add-drop-automator-v3/assets/NTU-Add-Drop-Automator-Home-Page.png">

## Features

- **Redis Database:** Stores detailed information on each user's NTU portal credentials (encrypted) and swap sessions (including unique session ID, old and new indexes, and swap status).
- **Spring Boot Backend:** Provides a robust RESTful API to manage asynchronous, periodic swap execution efficiently. The backend is packaged as a Dockerfile and hosted on Render/Heroku.
- **ReactJS Frontend:** A user-friendly interface for keying in user credentials, swap information, and viewing swap statuses. The frontend is hosted on Vercel.

## Prerequisites

Before running this project locally, ensure you have the following installed:

- Java Development Kit (JDK) 8 or higher
- Node.js and npm (Node Package Manager)
- Redis database (or Docker for Redis container)
- PostgreSQL database
- IDE (IntelliJ IDEA, Eclipse, VS Code, etc.)

## Installation

### Backend Setup

1. Clone this repository.
2. Open the `backend` directory in your preferred IDE.
3. Configure the `application.yml` file in the `src/main/resources` directory with your Redis database credentials.
4. Install Chrome and ChromeDriver for Selenium automation.
5. Run the Spring Boot application using `./mvnw spring-boot:run`.

### Frontend Setup

1. Navigate to the `frontend` directory in your terminal.
2. Run `npm install` to install the necessary dependencies.
3. Update the `src/config.js` file with the appropriate backend API URL.
4. Run `npm start` to start the ReactJS application.

### Local Development with Docker

1. Start Redis using Docker:
   ```bash
   docker run --name redis-local -p 6379:6379 -d redis:latest
   ```
2. Follow the Backend and Frontend setup steps above.

## Usage

- Access the frontend application via `http://localhost:3000`.
- Use the provided API endpoints to perform course swap operations:
  - `/api/login` - POST user authentication with NTU credentials.
  - `/api/submit-swap` - POST to start automated course swap process.
  - `/api/swap-status/{sessionId}` - GET real-time swap progress and status.
  - `/api/stop-swap/{sessionId}` - POST to stop ongoing swap process.
  - `/api/health` - GET application health check.

## API Documentation

### Authentication
- **POST** `/api/login`
  - Body: `{"username": "ntu_username", "password": "ntu_password", "numModules": 2}`
  - Response: `{"success": true, "session_id": "unique_session_id", "num_modules": 2}`

### Course Swapping  
- **POST** `/api/submit-swap`
  - Body: `{"num_modules": 2, "modules": [{"old_index": "12345", "new_indexes": "12346,12347"}]}`
  - Response: `{"success": true, "session_id": "session_id", "message": "Swap process started"}`

### Status Monitoring
- **GET** `/api/swap-status/{sessionId}`
  - Response: `{"status": "Processing", "message": "Attempting swap...", "details": [...]}`

## Contributing

Contributions are welcome! If you'd like to enhance this project or report issues, please submit a pull request or open an issue.

## License

This project is for educational purposes only. Please use responsibly and in accordance with NTU's terms of service.
