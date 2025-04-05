package com.example.demo.model;

import java.util.HashSet;
import java.util.Set;

import com.fasterxml.jackson.annotation.JsonIgnore;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinTable;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToMany;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "users")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class User {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @NotBlank
    @Size(min = 3, max = 20)
    @Column(unique = true)
    private String username;
    
    @NotBlank
    @Size(min = 6, max = 120)
    @JsonIgnore
    private String password;
    
    @OneToMany(mappedBy = "sender")
    @JsonIgnore
    private Set<FriendRequest> sentRequests = new HashSet<>();
    
    @OneToMany(mappedBy = "receiver")
    @JsonIgnore
    private Set<FriendRequest> receivedRequests = new HashSet<>();
    
    @ManyToMany
    @JoinTable(
        name = "users_friends",
        joinColumns = @JoinColumn(name = "user_id"),
        inverseJoinColumns = @JoinColumn(name = "friends_id")
    )
    @JsonIgnore
    private Set<User> friends = new HashSet<>();
    
    public Long getId() {
        return id;
    }
    
    public void setId(Long id) {
        this.id = id;
    }
    
    public Set<User> getFriends() {
        return friends;
    }
    
    public void setFriends(Set<User> friends) {
        this.friends = friends;
    }
    
    public String getUsername() {
        return username;
    }
    
    public void setUsername(String username) {
        this.username = username;
    }
    
    public String getPassword() {
        return password;
    }
    
    public void setPassword(String password) {
        this.password = password;
    }
    
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        User user = (User) o;
        return id != null && id.equals(user.id);
    }

    @Override
    public int hashCode() {
        // Use only the ID for hashCode calculation to avoid circular references
        return id != null ? id.hashCode() : 0;
    }
}
