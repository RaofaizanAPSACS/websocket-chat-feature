package com.example.whatsapp_chat_clone.repository;

import com.example.whatsapp_chat_clone.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {
    Optional<User> findByUsername(String username);
    
    @Query("SELECT u FROM User u WHERE u.isOnline = true")
    List<User> findOnlineUsers();
    
    boolean existsByUsername(String username);
}
