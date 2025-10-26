package com.example.whatsapp_chat_clone.dto;

import java.time.LocalDateTime;

public class ChatMessage {
    private String content;
    private String sender;
    private String receiver;
    private LocalDateTime timestamp;
    private MessageType type;
    
    public enum MessageType {
        CHAT, JOIN, LEAVE, TYPING, STOP_TYPING, DELIVERED
    }
    
    // Constructors
    public ChatMessage() {
        this.timestamp = LocalDateTime.now();
    }
    
    public ChatMessage(String content, String sender, String receiver) {
        this();
        this.content = content;
        this.sender = sender;
        this.receiver = receiver;
        this.type = MessageType.CHAT;
    }
    
    // Getters and Setters
    public String getContent() {
        return content;
    }
    
    public void setContent(String content) {
        this.content = content;
    }
    
    public String getSender() {
        return sender;
    }
    
    public void setSender(String sender) {
        this.sender = sender;
    }
    
    public String getReceiver() {
        return receiver;
    }
    
    public void setReceiver(String receiver) {
        this.receiver = receiver;
    }
    
    public LocalDateTime getTimestamp() {
        return timestamp;
    }
    
    public void setTimestamp(LocalDateTime timestamp) {
        this.timestamp = timestamp;
    }
    
    public MessageType getType() {
        return type;
    }
    
    public void setType(MessageType type) {
        this.type = type;
    }
}
