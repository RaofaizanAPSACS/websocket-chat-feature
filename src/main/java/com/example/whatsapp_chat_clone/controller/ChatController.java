package com.example.whatsapp_chat_clone.controller;

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

import java.util.Optional;

@Controller
public class ChatController {

    @Autowired
    private SimpMessagingTemplate messagingTemplate;
    
    @Autowired
    private UserRepository userRepository;
    
    @Autowired
    private MessageRepository messageRepository;

    @MessageMapping("/chat.sendMessage")
    public void sendMessage(@Payload ChatMessage chatMessage) {
        // Save to database
        Optional<User> senderOpt = userRepository.findByUsername(chatMessage.getSender());
        Optional<User> receiverOpt = userRepository.findByUsername(chatMessage.getReceiver());
        
        if (senderOpt.isPresent() && receiverOpt.isPresent()) {
            Message message = new Message(chatMessage.getContent(), senderOpt.get(), receiverOpt.get());
            messageRepository.save(message);
            
            // Send to all users - frontend will filter
            messagingTemplate.convertAndSend("/topic/chat", chatMessage);
        }
    }

    @MessageMapping("/chat.addUser")
    public ChatMessage addUser(@Payload ChatMessage chatMessage, SimpMessageHeaderAccessor headerAccessor) {
        // Add username in web socket session
        if (headerAccessor.getSessionAttributes() != null) {
            headerAccessor.getSessionAttributes().put("username", chatMessage.getSender());
        }
        
        
        // Update user online status
        Optional<User> userOpt = userRepository.findByUsername(chatMessage.getSender());
        if (userOpt.isPresent()) {
            User user = userOpt.get();
            user.setOnline(true);
            userRepository.save(user);
        }
        
        chatMessage.setType(ChatMessage.MessageType.JOIN);
        
        // Send to all users to update online status
        messagingTemplate.convertAndSend("/topic/public", chatMessage);
        
        return chatMessage;
    }

    @MessageMapping("/chat.leave")
    public ChatMessage leaveUser(@Payload ChatMessage chatMessage) {
        // Update user offline status
        Optional<User> userOpt = userRepository.findByUsername(chatMessage.getSender());
        if (userOpt.isPresent()) {
            User user = userOpt.get();
            user.setOnline(false);
            userRepository.save(user);
        }
        
        
        chatMessage.setType(ChatMessage.MessageType.LEAVE);
        
        // Send to all users to update online status
        messagingTemplate.convertAndSend("/topic/public", chatMessage);
        
        return chatMessage;
    }

    @MessageMapping("/chat.typing")
    public void handleTyping(@Payload ChatMessage chatMessage) {
        messagingTemplate.convertAndSendToUser(
            chatMessage.getReceiver(), 
            "/queue/typing", 
            chatMessage
        );
    }

    @MessageMapping("/chat.stopTyping")
    public void handleStopTyping(@Payload ChatMessage chatMessage) {
        messagingTemplate.convertAndSendToUser(
            chatMessage.getReceiver(), 
            "/queue/typing", 
            chatMessage
        );
    }

}
