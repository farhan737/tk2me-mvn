package com.example.demo.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.example.demo.model.FriendRequest;
import com.example.demo.model.User;
import com.example.demo.model.FriendRequest.Status;

@Repository
public interface FriendRequestRepository extends JpaRepository<FriendRequest, Long> {
    List<FriendRequest> findByReceiverAndStatus(User receiver, Status status);
    List<FriendRequest> findBySenderAndStatus(User sender, Status status);
    Optional<FriendRequest> findBySenderAndReceiver(User sender, User receiver);
}
