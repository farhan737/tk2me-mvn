package com.example.demo.controller;

import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.example.demo.model.Message;
import com.example.demo.model.User;
import com.example.demo.payload.request.MessageRequest;
import com.example.demo.payload.response.MessageResponse;
import com.example.demo.repository.MessageRepository;
import com.example.demo.repository.UserRepository;
import com.example.demo.security.UserDetailsImpl;

import jakarta.validation.Valid;

@CrossOrigin(origins = "*", maxAge = 3600)
@RestController
@RequestMapping("/api/messages")
public class MessageController {
    
    @Autowired
    private UserRepository userRepository;
    
    @Autowired
    private MessageRepository messageRepository;
    
    @GetMapping("/conversation/{username}")
    public ResponseEntity<?> getConversation(@PathVariable String username) {
        User currentUser = getCurrentUser();
        
        Optional<User> otherUserOpt = userRepository.findByUsername(username);
        if (!otherUserOpt.isPresent()) {
            return ResponseEntity.badRequest()
                    .body(new MessageResponse("Error: User not found"));
        }
        
        User otherUser = otherUserOpt.get();
        
        // Check if they are friends
        if (!currentUser.getFriends().contains(otherUser)) {
            return ResponseEntity.badRequest()
                    .body(new MessageResponse("Error: You are not friends with this user"));
        }
        
        List<Message> conversation = messageRepository.findConversation(currentUser, otherUser);
        
        // Mark messages as read
        conversation.stream()
                .filter(message -> message.getReceiver().getId().equals(currentUser.getId()) && !message.isRead())
                .forEach(message -> {
                    message.setRead(true);
                    messageRepository.save(message);
                });
        
        return ResponseEntity.ok(conversation);
    }
    
    @PostMapping("/send/{username}")
    public ResponseEntity<?> sendMessage(@PathVariable String username, 
                                         @Valid @RequestBody MessageRequest messageRequest) {
        User currentUser = getCurrentUser();
        
        Optional<User> receiverOpt = userRepository.findByUsername(username);
        if (!receiverOpt.isPresent()) {
            return ResponseEntity.badRequest()
                    .body(new MessageResponse("Error: User not found"));
        }
        
        User receiver = receiverOpt.get();
        
        // Check if they are friends
        if (!currentUser.getFriends().contains(receiver)) {
            return ResponseEntity.badRequest()
                    .body(new MessageResponse("Error: You are not friends with this user"));
        }
        
        Message message = new Message();
        message.setSender(currentUser);
        message.setReceiver(receiver);
        message.setContent(messageRequest.getContent());
        
        messageRepository.save(message);
        
        return ResponseEntity.ok(message);
    }
    
    @GetMapping("/unread")
    public ResponseEntity<?> getUnreadMessages() {
        User currentUser = getCurrentUser();
        
        List<Message> unreadMessages = messageRepository.findByReceiverAndReadFalse(currentUser);
        
        return ResponseEntity.ok(unreadMessages);
    }
    
    private User getCurrentUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        UserDetailsImpl userDetails = (UserDetailsImpl) authentication.getPrincipal();
        return userRepository.findById(userDetails.getId()).orElseThrow(() -> 
            new RuntimeException("Error: User not found"));
    }
}
