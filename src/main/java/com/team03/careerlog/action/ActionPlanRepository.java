package com.team03.careerlog.action;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface ActionPlanRepository extends JpaRepository<ActionPlan, Long> {
    Optional<ActionPlan> findByJobRecommendationId(Long recommendationId);
    List<ActionPlan> findAllByJobRecommendationUserLoginId(String loginId);
}
