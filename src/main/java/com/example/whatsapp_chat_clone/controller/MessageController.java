package com.example.whatsapp_chat_clone.controller;

import com.example.whatsapp_chat_clone.model.Message;
import com.example.whatsapp_chat_clone.model.User;
import com.example.whatsapp_chat_clone.repository.MessageRepository;
import com.example.whatsapp_chat_clone.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api/messages")
@CrossOrigin(origins = "*")
public class MessageController {

    @Autowired
    private MessageRepository messageRepository;
    
    @Autowired
    private UserRepository userRepository;

    @GetMapping("/conversation/{username1}/{username2}")
    public ResponseEntity<List<Message>> getConversation(
            @PathVariable String username1, 
            @PathVariable String username2) {
        
        Optional<User> user1Opt = userRepository.findByUsername(username1);
        Optional<User> user2Opt = userRepository.findByUsername(username2);
        
        if (user1Opt.isPresent() && user2Opt.isPresent()) {
            List<Message> messages = messageRepository.findConversationBetweenUsers(
                user1Opt.get(), user2Opt.get());
            return ResponseEntity.ok(messages);
        }
        
        return ResponseEntity.notFound().build();
    }

    @GetMapping("/unread/{username}")
    public ResponseEntity<List<Message>> getUnreadMessages(@PathVariable String username) {
        Optional<User> userOpt = userRepository.findByUsername(username);
        if (userOpt.isPresent()) {
            List<Message> unreadMessages = messageRepository.findUnreadMessagesForUser(userOpt.get());
            return ResponseEntity.ok(unreadMessages);
        }
        return ResponseEntity.notFound().build();
    }

    @GetMapping("/unread/count/{username}")
    public ResponseEntity<Long> getUnreadMessageCount(@PathVariable String username) {
        Optional<User> userOpt = userRepository.findByUsername(username);
        if (userOpt.isPresent()) {
            long count = messageRepository.countUnreadMessagesForUser(userOpt.get());
            return ResponseEntity.ok(count);
        }
        return ResponseEntity.notFound().build();
    }

    @PostMapping("/mark-read/{messageId}")
    public ResponseEntity<?> markMessageAsRead(@PathVariable Long messageId) {
        Optional<Message> messageOpt = messageRepository.findById(messageId);
        if (messageOpt.isPresent()) {
            Message message = messageOpt.get();
            message.setRead(true);
            messageRepository.save(message);
            return ResponseEntity.ok("Message marked as read");
        }
        return ResponseEntity.notFound().build();
    }

    @PostMapping("/mark-all-read/{username}")
    public ResponseEntity<?> markAllMessagesAsRead(@PathVariable String username) {
        Optional<User> userOpt = userRepository.findByUsername(username);
        if (userOpt.isPresent()) {
            List<Message> unreadMessages = messageRepository.findUnreadMessagesForUser(userOpt.get());
            for (Message message : unreadMessages) {
                message.setRead(true);
            }
            messageRepository.saveAll(unreadMessages);
            return ResponseEntity.ok("All messages marked as read");
        }
        return ResponseEntity.notFound().build();
    }
}
