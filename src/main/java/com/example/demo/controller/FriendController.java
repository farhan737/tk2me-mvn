package com.example.demo.controller;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.example.demo.model.FriendRequest;
import com.example.demo.model.User;
import com.example.demo.model.FriendRequest.Status;
import com.example.demo.payload.response.MessageResponse;
import com.example.demo.repository.FriendRequestRepository;
import com.example.demo.repository.UserRepository;
import com.example.demo.security.UserDetailsImpl;

@CrossOrigin(origins = "*", maxAge = 3600)
@RestController
@RequestMapping("/api/friends")
public class FriendController {
    
    @Autowired
    private UserRepository userRepository;
    
    @Autowired
    private FriendRequestRepository friendRequestRepository;
    
    @GetMapping("/list")
    public ResponseEntity<?> getFriends() {
        User currentUser = getCurrentUser();
        
        List<User> friends = currentUser.getFriends().stream()
                .map(friend -> {
                    User userWithoutPassword = new User();
                    userWithoutPassword.setId(friend.getId());
                    userWithoutPassword.setUsername(friend.getUsername());
                    return userWithoutPassword;
                })
                .collect(Collectors.toList());
        
        return ResponseEntity.ok(friends);
    }
    
    @GetMapping("/requests/pending")
    public ResponseEntity<?> getPendingRequests() {
        User currentUser = getCurrentUser();
        
        List<FriendRequest> pendingRequests = friendRequestRepository.findByReceiverAndStatus(
                currentUser, Status.PENDING);
        
        return ResponseEntity.ok(pendingRequests);
    }
    
    @PostMapping("/request/{username}")
    public ResponseEntity<?> sendFriendRequest(@PathVariable String username) {
        User currentUser = getCurrentUser();
        
        if (currentUser.getUsername().equals(username)) {
            return ResponseEntity.badRequest()
                    .body(new MessageResponse("Error: You cannot send a friend request to yourself"));
        }
        
        Optional<User> userOpt = userRepository.findByUsername(username);
        if (!userOpt.isPresent()) {
            return ResponseEntity.badRequest()
                    .body(new MessageResponse("Error: User not found"));
        }
        
        User receiver = userOpt.get();
        
        // Check if they are already friends
        if (currentUser.getFriends().contains(receiver)) {
            return ResponseEntity.badRequest()
                    .body(new MessageResponse("Error: You are already friends with this user"));
        }
        
        // Check if a request already exists
        Optional<FriendRequest> existingRequest = friendRequestRepository.findBySenderAndReceiver(
                currentUser, receiver);
        
        if (existingRequest.isPresent()) {
            FriendRequest request = existingRequest.get();
            if (request.getStatus() == Status.PENDING) {
                return ResponseEntity.badRequest()
                        .body(new MessageResponse("Error: A pending request already exists"));
            } else if (request.getStatus() == Status.REJECTED) {
                // Update the rejected request to pending
                request.setStatus(Status.PENDING);
                request.setUpdatedAt(LocalDateTime.now());
                friendRequestRepository.save(request);
                return ResponseEntity.ok(new MessageResponse("Friend request sent successfully"));
            }
        }
        
        // Check if the receiver has already sent a request to the current user
        Optional<FriendRequest> reverseRequest = friendRequestRepository.findBySenderAndReceiver(
                receiver, currentUser);
        
        if (reverseRequest.isPresent()) {
            FriendRequest request = reverseRequest.get();
            if (request.getStatus() == Status.PENDING) {
                // Auto-accept the reverse request
                request.setStatus(Status.ACCEPTED);
                request.setUpdatedAt(LocalDateTime.now());
                friendRequestRepository.save(request);
                
                // Add each other as friends
                currentUser.getFriends().add(receiver);
                receiver.getFriends().add(currentUser);
                
                userRepository.save(currentUser);
                userRepository.save(receiver);
                
                return ResponseEntity.ok(new MessageResponse("Friend request automatically accepted as the other user had already sent you a request"));
            }
        }
        
        // Create a new friend request
        FriendRequest friendRequest = new FriendRequest();
        friendRequest.setSender(currentUser);
        friendRequest.setReceiver(receiver);
        friendRequest.setStatus(Status.PENDING);
        
        friendRequestRepository.save(friendRequest);
        
        return ResponseEntity.ok(new MessageResponse("Friend request sent successfully"));
    }
    
    @PutMapping("/request/{requestId}/accept")
    public ResponseEntity<?> acceptFriendRequest(@PathVariable Long requestId) {
        User currentUser = getCurrentUser();
        
        Optional<FriendRequest> requestOpt = friendRequestRepository.findById(requestId);
        if (!requestOpt.isPresent()) {
            return ResponseEntity.badRequest()
                    .body(new MessageResponse("Error: Friend request not found"));
        }
        
        FriendRequest request = requestOpt.get();
        
        // Verify the current user is the receiver of the request
        if (!request.getReceiver().getId().equals(currentUser.getId())) {
            return ResponseEntity.badRequest()
                    .body(new MessageResponse("Error: You are not authorized to accept this request"));
        }
        
        // Verify the request is pending
        if (request.getStatus() != Status.PENDING) {
            return ResponseEntity.badRequest()
                    .body(new MessageResponse("Error: This request is not pending"));
        }
        
        // Accept the request
        request.setStatus(Status.ACCEPTED);
        request.setUpdatedAt(LocalDateTime.now());
        friendRequestRepository.save(request);
        
        // Add each other as friends
        User sender = request.getSender();
        currentUser.getFriends().add(sender);
        sender.getFriends().add(currentUser);
        
        userRepository.save(currentUser);
        userRepository.save(sender);
        
        return ResponseEntity.ok(new MessageResponse("Friend request accepted successfully"));
    }
    
    @PutMapping("/request/{requestId}/reject")
    public ResponseEntity<?> rejectFriendRequest(@PathVariable Long requestId) {
        User currentUser = getCurrentUser();
        
        Optional<FriendRequest> requestOpt = friendRequestRepository.findById(requestId);
        if (!requestOpt.isPresent()) {
            return ResponseEntity.badRequest()
                    .body(new MessageResponse("Error: Friend request not found"));
        }
        
        FriendRequest request = requestOpt.get();
        
        // Verify the current user is the receiver of the request
        if (!request.getReceiver().getId().equals(currentUser.getId())) {
            return ResponseEntity.badRequest()
                    .body(new MessageResponse("Error: You are not authorized to reject this request"));
        }
        
        // Verify the request is pending
        if (request.getStatus() != Status.PENDING) {
            return ResponseEntity.badRequest()
                    .body(new MessageResponse("Error: This request is not pending"));
        }
        
        // Reject the request
        request.setStatus(Status.REJECTED);
        request.setUpdatedAt(LocalDateTime.now());
        friendRequestRepository.save(request);
        
        return ResponseEntity.ok(new MessageResponse("Friend request rejected successfully"));
    }
    
    private User getCurrentUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        UserDetailsImpl userDetails = (UserDetailsImpl) authentication.getPrincipal();
        return userRepository.findById(userDetails.getId()).orElseThrow(() -> 
            new RuntimeException("Error: User not found"));
    }
}
