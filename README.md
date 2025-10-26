# WhatsApp Chat Clone - Real-time WebSocket Demo

A real-time chat application built with Spring Boot and WebSockets, featuring one-to-one private messaging with H2 database for message persistence.

## 🚀 Features

- **Real-time messaging** using WebSockets (STOMP protocol)
- **One-to-one private chat** between users
- **User authentication** (simple username-based login)
- **Online/offline status** tracking
- **Message history** persistence with H2 database
- **Responsive UI** with modern chat interface
- **Auto-switch conversations** when receiving messages

## 🛠️ Tech Stack

- **Backend**: Spring Boot 3.x, Java 21
- **Database**: H2 (in-memory)
- **WebSocket**: STOMP over SockJS
- **Frontend**: HTML, CSS, JavaScript
- **Build Tool**: Maven
- **Containerization**: Docker

## 🚀 Quick Start

### Prerequisites
- Java 21 or higher
- Maven (or use Maven Wrapper included)

### Run the Application

**Option A: Using Maven Wrapper (Recommended)**
```bash
# Windows
.\mvnw.cmd spring-boot:run

# Linux/Mac
./mvnw spring-boot:run
```

**Option B: Using Docker**
```bash
# Build and run with Docker
docker build -t whatsapp-chat-clone .
docker run -p 8080:8080 whatsapp-chat-clone

# Or using docker-compose
docker-compose up --build
```

### Access the Application
- Open your browser and go to: `http://localhost:8080`
- The application will start on port 8080

## 🎯 How to Use

### 1. Login
- Enter a username (e.g., "alice", "bob", "charlie")
- Click "Login" to join the chat

### 2. Start Chatting
- Select a user from the sidebar to start a conversation
- Type messages in the input field
- Messages are delivered in real-time
- Chat history is automatically loaded

### 3. Test Real-time Features
- Open multiple browser tabs/windows
- Login with different usernames
- Send messages between users
- Observe real-time message delivery

## 🏗️ Architecture

### WebSocket Communication
- **Protocol**: STOMP over WebSocket with SockJS fallback
- **Message Broker**: Spring's in-memory broker
- **Security**: Session-based user routing
- **Topics**: Session-specific topics for private messaging

### Message Flow
1. **Client connects** via WebSocket to `/ws`
2. **User joins** and gets session-specific topics
3. **Messages sent** to `/app/chat.sendMessage`
4. **Server routes** to receiver's session topic
5. **Real-time delivery** to specific user

### Key Components
- **WebSocketConfig**: WebSocket and STOMP configuration
- **ChatController**: WebSocket message handling
- **WebSocketSessionManager**: Session management and routing
- **UserController**: REST API for user operations
- **MessageController**: Message history API

## 📡 API Endpoints

### REST Endpoints
- `POST /api/users/login` - User login
- `GET /api/users/online` - Get online users
- `GET /api/users/session/{username}` - Get user session ID
- `GET /api/messages/conversation/{user1}/{user2}` - Get conversation history

### WebSocket Endpoints
- `/ws` - WebSocket connection endpoint
- `/app/chat.sendMessage` - Send chat message
- `/app/chat.addUser` - User joins chat
- `/app/chat.leave` - User leaves chat

## 🔧 Configuration

### Database
- **Type**: H2 in-memory database
- **Console**: Available at `http://localhost:8080/h2-console`
- **Auto-creation**: Tables created automatically on startup

### WebSocket
- **Endpoint**: `/ws`
- **Message Broker**: `/topic/*` and `/queue/*`
- **Application Prefix**: `/app/*`

## ☁️ Deploy to Render

1. **Fork this repository** to your GitHub account
2. **Connect to Render** at [render.com](https://render.com)
3. **Create new Web Service** from your repository
4. **Use the included Dockerfile** for automatic configuration
5. **Deploy** and access your live chat application!

## 🏗️ Project Structure

```
src/
├── main/
│   ├── java/com/example/whatsapp_chat_clone/
│   │   ├── config/
│   │   │   ├── WebSocketConfig.java          # WebSocket configuration
│   │   │   ├── CorsConfig.java               # CORS configuration
│   │   │   └── DataInitializer.java          # Demo data setup
│   │   ├── controller/
│   │   │   ├── ChatController.java           # WebSocket message handling
│   │   │   ├── UserController.java           # User REST endpoints
│   │   │   └── MessageController.java        # Message REST endpoints
│   │   ├── model/
│   │   │   ├── User.java                     # User entity
│   │   │   └── Message.java                  # Message entity
│   │   ├── repository/
│   │   │   ├── UserRepository.java           # User data access
│   │   │   └── MessageRepository.java        # Message data access
│   │   └── dto/
│   │       └── ChatMessage.java              # Message DTO
│   └── resources/
│       ├── static/
│       │   └── index.html                    # Frontend UI
│       └── application.properties            # Application configuration
```

## 📝 Demo Users

The application comes with pre-configured demo users:
- **alice** - Default user 1
- **bob** - Default user 2  
- **charlie** - Default user 3

## 🆘 Troubleshooting

### Common Issues
1. **Port 8080 already in use**: Change port in `application.properties`
2. **WebSocket connection failed**: Check browser console for errors
3. **Messages not appearing**: Verify WebSocket connection and subscriptions

### Debug Mode
- Check browser console for WebSocket connection status
- Monitor server logs for message routing
- Use `/api/users/debug/sessions` to see active sessions

---

**Happy Chatting! 🎉**