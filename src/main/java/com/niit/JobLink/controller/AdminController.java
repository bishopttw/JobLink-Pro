package com.niit.joblink.controller;

import com.niit.joblink.model.User;
import com.niit.joblink.service.JobService;
import com.niit.joblink.service.JobFetcherService;
import jakarta.servlet.http.HttpSession;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

@Controller
@RequestMapping("/admin")
public class AdminController {

    private final JobService jobService;
    private final JobFetcherService jobFetcherService;

    // Single unified constructor injection to safely register both managed beans
    public AdminController(JobService jobService, JobFetcherService jobFetcherService) {
        this.jobService = jobService;
        this.jobFetcherService = jobFetcherService;
    }

    /**
     * Decentralized Administrator Session Guardian Node
     */
    private boolean isNotAdmin(HttpSession session) {
        // Look up primary session descriptor or fall back to native user alias definitions
        User user = (User) session.getAttribute("currentUser");
        if (user == null) {
            user = (User) session.getAttribute("user");
        }
        return user == null || !"ADMIN".equalsIgnoreCase(user.getRole());
    }

    /**
     * Expose endpoint to pull open-source roles instantly from Himalayas API
     */
    @GetMapping("/jobs/sync")
    public String syncExternalApiJobs(HttpSession session) {
        if (isNotAdmin(session)) {
            System.err.println("SECURITY ENGINE: Access denied to automated sync pipeline - Session validation failure.");
            return "redirect:/";
        }
        
        System.out.println("LOG ENGINE: Invoking Himalayas public API integration pipeline stream...");
        
        // Trigger our updated database-backed execution logic
        jobFetcherService.fetchJobsFromHimalayas();
        
        return "redirect:/dashboard?action=synced_success";
    }

    @GetMapping("/users/confirm/{id}")
    public String confirmUser(@PathVariable Long id, HttpSession session) {
        if (isNotAdmin(session)) return "redirect:/";
        jobService.findUserById(id).ifPresent(user -> {
            user.setStatus("ACTIVE");
            jobService.updateUser(user);
        });
        return "redirect:/dashboard?action=confirmed";
    }

    @GetMapping("/users/pause/{id}")
    public String pauseUser(@PathVariable Long id, HttpSession session) {
        if (isNotAdmin(session)) return "redirect:/";
        jobService.findUserById(id).ifPresent(user -> {
            user.setStatus("PAUSED");
            jobService.updateUser(user);
        });
        return "redirect:/dashboard?action=paused";
    }

    @GetMapping("/users/upgrade/{id}")
    public String upgradeUser(@PathVariable Long id, HttpSession session) {
        if (isNotAdmin(session)) return "redirect:/";
        jobService.findUserById(id).ifPresent(user -> {
            user.setRole("ADMIN");
            jobService.updateUser(user);
        });
        return "redirect:/dashboard?action=upgraded";
    }

    @GetMapping("/users/delete/{id}")
    public String deleteUser(@PathVariable Long id, HttpSession session) {
        if (isNotAdmin(session)) return "redirect:/";
        jobService.deleteUserById(id);
        return "redirect:/dashboard?action=deleted";
    }

    @PostMapping("/users/tokens/add")
    public String addTokensToUser(@RequestParam Long userId, @RequestParam int amount, HttpSession session) {
        if (isNotAdmin(session)) return "redirect:/";
        jobService.findUserById(userId).ifPresent(user -> {
            user.setTokens(user.getTokens() + amount);
            if ("PENDING".equalsIgnoreCase(user.getStatus())) {
                user.setStatus("ACTIVE");
            }
            jobService.updateUser(user);
        });
        return "redirect:/dashboard?action=tokensadded";
    }
}