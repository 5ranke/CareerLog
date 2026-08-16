package com.team03.careerlog.job;

import com.team03.careerlog.user.User;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

import java.time.LocalDateTime;

@Entity
@Table(name = "job_recommendations")
public class JobRecommendation {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "job_posting_id", nullable = false)
    private JobPosting jobPosting;

    @Column(name = "recommendation_reason", columnDefinition = "TEXT")
    private String recommendationReason;

    @Column(nullable = false, length = 30)
    private String status;

    @Column(name = "recommended_at", nullable = false)
    private LocalDateTime recommendedAt;

    protected JobRecommendation() {
    }

    public JobRecommendation(User user, JobPosting jobPosting, String recommendationReason) {
        this.user = user;
        this.jobPosting = jobPosting;
        this.recommendationReason = recommendationReason;
        this.status = "RECOMMENDED";
        this.recommendedAt = LocalDateTime.now();
    }

    public JobPosting getJobPosting() { return jobPosting; }
    public Long getId() { return id; }
    public User getUser() { return user; }
    public String getRecommendationReason() { return recommendationReason; }
    public String getStatus() { return status; }
}
