package com.team03.careerlog.action;

import com.team03.careerlog.job.JobRecommendation;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "action_plans")
public class ActionPlan {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "job_recommendation_id", nullable = false, unique = true)
    private JobRecommendation jobRecommendation;

    private LocalDate startDate;
    private LocalDate deadline;
    private LocalDateTime createdAt;

    protected ActionPlan() {}

    public ActionPlan(JobRecommendation jobRecommendation, LocalDate startDate, LocalDate deadline) {
        this.jobRecommendation = jobRecommendation;
        this.startDate = startDate;
        this.deadline = deadline;
        this.createdAt = LocalDateTime.now();
    }

    public Long getId() { return id; }
    public JobRecommendation getJobRecommendation() { return jobRecommendation; }
    public LocalDate getStartDate() { return startDate; }
    public LocalDate getDeadline() { return deadline; }
}
