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

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    protected CareerNote() {
    }

    public CareerNote(User user, String title, String content, LocalDate noteDate) {
        this.user = user;
        this.title = normalizeTitle(title);
        this.content = content;
        this.noteDate = noteDate;
        this.createdAt = LocalDateTime.now();
        this.updatedAt = this.createdAt;
    }

    public void update(String title, String content, LocalDate noteDate) {
        this.title = normalizeTitle(title);
        this.content = content;
        this.noteDate = noteDate;
    }

    public void updateAiSummary(String aiSummary) {
        this.aiSummary = aiSummary;
    }

    @PreUpdate
    void updateTimestamp() {
        this.updatedAt = LocalDateTime.now();
    }

    private static String normalizeTitle(String title) {
        return title == null || title.isBlank() ? null : title.trim();
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

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }
}
