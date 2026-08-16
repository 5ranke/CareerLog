package com.team03.careerlog.profile;

import com.team03.careerlog.ai.CareerNoteAnalysis;
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
import java.util.Arrays;
import java.util.Collection;
import java.util.LinkedHashSet;

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

    public void apply(CareerNoteAnalysis analysis) {
        preferredJobs = merge(preferredJobs, analysis.preferredJobs());
        interests = merge(interests, analysis.interests());
        workPreferences = merge(workPreferences, analysis.workPreferences());
        if (analysis.summary() != null && !analysis.summary().isBlank()) {
            String accumulated = summary == null ? analysis.summary() : summary + " " + analysis.summary();
            summary = accumulated.length() > 2_000
                    ? accumulated.substring(accumulated.length() - 2_000)
                    : accumulated;
        }
        updatedAt = LocalDateTime.now();
    }

    private String merge(String current, Collection<String> additions) {
        LinkedHashSet<String> values = new LinkedHashSet<>();
        if (current != null && !current.isBlank()) {
            values.addAll(Arrays.asList(current.split(",\\s*")));
        }
        if (additions != null) {
            additions.stream().filter(value -> value != null && !value.isBlank()).map(String::trim).forEach(values::add);
        }
        return values.isEmpty() ? null : String.join(", ", values);
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
