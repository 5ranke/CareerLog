package com.team03.careerlog.reference;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import java.time.LocalDateTime;

@Entity
@Table(name = "reference_contents")
public class ReferenceContent {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 300)
    private String title;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String url;

    @Column(name = "reference_type", nullable = false, length = 30)
    private String referenceType;

    @Column(name = "thumbnail_url", columnDefinition = "TEXT")
    private String thumbnailUrl;

    @Column(length = 100)
    private String source;

    @Column(columnDefinition = "TEXT")
    private String description;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    protected ReferenceContent() {
    }

    public ReferenceContent(String title, String url, String referenceType,
                            String thumbnailUrl, String source, String description) {
        this.title = title;
        this.url = url;
        this.referenceType = referenceType.toUpperCase();
        this.thumbnailUrl = thumbnailUrl;
        this.source = source;
        this.description = description;
        this.createdAt = LocalDateTime.now();
    }

    public Long getId() { return id; }
    public String getTitle() { return title; }
    public String getUrl() { return url; }
    public String getReferenceType() { return referenceType; }
    public String getThumbnailUrl() { return thumbnailUrl; }
    public String getSource() { return source; }
    public String getDescription() { return description; }
    public LocalDateTime getCreatedAt() { return createdAt; }
}
