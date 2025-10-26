package com.example.whatsapp_chat_clone.config;

import org.springframework.stereotype.Component;
import org.springframework.web.socket.messaging.SessionDisconnectEvent;
import org.springframework.web.socket.messaging.SessionConnectEvent;
import org.springframework.context.event.EventListener;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.concurrent.ConcurrentHashMap;
import java.util.Map;

@Component
public class WebSocketSessionManager {

    @Autowired
    private SimpMessagingTemplate messagingTemplate;

    // Map to store session ID to username mapping
    private final Map<String, String> sessionToUser = new ConcurrentHashMap<>();
    
    // Map to store username to session ID mapping (for one session per user)
    private final Map<String, String> userToSession = new ConcurrentHashMap<>();

    @EventListener
    public void handleSessionConnected(SessionConnectEvent event) {
        // Session connected
    }

    @EventListener
    public void handleSessionDisconnected(SessionDisconnectEvent event) {
        String sessionId = event.getSessionId();
        String username = sessionToUser.remove(sessionId);
        
        if (username != null) {
            userToSession.remove(username);
        }
    }

    public void addUserSession(String sessionId, String username) {
        // Remove old session if user was already connected
        String oldSessionId = userToSession.get(username);
        if (oldSessionId != null) {
            sessionToUser.remove(oldSessionId);
        }
        
        sessionToUser.put(sessionId, username);
        userToSession.put(username, sessionId);
    }

    public String getUsernameBySessionId(String sessionId) {
        return sessionToUser.get(sessionId);
    }

    public String getSessionIdByUsername(String username) {
        return userToSession.get(username);
    }

    public boolean isUserOnline(String username) {
        return userToSession.containsKey(username);
    }

    public void sendMessageToUser(String username, String destination, Object message) {
        String sessionId = getSessionIdByUsername(username);
        
        if (sessionId != null) {
            try {
                // Use session-specific topic for better security
                // This prevents other users from subscribing to someone else's messages
                String sessionTopic = "/topic/session/" + sessionId + destination;
                messagingTemplate.convertAndSend(sessionTopic, message);
            } catch (Exception e) {
                System.err.println("Error sending message to " + username + ": " + e.getMessage());
            }
        }
    }
}
