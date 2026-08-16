package com.team03.careerlog.action.dto;

import com.team03.careerlog.action.ChecklistItem;

import java.time.LocalDate;

public record ChecklistItemResponse(Long id, Long actionPlanId, String title, String description,
                                    LocalDate dueDate, boolean completed) {
    public static ChecklistItemResponse from(ChecklistItem item) {
        return new ChecklistItemResponse(item.getId(), item.getActionPlan().getId(), item.getTitle(),
                item.getDescription(), item.getDueDate(), item.isCompleted());
    }
}
