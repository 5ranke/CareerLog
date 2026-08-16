package com.team03.careerlog.job;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "job_postings")
public class JobPosting {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "external_id", length = 100)
    private String externalId;

    @Column(length = 100)
    private String source;

    @Column(name = "company_name", nullable = false, length = 200)
    private String companyName;

    @Column(nullable = false, length = 300)
    private String title;

    @Column(columnDefinition = "TEXT")
    private String description;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String url;

    private LocalDate deadline;

    @Column(length = 200)
    private String location;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    protected JobPosting() {
    }

    public JobPosting(String externalId, String source, String companyName, String title,
                      String description, String url, LocalDate deadline, String location) {
        this.externalId = externalId;
        this.source = source;
        this.companyName = companyName;
        this.title = title;
        this.description = description;
        this.url = url;
        this.deadline = deadline;
        this.location = location;
        this.createdAt = LocalDateTime.now();
    }

    public Long getId() { return id; }
    public String getSource() { return source; }
    public String getCompanyName() { return companyName; }
    public String getTitle() { return title; }
    public String getDescription() { return description; }
    public String getUrl() { return url; }
    public LocalDate getDeadline() { return deadline; }
    public String getLocation() { return location; }
}
