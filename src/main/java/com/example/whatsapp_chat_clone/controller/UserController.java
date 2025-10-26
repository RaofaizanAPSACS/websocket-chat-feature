package com.example.whatsapp_chat_clone.controller;

import com.example.whatsapp_chat_clone.model.User;
import com.example.whatsapp_chat_clone.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api/users")
@CrossOrigin(origins = "*")
public class UserController {

    @Autowired
    private UserRepository userRepository;
    
    @GetMapping("/health")
    public ResponseEntity<String> health() {
        return ResponseEntity.ok("Server is running!");
    }

    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody LoginRequest request) {
        String username = request.getUsername();
        
        if (username == null || username.trim().isEmpty()) {
            return ResponseEntity.badRequest().body("Username cannot be empty");
        }
        
        Optional<User> existingUser = userRepository.findByUsername(username);
        User user;
        
        if (existingUser.isPresent()) {
            user = existingUser.get();
            user.setOnline(true);
        } else {
            user = new User(username);
            user.setOnline(true);
        }
        
        userRepository.save(user);
        return ResponseEntity.ok(user);
    }

    @PostMapping("/logout/{username}")
    public ResponseEntity<?> logout(@PathVariable String username) {
        Optional<User> userOpt = userRepository.findByUsername(username);
        if (userOpt.isPresent()) {
            User user = userOpt.get();
            user.setOnline(false);
            userRepository.save(user);
            return ResponseEntity.ok("User logged out successfully");
        }
        return ResponseEntity.notFound().build();
    }

    @GetMapping("/online")
    public ResponseEntity<List<User>> getOnlineUsers() {
        List<User> onlineUsers = userRepository.findOnlineUsers();
        return ResponseEntity.ok(onlineUsers);
    }

    @GetMapping("/all")
    public ResponseEntity<List<User>> getAllUsers() {
        List<User> users = userRepository.findAll();
        return ResponseEntity.ok(users);
    }

    @GetMapping("/{username}")
    public ResponseEntity<User> getUserByUsername(@PathVariable String username) {
        Optional<User> user = userRepository.findByUsername(username);
        return user.map(ResponseEntity::ok)
                  .orElse(ResponseEntity.notFound().build());
    }

    // Inner class for login request
    public static class LoginRequest {
        private String username;
        
        public String getUsername() {
            return username;
        }
        
        public void setUsername(String username) {
            this.username = username;
        }
    }
}
