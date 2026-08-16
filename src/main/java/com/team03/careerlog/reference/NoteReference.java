package com.team03.careerlog.reference;

import com.team03.careerlog.note.CareerNote;
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
@Table(name = "note_references")
public class NoteReference {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "career_note_id", nullable = false)
    private CareerNote careerNote;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "reference_id", nullable = false)
    private ReferenceContent reference;

    @Column(name = "recommendation_reason", columnDefinition = "TEXT")
    private String recommendationReason;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    protected NoteReference() {
    }

    public NoteReference(CareerNote careerNote, ReferenceContent reference, String recommendationReason) {
        this.careerNote = careerNote;
        this.reference = reference;
        this.recommendationReason = recommendationReason;
        this.createdAt = LocalDateTime.now();
    }

    public ReferenceContent getReference() { return reference; }
    public String getRecommendationReason() { return recommendationReason; }
}
