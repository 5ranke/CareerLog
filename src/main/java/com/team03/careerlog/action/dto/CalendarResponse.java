package com.team03.careerlog.action.dto;

import java.time.LocalDate;
import java.util.List;

public record CalendarResponse(List<JobDeadline> deadlines, List<ChecklistItemResponse> checklistItems) {
    public record JobDeadline(Long actionPlanId, Long jobPostingId, String companyName, String jobTitle, LocalDate deadline) {}
}
