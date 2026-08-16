package com.team03.careerlog.action;

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
@Table(name = "checklist_items")
public class ChecklistItem {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "action_plan_id", nullable = false)
    private ActionPlan actionPlan;

    @Column(nullable = false, length = 300)
    private String title;

    @Column(columnDefinition = "TEXT")
    private String description;

    @Column(name = "due_date", nullable = false)
    private LocalDate dueDate;

    @Column(name = "is_completed", nullable = false)
    private boolean completed;

    @Column(name = "sort_order", nullable = false)
    private int sortOrder;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    protected ChecklistItem() {}

    public ChecklistItem(ActionPlan actionPlan, String title, String description, LocalDate dueDate, int sortOrder) {
        this.actionPlan = actionPlan;
        this.title = title;
        this.description = description;
        this.dueDate = dueDate;
        this.sortOrder = sortOrder;
        this.createdAt = LocalDateTime.now();
        this.updatedAt = this.createdAt;
    }

    public void setCompleted(boolean completed) { this.completed = completed; }
    @PreUpdate void touch() { updatedAt = LocalDateTime.now(); }
    public Long getId() { return id; }
    public ActionPlan getActionPlan() { return actionPlan; }
    public String getTitle() { return title; }
    public String getDescription() { return description; }
    public LocalDate getDueDate() { return dueDate; }
    public boolean isCompleted() { return completed; }
}
