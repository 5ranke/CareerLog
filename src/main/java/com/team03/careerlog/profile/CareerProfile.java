package com.team03.careerlog.profile;

import com.team03.careerlog.user.User;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OneToOne;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;

import java.time.LocalDateTime;

@Entity
@Table(name = "career_profiles")
public class CareerProfile {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id", nullable = false, unique = true)
    private User user;

    @Column(name = "preferred_jobs", columnDefinition = "TEXT")
    private String preferredJobs;

    @Column(columnDefinition = "TEXT")
    private String interests;

    @Column(name = "work_preferences", columnDefinition = "TEXT")
    private String workPreferences;

    @Column(columnDefinition = "TEXT")
    private String summary;

    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    protected CareerProfile() {
    }

    public CareerProfile(User user) {
        this.user = user;
        this.updatedAt = LocalDateTime.now();
    }

    @PreUpdate
    void updateTimestamp() {
        updatedAt = LocalDateTime.now();
    }

    public Long getId() { return id; }
    public String getPreferredJobs() { return preferredJobs; }
    public String getInterests() { return interests; }
    public String getWorkPreferences() { return workPreferences; }
    public String getSummary() { return summary; }
    public LocalDateTime getUpdatedAt() { return updatedAt; }
}
