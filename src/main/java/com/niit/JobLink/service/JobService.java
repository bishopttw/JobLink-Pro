package com.niit.joblink.service;

import com.niit.joblink.model.Job;
import com.niit.joblink.model.User;
import com.niit.joblink.repository.JobRepository;
import com.niit.joblink.repository.UserRepository;
import org.springframework.stereotype.Service;
import jakarta.annotation.PostConstruct;
import java.util.List;
import java.util.Optional;

@Service
public class JobService {

    private final JobRepository jobRepository;
    private final UserRepository userRepository;
	private final SupabaseSyncService supabaseSyncService;
	

    public JobService(JobRepository jobRepository, UserRepository userRepository, SupabaseSyncService supabaseSyncService) {
        this.jobRepository = jobRepository;
        this.userRepository = userRepository;
		this.supabaseSyncService = supabaseSyncService;
    }

    @PostConstruct
    public void seedDatabase() {
        if (userRepository.count() == 0) {
            User admin = new User("admin@joblink.com", "admin123", "ADMIN");
            admin.setStatus("ACTIVE");
            userRepository.save(admin);

            User student = new User("student@niit.com", "user123", "USER");
            student.setStatus("ACTIVE");
            student.setTokens(3);
            userRepository.save(student);
        }
        if (jobRepository.count() == 0) {
            jobRepository.save(new Job("Senior Java Architect", "CypressWorks", "Lagos, NG", "₦1,200,000/mo"));
            jobRepository.save(new Job("Backend Engineer (Spring Boot)", "Majux AI", "Ikeja, NG", "₦850,000/mo"));
        }
    }

    public List<Job> getAllJobs() { return jobRepository.findAll(); }
    public Optional<Job> getJobById(Long id) { return jobRepository.findById(id); }
    public void saveManualJob(Job job) { jobRepository.save(job); }
    public List<User> getAllUsers() { return userRepository.findAll(); }
    public Optional<User> findUserByUsername(String name) { return userRepository.findByUsername(name); }
    public Optional<User> findUserById(Long id) { return userRepository.findById(id); }
    public void deleteUserById(Long id) { userRepository.deleteById(id); }

    public boolean unlockJobForUser(Long userId, Long jobId) {
        User user = userRepository.findById(userId).orElse(null);
        if (user == null || !"ACTIVE".equalsIgnoreCase(user.getStatus())) return false;
        if (user.getUnlockedJobIds().contains(jobId)) return true;

        if (user.getTokens() > 0) {
            user.setTokens(user.getTokens() - 1);
            user.getUnlockedJobIds().add(jobId);
            userRepository.save(user);
            return true;
        }
        return false;
    }

    public boolean authenticate(String username, String password) {
        return userRepository.findByUsername(username)
                .map(user -> user.getPassword().equals(password))
                .orElse(false);
    }

    public void registerNewUser(User user) {
        user.setRole("USER");
        user.setStatus("PENDING");
        user.setTokens(0);
        userRepository.save(user);
		supabaseSyncService.pushToSupabase(user);
    }
	
	public void updateUser(User user) {
        userRepository.save(user); // Saves updates to H2
        supabaseSyncService.pushToSupabase(user); // Synchronizes to Supabase cloud
    }
}