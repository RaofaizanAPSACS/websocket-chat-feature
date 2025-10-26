package com.example.whatsapp_chat_clone.config;

import com.example.whatsapp_chat_clone.model.User;
import com.example.whatsapp_chat_clone.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

@Component
public class DataInitializer implements CommandLineRunner {

    @Autowired
    private UserRepository userRepository;

    @Override
    public void run(String... args) throws Exception {
        // Create some demo users if they don't exist
        if (!userRepository.existsByUsername("alice")) {
            userRepository.save(new User("alice"));
        }
        if (!userRepository.existsByUsername("bob")) {
            userRepository.save(new User("bob"));
        }
        if (!userRepository.existsByUsername("charlie")) {
            userRepository.save(new User("charlie"));
        }
        
        System.out.println("Demo users created: alice, bob, charlie");
    }
}
