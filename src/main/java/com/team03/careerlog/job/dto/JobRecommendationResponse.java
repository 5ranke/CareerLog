package com.team03.careerlog.job.dto;

import com.team03.careerlog.job.JobPosting;

import java.time.LocalDate;

public record JobRecommendationResponse(
        Long jobPostingId,
        String companyName,
        String title,
        String description,
        String url,
        LocalDate deadline,
        String location,
        String source,
        String recommendationReason,
        String status
) {
    public static JobRecommendationResponse of(
            JobPosting posting, String reason, String status) {
        return new JobRecommendationResponse(posting.getId(), posting.getCompanyName(), posting.getTitle(),
                posting.getDescription(), posting.getUrl(), posting.getDeadline(), posting.getLocation(),
                posting.getSource(), reason, status);
    }
}
