package com.niit.joblink.service;

import com.niit.joblink.model.User;
import com.niit.joblink.repository.UserRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import jakarta.annotation.PostConstruct;
import java.sql.*;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

@Service
public class SupabaseSyncService {

    private final UserRepository userRepository;

    @Value("${supabase.datasource.url}")
    private String supabaseUrl;

    @Value("${supabase.datasource.username}")
    private String supabaseUser;

    @Value("${supabase.datasource.password}")
    private String supabasePassword;

    public SupabaseSyncService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    /**
     * LIFECYCLE RECOVERY PHASE: Runs automatically on application startup.
     * If H2 has been cleared out by a restart, it pulls data down from Supabase.
     */
    @PostConstruct
    public void restoreFromSupabaseIfH2IsEmpty() {
        if (userRepository.count() == 0) {
            System.out.println("⚠️ [H2 EMPTY] Local memory is blank. Initiating Supabase Recovery pipeline...");
            
            String query = "SELECT username, password, role, status, tokens, paystack_reference FROM users";
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
            String timestamp = LocalDateTime.now().format(formatter);

            try (Connection conn = DriverManager.getConnection(supabaseUrl, supabaseUser, supabasePassword);
                 Statement stmt = conn.createStatement();
                 ResultSet rs = stmt.executeQuery(query)) {

                while (rs.next()) {
                    User user = new User();
                    user.setUsername(rs.getString("username"));
                    user.setPassword(rs.getString("password"));
                    user.setRole(rs.getString("role"));
                    user.setStatus(rs.getString("status"));
                    user.setTokens(rs.getInt("tokens"));
                    user.setPaystackReference(rs.getString("paystack_reference"));
                    
                    // Mark records as recovered from cloud
                    user.setStorageLocation("SUPABASE_CLOUD");
                    user.setDateLastDisplayed("Recovered on: " + timestamp);
                    
                    userRepository.save(user); // Repopulate temporary H2 cache
                }
                System.out.println("✅ [RECOVERY SUCCESS] Database records mirrored back to H2 successfully.");
            } catch (SQLException e) {
                System.err.println("❌ Supabase connection failed during data restoration: " + e.getMessage());
            }
        }
    }

    /**
     * REPLICATION BACKUP PHASE: Pushes a local user modification directly up to Supabase.
     */
    public void pushToSupabase(User user) {
        String upsertQuery = "INSERT INTO users (username, password, role, status, tokens, paystack_reference) " +
                             "VALUES (?, ?, ?, ?, ?, ?) " +
                             "ON CONFLICT (username) DO UPDATE SET " +
                             "password = EXCLUDED.password, role = EXCLUDED.role, status = EXCLUDED.status, " +
                             "tokens = EXCLUDED.tokens, paystack_reference = EXCLUDED.paystack_reference";

        try (Connection conn = DriverManager.getConnection(supabaseUrl, supabaseUser, supabasePassword);
             PreparedStatement stmt = conn.prepareStatement(upsertQuery)) {
            
            stmt.setString(1, user.getUsername());
            stmt.setString(2, user.getPassword());
            stmt.setString(3, user.getRole());
            stmt.setString(4, user.getStatus());
            stmt.setInt(5, user.getTokens());
            stmt.setString(6, user.getPaystackReference() != null ? user.getPaystackReference() : "NONE");
            
            stmt.executeUpdate();
            System.out.println("☁️ [SUPABASE SYNC] Backup copy safely updated in the cloud.");
        } catch (SQLException e) {
            System.err.println("❌ Cloud synchronization failed: " + e.getMessage());
        }
    }
}
