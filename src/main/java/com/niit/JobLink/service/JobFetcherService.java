package com.niit.joblink.service;

import com.niit.joblink.model.Job;
import com.niit.joblink.repository.JobRepository;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Service
public class JobFetcherService {

    private final JobRepository jobRepository;
    private final RestTemplate restTemplate;

    public JobFetcherService(JobRepository jobRepository) {
        this.jobRepository = jobRepository;
        this.restTemplate = new RestTemplate();
    }

    /**
     * Pipeline 3: Fetch from Live Himalayas Public API and save to local DB
     */
    public void fetchJobsFromHimalayas() {
        try {
            String url = "https://himalayas.app/jobs/api";
            System.out.println("LOG ENGINE: Contacting Himalayas API at: " + url);

            // Fetch the raw JSON payload mapped into a standard Map structure
            Map<String, Object> response = restTemplate.getForObject(url, Map.class);
            
            if (response != null && response.containsKey("jobs")) {
                List<Map<String, Object>> jobsList = (List<Map<String, Object>>) response.get("jobs");
                List<Job> jobBatch = new ArrayList<>();
                
                // Pull the top 5 live listings to keep the dashboard fast
                int itemsToFetch = Math.min(jobsList.size(), 5);
                System.out.println("LOG ENGINE: Successfully parsed " + jobsList.size() + " total items. Ingesting top " + itemsToFetch + " positions...");

                for (int i = 0; i < itemsToFetch; i++) {
                    Map<String, Object> apiJob = jobsList.get(i);
                    
                    // Extract data elements safely using the API's actual keys
                    String title = (String) apiJob.getOrDefault("title", "Premium Technical Role");
                    String company = (String) apiJob.getOrDefault("companyName", "Confidential Enterprise");
                    
                    // Location processing (Extracting values from list or fallback)
                    String location = "Remote";
                    Object locationData = apiJob.get("locationRestrictions");
                    if (locationData instanceof List && !((List<?>) locationData).isEmpty()) {
                        location = ((List<?>) locationData).get(0).toString();
                    } else if (locationData != null) {
                        location = locationData.toString();
                    }

                    String salary = "Competitive Framework";

                    // Map it cleanly to your real Database Entity model constructor
                    Job job = new Job(title, company, location, salary);
                    jobBatch.add(job);
                }

                if (!jobBatch.isEmpty()) {
                    jobRepository.saveAll(jobBatch);
                    System.out.println("SUCCESS: Ingested " + jobBatch.size() + " positions into the system database!");
                }
            } else {
                System.err.println("WARNING: Target endpoint returned successfully but payload is missing 'jobs' collection element.");
            }
        } catch (Exception e) {
            System.err.println("CRITICAL: Failed to pull data stream from Himalayas API. Reason: " + e.getMessage());
            e.printStackTrace();
        }
    }
}