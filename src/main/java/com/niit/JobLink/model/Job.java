package com.niit.joblink.model;

import jakarta.persistence.*;

@Entity
@Table(name = "jobs")
public class Job {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String title;
    private String company;
    private String location;
    private String salary;

    public Job() {}
    public Job(String title, String company, String location, String salary) {
        this.title = title;
        this.company = company;
        this.location = location;
        this.salary = salary;
    }

    public Long getId() { return id; }
    public String getTitle() { return title; }
    public void setTitle(String t) { this.title = t; }
    public String getCompany() { return company; }
    public void setCompany(String c) { this.company = c; }
    public String getLocation() { return location; }
    public void setLocation(String l) { this.location = l; }
    public String getSalary() { return salary; }
    public void setSalary(String s) { this.salary = s; }
}