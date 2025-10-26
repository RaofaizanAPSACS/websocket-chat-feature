package com.example.whatsapp_chat_clone.controller;

import com.example.whatsapp_chat_clone.config.WebSocketSessionManager;
import com.example.whatsapp_chat_clone.dto.ChatMessage;
import com.example.whatsapp_chat_clone.model.Message;
import com.example.whatsapp_chat_clone.model.User;
import com.example.whatsapp_chat_clone.repository.MessageRepository;
import com.example.whatsapp_chat_clone.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.messaging.simp.SimpMessageHeaderAccessor;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Controller;

import java.time.LocalDateTime;
import java.util.Optional;

@Controller
public class ChatController {

    @Autowired
    private SimpMessagingTemplate messagingTemplate;
    
    @Autowired
    private UserRepository userRepository;
    
    @Autowired
    private MessageRepository messageRepository;
    
    @Autowired
    private WebSocketSessionManager sessionManager;

    @MessageMapping("/chat.sendMessage")
    public void sendMessage(@Payload ChatMessage chatMessage, SimpMessageHeaderAccessor headerAccessor) {
        String sessionId = headerAccessor.getSessionId();
        String authenticatedUser = sessionManager.getUsernameBySessionId(sessionId);
        
        // Security: Verify the sender is authenticated and matches the message sender
        if (authenticatedUser == null || !authenticatedUser.equals(chatMessage.getSender())) {
            return;
        }
        
        // Validate receiver exists and is online
        Optional<User> senderOpt = userRepository.findByUsername(chatMessage.getSender());
        Optional<User> receiverOpt = userRepository.findByUsername(chatMessage.getReceiver());
        
        if (senderOpt.isPresent() && receiverOpt.isPresent()) {
            // Save message to database
            Message message = new Message(chatMessage.getContent(), senderOpt.get(), receiverOpt.get());
            messageRepository.save(message);
            
            // Set timestamp for the message
            chatMessage.setTimestamp(LocalDateTime.now());
            
            // Send message ONLY to the receiver (server-side routing)
            if (sessionManager.isUserOnline(chatMessage.getReceiver())) {
                sessionManager.sendMessageToUser(chatMessage.getReceiver(), "/queue/messages", chatMessage);
            }
            
            // Send delivery confirmation back to sender
            ChatMessage confirmation = new ChatMessage();
            confirmation.setType(ChatMessage.MessageType.DELIVERED);
            confirmation.setContent("Message delivered to " + chatMessage.getReceiver());
            confirmation.setSender("System");
            confirmation.setReceiver(chatMessage.getSender());
            confirmation.setTimestamp(LocalDateTime.now());
            sessionManager.sendMessageToUser(chatMessage.getSender(), "/status", confirmation);
        }
    }
    
    // Test endpoint to verify WebSocket is working
    @MessageMapping("/chat.test")
    public void testMessage() {
        messagingTemplate.convertAndSend("/topic/test", "WebSocket is working!");
    }

    @MessageMapping("/chat.addUser")
    public ChatMessage addUser(@Payload ChatMessage chatMessage, SimpMessageHeaderAccessor headerAccessor) {
        String sessionId = headerAccessor.getSessionId();
        String username = chatMessage.getSender();
        
        // Validate user exists
        Optional<User> userOpt = userRepository.findByUsername(username);
        if (!userOpt.isPresent()) {
            return null;
        }
        
        // Register user session
        sessionManager.addUserSession(sessionId, username);
        
        // Update user online status
        User user = userOpt.get();
        user.setOnline(true);
        userRepository.save(user);

        chatMessage.setType(ChatMessage.MessageType.JOIN);
        chatMessage.setTimestamp(LocalDateTime.now());

        // Notify all users about the new user joining
        messagingTemplate.convertAndSend("/topic/public", chatMessage);

        return chatMessage;
    }

    @MessageMapping("/chat.leave")
    public ChatMessage leaveUser(@Payload ChatMessage chatMessage, SimpMessageHeaderAccessor headerAccessor) {
        String sessionId = headerAccessor.getSessionId();
        String authenticatedUser = sessionManager.getUsernameBySessionId(sessionId);
        
        // Security: Verify the user is authenticated
        if (authenticatedUser == null || !authenticatedUser.equals(chatMessage.getSender())) {
            return null;
        }
        
        // Update user offline status
        Optional<User> userOpt = userRepository.findByUsername(chatMessage.getSender());
        if (userOpt.isPresent()) {
            User user = userOpt.get();
            user.setOnline(false);
            userRepository.save(user);
        }
        
        chatMessage.setType(ChatMessage.MessageType.LEAVE);
        chatMessage.setTimestamp(LocalDateTime.now());
        
        // Notify all users about the user leaving
        messagingTemplate.convertAndSend("/topic/public", chatMessage);
        
        return chatMessage;
    }

    @MessageMapping("/chat.typing")
    public void handleTyping(@Payload ChatMessage chatMessage, SimpMessageHeaderAccessor headerAccessor) {
        String sessionId = headerAccessor.getSessionId();
        String authenticatedUser = sessionManager.getUsernameBySessionId(sessionId);
        
        // Security: Verify the user is authenticated
        if (authenticatedUser == null || !authenticatedUser.equals(chatMessage.getSender())) {
            return;
        }
        
        // Send typing indicator only to the receiver
        if (sessionManager.isUserOnline(chatMessage.getReceiver())) {
            sessionManager.sendMessageToUser(chatMessage.getReceiver(), "/queue/typing", chatMessage);
        }
    }

    @MessageMapping("/chat.stopTyping")
    public void handleStopTyping(@Payload ChatMessage chatMessage, SimpMessageHeaderAccessor headerAccessor) {
        String sessionId = headerAccessor.getSessionId();
        String authenticatedUser = sessionManager.getUsernameBySessionId(sessionId);
        
        // Security: Verify the user is authenticated
        if (authenticatedUser == null || !authenticatedUser.equals(chatMessage.getSender())) {
            return;
        }
        
        // Send stop typing indicator only to the receiver
        if (sessionManager.isUserOnline(chatMessage.getReceiver())) {
            sessionManager.sendMessageToUser(chatMessage.getReceiver(), "/queue/typing", chatMessage);
        }
    }

}
