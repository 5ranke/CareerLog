package com.team03.careerlog.action.dto;

import com.team03.careerlog.action.ActionPlan;

import java.time.LocalDate;
import java.util.List;

public record ActionPlanResponse(Long id, Long jobPostingId, String companyName, String jobTitle,
                                 LocalDate startDate, LocalDate deadline, List<ChecklistItemResponse> checklistItems) {
    public static ActionPlanResponse from(ActionPlan plan, List<ChecklistItemResponse> items) {
        var posting = plan.getJobRecommendation().getJobPosting();
        return new ActionPlanResponse(plan.getId(), posting.getId(), posting.getCompanyName(), posting.getTitle(),
                plan.getStartDate(), plan.getDeadline(), items);
    }
}
