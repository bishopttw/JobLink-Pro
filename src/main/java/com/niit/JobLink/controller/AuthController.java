package com.niit.joblink.controller;

import com.niit.joblink.model.Job;
import com.niit.joblink.model.User;
import com.niit.joblink.service.JobService;
import jakarta.servlet.http.HttpSession;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@Controller
public class AuthController {

    private final JobService jobService;

    public AuthController(JobService jobService) {
        this.jobService = jobService;
    }

    @GetMapping("/")
    public String showLoginPage() { return "login"; }

    @GetMapping("/register")
    public String showRegistrationPage() { return "register"; }

    @PostMapping("/register")
    public String registerUser(@ModelAttribute User user) {
        jobService.registerNewUser(user);
        return "redirect:/?registered=true";
    }

    @PostMapping("/login")
    public String login(@RequestParam String username, @RequestParam String password, HttpSession session) {
        if (jobService.authenticate(username, password)) {
            User loggedInUser = jobService.findUserByUsername(username).get();
            if ("PAUSED".equalsIgnoreCase(loggedInUser.getStatus())) {
                return "redirect:/?error=paused";
            }
            session.setAttribute("currentUser", loggedInUser);
            return "redirect:/dashboard";
        }
        return "redirect:/?error=true";
    }

    @GetMapping("/dashboard")
    public String showDashboardPage(Model model, HttpSession session) {
        User sessionUser = (User) session.getAttribute("currentUser");
        if (sessionUser == null) return "redirect:/";

        User dbUser = jobService.findUserById(sessionUser.getId()).get();
        session.setAttribute("currentUser", dbUser);

        List<Job> liveJobs = jobService.getAllJobs();
        model.addAttribute("jobs", liveJobs);
        model.addAttribute("user", dbUser);

        if ("ADMIN".equalsIgnoreCase(dbUser.getRole())) {
            model.addAttribute("allUsers", jobService.getAllUsers());
        }
        return "dashboard";
    }

    @GetMapping("/jobs/unlock/{id}")
    public String unlockJob(@PathVariable Long id, HttpSession session) {
        User user = (User) session.getAttribute("currentUser");
        if (user == null) return "redirect:/";

        boolean success = jobService.unlockJobForUser(user.getId(), id);
        if (!success) return "redirect:/dashboard?error=notokens";
        return "redirect:/dashboard?unlocked=" + id;
    }

    @PostMapping("/jobs/post")
    public String adminPostJob(@ModelAttribute Job job, HttpSession session) {
        User user = (User) session.getAttribute("currentUser");
        if (user != null && "ADMIN".equalsIgnoreCase(user.getRole())) {
            jobService.saveManualJob(job);
            return "redirect:/dashboard?posted=true";
        }
        return "redirect:/dashboard?unauthorized=true";
    }

    @GetMapping("/logout")
    public String terminate(HttpSession session) {
        session.invalidate();
        return "redirect:/";
    }
}