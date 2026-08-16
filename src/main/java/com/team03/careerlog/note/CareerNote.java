package com.team03.careerlog.note;

import com.team03.careerlog.user.User;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;

@Entity
@Table(name = "career_notes")
public class CareerNote {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(length = 200)
    private String title;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String content;

    @Column(name = "note_date", nullable = false)
    private LocalDate noteDate;

    @Column(name = "ai_summary", columnDefinition = "TEXT")
    private String aiSummary;

    @Column(name = "input_reason", columnDefinition = "TEXT")
    private String inputReason;

    @Column(columnDefinition = "TEXT")
    private String experience;

    @Column(columnDefinition = "TEXT")
    private String activities;

    @Column(columnDefinition = "TEXT")
    private String reaction;

    @Column(name = "structured_reason", columnDefinition = "TEXT")
    private String reason;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    protected CareerNote() {
    }

    public CareerNote(User user, String title, String content, LocalDate noteDate) {
        this(user, title, content, null, noteDate);
    }

    public CareerNote(User user, String whatDidYouDo, String memorablePoint, String inputReason, LocalDate noteDate) {
        this.user = user;
        this.title = normalizeTitle(whatDidYouDo);
        this.content = memorablePoint;
        this.inputReason = normalize(inputReason);
        this.noteDate = noteDate;
        this.createdAt = LocalDateTime.now();
        this.updatedAt = this.createdAt;
    }

    public void update(String whatDidYouDo, String memorablePoint, String inputReason, LocalDate noteDate) {
        this.title = normalizeTitle(whatDidYouDo);
        this.content = memorablePoint;
        this.inputReason = normalize(inputReason);
        this.noteDate = noteDate;
    }

    public void applyAnalysis(com.team03.careerlog.ai.CareerNoteAnalysis analysis) {
        this.experience = normalize(analysis.experience());
        this.activities = analysis.activities() == null ? null : analysis.activities().stream()
                .filter(value -> value != null && !value.isBlank()).map(String::trim).distinct()
                .limit(10).reduce((left, right) -> left + "\n" + right).orElse(null);
        this.reaction = normalize(analysis.reaction());
        this.reason = normalize(analysis.reason());
    }

    @PreUpdate
    void updateTimestamp() {
        this.updatedAt = LocalDateTime.now();
    }

    private static String normalizeTitle(String title) {
        return title == null || title.isBlank() ? null : title.trim();
    }

    private static String normalize(String value) {
        return value == null || value.isBlank() ? null : value.trim();
    }

    public Long getId() {
        return id;
    }

    public String getTitle() {
        return title;
    }

    public String getContent() {
        return content;
    }

    public LocalDate getNoteDate() {
        return noteDate;
    }

    public String getAiSummary() {
        return aiSummary;
    }

    public String getInputReason() { return inputReason; }
    public String getExperience() { return experience; }
    public List<String> getActivities() {
        return activities == null || activities.isBlank() ? List.of() : Arrays.asList(activities.split("\\n"));
    }
    public String getReaction() { return reaction; }
    public String getReason() { return reason; }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }
}
