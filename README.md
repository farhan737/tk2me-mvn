# TK2ME Messaging Application

A complete messaging application with a Spring Boot backend and Flutter frontend. This application allows users to register, login, send friend requests, accept/deny friend requests, and chat with friends.

## Backend (Spring Boot)

The backend is built using Spring Boot and provides RESTful APIs for the Flutter frontend to communicate with.

### Features

- User registration and authentication with JWT
- Friend request management (send, accept, reject)
- Private messaging between friends
- MySQL database for data persistence

### Prerequisites

- Java 17 or higher
- MySQL database
- Maven

### Configuration

The backend is configured to connect to a MySQL database. You can modify the database connection settings in `src/main/resources/application.properties`.

```properties
spring.datasource.url=jdbc:mysql://localhost:3306/tk2me?createDatabaseIfNotExist=true
spring.datasource.username=root
spring.datasource.password=
```

### Running the Backend

1. Make sure MySQL is running
2. Navigate to the project directory
3. Run the application using Maven:

```bash
mvn spring-boot:run
```

The backend will start on `http://localhost:8080`.

### API Endpoints

#### Authentication
- POST `/api/auth/signup` - Register a new user
- POST `/api/auth/signin` - Login and get JWT token

#### Friends
- GET `/api/friends/list` - Get list of friends
- GET `/api/friends/requests/pending` - Get pending friend requests
- POST `/api/friends/request/{username}` - Send a friend request
- PUT `/api/friends/request/{requestId}/accept` - Accept a friend request
- PUT `/api/friends/request/{requestId}/reject` - Reject a friend request

#### Messages
- GET `/api/messages/conversation/{username}` - Get conversation with a friend
- POST `/api/messages/send/{username}` - Send a message to a friend
- GET `/api/messages/unread` - Get unread messages

## Frontend (Flutter)

The Flutter frontend provides a mobile application interface for the messaging system.

### Features

- User registration and login
- Friend management (add friends, accept/reject requests)
- Real-time messaging with friends
- Beautiful and intuitive UI

### Prerequisites

- Flutter SDK
- Android Studio or VS Code with Flutter extensions

### Running the Frontend

1. Navigate to the Flutter project directory
2. Get dependencies:

```bash
flutter pub get
```

3. Run the application:

```bash
flutter run
```

### Configuration

The Flutter app is configured to connect to the backend at `http://10.0.2.2:8080` for Android emulators. If you're using a physical device or iOS simulator, you'll need to update the base URL in `lib/services/api_service.dart`.

## Usage

1. Start the Spring Boot backend
2. Start the Flutter application
3. Register a new account or login with existing credentials
4. Add friends by username
5. Accept or reject incoming friend requests
6. Chat with your friends

## Security

- All API endpoints (except authentication) are secured with JWT
- Passwords are encrypted using BCrypt
- CORS is enabled for cross-origin requests

## Database Schema

The application uses the following database tables:
- `users` - Stores user information
- `friend_requests` - Tracks friend requests between users
- `messages` - Stores chat messages between users
# tk2me-mvn
