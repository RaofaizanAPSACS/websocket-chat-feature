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

- **Backend**: Spring Boot 3.x
- **Database**: H2 (in-memory)
- **WebSocket**: STOMP over SockJS
- **Message Broker**: Spring's Simple Message Broker
- **Frontend**: HTML, CSS, JavaScript
- **Build Tool**: Maven

## 🌐 WebSocket Communication Protocol

### What are WebSockets?
WebSockets provide **full-duplex communication** between client and server over a single TCP connection. Unlike HTTP's request-response model, WebSockets enable:
- **Real-time bidirectional communication**
- **Low latency** message delivery
- **Persistent connections** that stay open
- **Efficient data transfer** with minimal overhead

### WebSocket vs HTTP
| Feature | HTTP | WebSocket |
|---------|------|-----------|
| **Connection** | Request-Response | Persistent |
| **Latency** | High (new connection each time) | Low (reuse connection) |
| **Real-time** | No (polling required) | Yes (push messages) |
| **Overhead** | High (headers each request) | Low (minimal framing) |
| **Use Case** | Traditional web apps | Real-time applications |

### STOMP Protocol
**STOMP (Simple Text Oriented Messaging Protocol)** is a messaging protocol that works on top of WebSockets:
- **Text-based protocol** (human-readable)
- **Frame-based messaging** with headers and body
- **Destination-based routing** (topics/queues)
- **Message acknowledgment** and error handling
- **Subscription management**

### SockJS Fallback
**SockJS** provides WebSocket-like functionality with fallbacks:
- **Primary**: Native WebSocket connection
- **Fallbacks**: HTTP streaming, long polling, iframe
- **Browser compatibility** across all browsers
- **Automatic fallback** when WebSocket is blocked

## 📡 Message Broker Architecture

### Spring's Simple Message Broker
This application uses Spring's **in-memory message broker**:

```
┌─────────────────┐    WebSocket    ┌──────────────────┐
│   Frontend      │◄──────────────►│   Spring Boot    │
│   (Browser)     │   STOMP/SockJS  │   Application    │
└─────────────────┘                 └──────────────────┘
                                             │
                                             ▼
                                    ┌──────────────────┐
                                    │ Message Broker   │
                                    │ (In-Memory)      │
                                    └──────────────────┘
                                             │
                                             ▼
                                    ┌──────────────────┐
                                    │ Topic/Queue      │
                                    │ Routing          │
                                    └──────────────────┘
```

### Message Flow
1. **Client connects** via WebSocket to `/ws`
2. **STOMP handshake** establishes protocol
3. **Client subscribes** to topics/queues
4. **Messages sent** to `/app/*` endpoints
5. **Server processes** and routes messages
6. **Broker delivers** to subscribed clients

### Topics vs Queues
- **Topics** (`/topic/*`): **Broadcast** to all subscribers
- **Queues** (`/queue/*`): **Point-to-point** delivery to specific user

## 🏗️ Spring Boot WebSocket Implementation

### 1. WebSocket Configuration
```java
@Configuration
@EnableWebSocketMessageBroker
public class WebSocketConfig implements WebSocketMessageBrokerConfigurer {
    
    @Override
    public void configureMessageBroker(MessageBrokerRegistry config) {
        // Enable simple broker for topics and queues
        config.enableSimpleBroker("/topic", "/queue");
        // Set application destination prefix
        config.setApplicationDestinationPrefixes("/app");
    }
    
    @Override
    public void registerStompEndpoints(StompEndpointRegistry registry) {
        // Register WebSocket endpoint with SockJS fallback
        registry.addEndpoint("/ws").withSockJS();
    }
}
```

### 2. Message Controllers
```java
@Controller
public class ChatController {
    
    @MessageMapping("/chat.sendMessage")
    public void sendMessage(@Payload ChatMessage chatMessage) {
        // Process message and send to broker
        messagingTemplate.convertAndSend("/topic/chat", chatMessage);
    }
}
```

### 3. Frontend Connection
```javascript
// Establish WebSocket connection
const socket = new SockJS('/ws');
const stompClient = Stomp.over(socket);

// Connect and subscribe
stompClient.connect({}, function (frame) {
    // Subscribe to topic
    stompClient.subscribe('/topic/chat', function (message) {
        // Handle incoming message
    });
    
    // Send message
    stompClient.send("/app/chat.sendMessage", {}, JSON.stringify(message));
});
```

## 🔄 Real-time Communication Flow

### 1. User Login Flow
```
User → POST /api/users/login → Server
Server → Update User Status → Database
Server → Broadcast JOIN → /topic/public
All Clients → Receive JOIN → Update UI
```

### 2. Message Sending Flow
```
User A → Send Message → /app/chat.sendMessage
Server → Process Message → Save to Database
Server → Broadcast Message → /topic/chat
All Clients → Receive Message → Filter by Receiver
User B → Display Message → Update Chat UI
```

### 3. Auto-switch Conversation Flow
```
User B → Receives Message → Check Sender
If Sender ≠ Current Chat → Auto-switch
Update UI → Load History → Display Message
```

## 🎯 Key Communication Features

### 1. **Real-time Message Delivery**
- **Instant delivery** without page refresh
- **Bidirectional communication** (send/receive)
- **Connection persistence** for efficiency

### 2. **Message Filtering**
- **Client-side filtering** by receiver
- **Auto-switch** to sender's conversation
- **Topic-based routing** for scalability

### 3. **Connection Management**
- **Automatic reconnection** on connection loss
- **Heartbeat mechanism** to detect disconnections
- **Session management** for user tracking

### 4. **Error Handling**
- **Graceful fallbacks** with SockJS
- **Connection error recovery**
- **Message delivery confirmation**

## 🔧 Spring Boot Specific Features

### 1. **SimpMessagingTemplate**
```java
@Autowired
private SimpMessagingTemplate messagingTemplate;

// Send to specific topic
messagingTemplate.convertAndSend("/topic/chat", message);

// Send to specific user
messagingTemplate.convertAndSendToUser(username, "/queue/messages", message);
```

### 2. **Message Mapping Annotations**
```java
@MessageMapping("/chat.sendMessage")  // Maps to /app/chat.sendMessage
@MessageMapping("/chat.addUser")      // Maps to /app/chat.addUser
@MessageMapping("/chat.leave")        // Maps to /app/chat.leave
```

### 3. **Session Management**
```java
@MessageMapping("/chat.addUser")
public ChatMessage addUser(@Payload ChatMessage chatMessage, 
                          SimpMessageHeaderAccessor headerAccessor) {
    // Access session attributes
    headerAccessor.getSessionAttributes().put("username", chatMessage.getSender());
    return chatMessage;
}
```

### 4. **CORS Configuration**
```java
@Configuration
public class CorsConfig {
    @Bean
    public CorsFilter corsFilter() {
        // Allow WebSocket connections from frontend
        CorsConfiguration config = new CorsConfiguration();
        config.addAllowedOrigin("*");
        config.addAllowedMethod("*");
        config.addAllowedHeader("*");
        return new CorsFilter(new UrlBasedCorsConfigurationSource());
    }
}
```

## 📊 Performance Considerations

### 1. **Connection Scaling**
- **In-memory broker** suitable for single instance
- **Redis/RabbitMQ** for multi-instance deployment
- **Connection pooling** for high concurrency

### 2. **Message Optimization**
- **JSON serialization** for message format
- **Message compression** for large payloads
- **Batch processing** for multiple messages

### 3. **Resource Management**
- **Connection cleanup** on user logout
- **Memory management** for message history
- **Database optimization** for message storage

## 🚀 Production Considerations

### 1. **Scalability**
- **Load balancer** with sticky sessions
- **External message broker** (Redis/RabbitMQ)
- **Database clustering** for message persistence

### 2. **Security**
- **Authentication** integration
- **Authorization** for message access
- **Rate limiting** for message sending

### 3. **Monitoring**
- **Connection metrics** tracking
- **Message delivery** monitoring
- **Error logging** and alerting

## 📋 Prerequisites

- Java 17 or higher
- Maven (or use Maven Wrapper included)
- Web browser with JavaScript enabled

## 🚀 Quick Start

### 1. Clone the Repository
```bash
git clone <repository-url>
cd whatsapp-chat-clone
```

### 2. Run the Application

**Option A: Using Maven Wrapper (Recommended)**
```bash
# Windows
.\mvnw.cmd spring-boot:run

# Linux/Mac
./mvnw spring-boot:run
```

**Option B: Using Maven (if installed)**
```bash
mvn spring-boot:run
```

### 3. Access the Application
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
│   │   │   └── Message.java                   # Message entity
│   │   ├── repository/
│   │   │   ├── UserRepository.java            # User data access
│   │   │   └── MessageRepository.java        # Message data access
│   │   └── dto/
│   │       └── ChatMessage.java               # Message DTO
│   └── resources/
│       ├── static/
│       │   └── index.html                     # Frontend UI
│       └── application.properties            # Application configuration
└── test/
    └── java/com/example/whatsapp_chat_clone/
        └── WhatsappChatCloneApplicationTests.java
```

## 🔧 Configuration

### Database Configuration
The application uses H2 in-memory database with the following settings:
- **URL**: `jdbc:h2:mem:testdb`
- **Username**: `sa`
- **Password**: `password`
- **Console**: Available at `http://localhost:8080/h2-console`

### WebSocket Configuration
- **Endpoint**: `/ws`
- **Topics**: `/topic/chat`, `/topic/public`
- **Message Mappings**: `/app/chat.*`

## 📡 API Endpoints

### REST Endpoints
- `POST /api/users/login` - User login
- `GET /api/users/online` - Get online users
- `GET /api/messages/conversation/{user1}/{user2}` - Get conversation history

### WebSocket Endpoints
- `/ws` - WebSocket connection endpoint
- `/app/chat.sendMessage` - Send chat message
- `/app/chat.addUser` - User joins chat
- `/app/chat.leave` - User leaves chat

## 🎨 Frontend Features

- **Responsive Design**: Works on desktop and mobile
- **Real-time Updates**: Messages appear instantly
- **Auto-scroll**: Chat automatically scrolls to latest messages
- **User Status**: Shows online/offline status
- **Message Timestamps**: Displays when messages were sent
- **Auto-switch**: Automatically switches to sender's conversation

## 📝 Demo Users

The application comes with pre-configured demo users:
- **alice** - Default user 1
- **bob** - Default user 2  
- **charlie** - Default user 3


## 📄 License

This project is for educational and demonstration purposes.

## 🆘 Support

If you encounter any issues:
1. Check the troubleshooting section
2. Review browser console for errors
3. Ensure all prerequisites are installed
4. Try restarting the application

---

**Happy Chatting! 🎉**