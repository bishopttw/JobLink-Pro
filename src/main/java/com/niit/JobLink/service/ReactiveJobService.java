package com.niit.joblink.service;

import com.niit.joblink.model.Job;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Flux;
import java.time.Duration;
import java.util.Arrays;
import java.util.List;

@Service
public class ReactiveJobService {

    private final WebClient webClient;

    public ReactiveJobService(WebClient.Builder webClientBuilder) {
        // Initialize non-blocking HTTP client for Chapter 7
        this.webClient = webClientBuilder.baseUrl("https://api.example.com").build();
    }

    /**
     * Chapter 6: Caching Demo
     * This cache intercepts repeated local database calls.
     */
    @Cacheable(value = "localJobsCache")
    public List<Job> getCachedLocalJobs() {
        System.out.println("⚠️ [CACHE MISS] - Fetching from database... (Simulating 3-second delay)");
        try {
            Thread.sleep(3000); 
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
        return Arrays.asList(
            new Job("Senior Java Architect", "CypressWorks", "Lagos, NG", "₦1.2M/mo"),
            new Job("Backend Engineer", "Majux AI", "Ikeja, NG", "₦850k/mo")
        );
    }
	
    /**
     * Chapter 7: Reactive Non-Blocking Streams Demo
     * Emits automated mock data streams over a timeline loop.
     */
    public Flux<Job> getLiveReactiveJobStream() {
        return Flux.just(
            new Job("Cloud Architect (AWS)", "FinTech Corp", "Lagos, NG", "₦1.5M/mo"),
            new Job("Python Developer", "DataWorks", "Remote", "₦700k/mo"),
            new Job("DevOps Engineer", "CypressWorks", "Yaba, NG", "₦1.1M/mo")
        ).delayElements(Duration.ofSeconds(1)); // Streams out elements 1 by 1 asynchronously
    }
}

