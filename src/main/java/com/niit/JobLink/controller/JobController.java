package com.niit.joblink.controller;

import com.niit.joblink.model.Job;
import com.niit.joblink.service.JobService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import java.util.List;

@RestController
public class JobController {

    private final JobService jobService;

    public JobController(JobService jobService) {
        this.jobService = jobService;
    }

    @GetMapping("/api/jobs")
    public List<Job> getJobsApi() {
        return jobService.getAllJobs();
    }
}