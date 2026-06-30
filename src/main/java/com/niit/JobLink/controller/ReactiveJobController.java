package com.niit.joblink.controller;

import com.niit.joblink.model.Job;
import com.niit.joblink.service.ReactiveJobService;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Flux;
import java.util.List;

@RestController
public class ReactiveJobController {

    private final ReactiveJobService reactiveJobService;

    public ReactiveJobController(ReactiveJobService reactiveJobService) {
        this.reactiveJobService = reactiveJobService;
    }

    // 1. Test Caching Optimization Page
    @GetMapping("/api/jobs/cached")
    public List<Job> testCacheRoute() {
        long startTime = System.currentTimeMillis();
        List<Job> data = reactiveJobService.getCachedLocalJobs();
        long endTime = System.currentTimeMillis();
        System.out.println(">>> [METRICS ENGINE]: Request filled in: " + (endTime - startTime) + " ms");
        return data;
    }

    // 2. Test Live Reactive Stream Page (Server-Sent Events)
    @GetMapping(value = "/api/jobs/stream", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public Flux<Job> streamLiveJobs() {
        return reactiveJobService.getLiveReactiveJobStream();
    }
}
