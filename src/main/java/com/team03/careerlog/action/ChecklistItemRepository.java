package com.team03.careerlog.action;

import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public interface ChecklistItemRepository extends JpaRepository<ChecklistItem, Long> {
    List<ChecklistItem> findAllByActionPlanIdOrderByDueDateAscSortOrderAsc(Long actionPlanId);
    List<ChecklistItem> findAllByActionPlanJobRecommendationUserLoginIdAndDueDateBetweenOrderByDueDateAscSortOrderAsc(
            String loginId, LocalDate from, LocalDate to);
    Optional<ChecklistItem> findByIdAndActionPlanJobRecommendationUserLoginId(Long id, String loginId);
}
