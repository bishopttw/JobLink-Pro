package com.niit.joblink.model;

import jakarta.persistence.*;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "users")
public class User {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(unique = true, nullable = false)
    private String username;
    private String password;
    private String role; // "ADMIN" or "USER"
    private String status; // "PENDING", "ACTIVE", "PAUSED"
    private int tokens;    
    private String paystackReference; 
	
	private String storageLocation = "LOCAL_H2"; // "H2" or "SUPABASE"
	private String dateLastDisplayed = "Freshly Created";

    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(name = "user_unlocked_jobs", joinColumns = @JoinColumn(name = "user_id"))
    @Column(name = "job_id")
    private List<Long> unlockedJobIds = new ArrayList<>();

    public User() {}
    public User(String username, String password, String role) {
        this.username = username;
        this.password = password;
        this.role = role;
        this.status = "PENDING";
        this.tokens = 0;
        this.paystackReference = "NONE";
    }

    public Long getId() { return id; }
    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }
    public String getPassword() { return password; }
    public void setPassword(String password) { this.password = password; }
    public String getRole() { return role; }
    public void setRole(String role) { this.role = role; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
    public int getTokens() { return tokens; }
    public void setTokens(int tokens) { this.tokens = tokens; }
    public String getPaystackReference() { return paystackReference; }
    public void setPaystackReference(String ref) { this.paystackReference = ref; }
    public List<Long> getUnlockedJobIds() { return unlockedJobIds; }
    public void setUnlockedJobIds(List<Long> unlockedJobIds) { this.unlockedJobIds = unlockedJobIds; }
	public String getStorageLocation() { return storageLocation; }
	public void setStorageLocation(String storageLocation) { this.storageLocation = storageLocation; }
	public String getDateLastDisplayed() { return dateLastDisplayed; }
	public void setDateLastDisplayed(String dateLastDisplayed) { this.dateLastDisplayed = dateLastDisplayed; }
}