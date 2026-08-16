package com.team03.careerlog.job;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface JobRecommendationRepository extends JpaRepository<JobRecommendation, Long> {
    Optional<JobRecommendation> findByUserIdAndJobPostingId(Long userId, Long jobPostingId);
}
