package com.niit.joblink;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cache.annotation.EnableCaching;

@SpringBootApplication
@EnableCaching // Activates Chapter 6 Caching Abstraction 
public class JobLinkApplication {
    public static void main(String[] args) {
        SpringApplication.run(JobLinkApplication.class, args);
    }
}




