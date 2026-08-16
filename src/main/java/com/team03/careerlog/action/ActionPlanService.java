package com.team03.careerlog.action;

import com.team03.careerlog.action.dto.ActionPlanResponse;
import com.team03.careerlog.action.dto.CalendarResponse;
import com.team03.careerlog.action.dto.ChecklistItemResponse;
import com.team03.careerlog.job.JobRecommendation;
import com.team03.careerlog.job.JobRecommendationRepository;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Service
@Transactional(readOnly = true)
public class ActionPlanService {

    private static final List<String> TASKS = List.of(
            "채용 공고의 핵심 업무와 자격 요건 표시",
            "회사와 서비스 조사 후 지원 동기 키워드 정리",
            "공고와 연결되는 내 경험 2개 선정",
            "첫 번째 경험을 STAR 방식으로 정리",
            "두 번째 경험을 STAR 방식으로 정리",
            "이력서에서 관련 경험과 기술 강조",
            "자기소개서 지원 동기 초안 작성",
            "포트폴리오 또는 프로젝트 설명 보완",
            "예상 면접 질문 5개와 답변 정리",
            "이력서·자기소개서 오탈자 최종 점검",
            "제출 파일과 지원 링크 최종 확인",
            "지원서 제출"
    );

    private final JobRecommendationRepository recommendationRepository;
    private final ActionPlanRepository actionPlanRepository;
    private final ChecklistItemRepository checklistRepository;

    public ActionPlanService(JobRecommendationRepository recommendationRepository,
                             ActionPlanRepository actionPlanRepository,
                             ChecklistItemRepository checklistRepository) {
        this.recommendationRepository = recommendationRepository;
        this.actionPlanRepository = actionPlanRepository;
        this.checklistRepository = checklistRepository;
    }

    @Transactional
    public ActionPlanResponse create(String loginId, Long jobPostingId) {
        JobRecommendation recommendation = recommendationRepository.findAll().stream()
                .filter(item -> item.getUser().getLoginId().equals(loginId)
                        && item.getJobPosting().getId().equals(jobPostingId))
                .findFirst()
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "추천 공고를 찾을 수 없습니다."));

        ActionPlan existing = actionPlanRepository.findByJobRecommendationId(recommendation.getId()).orElse(null);
        if (existing != null) return response(existing);

        LocalDate startDate = LocalDate.now();
        LocalDate deadline = recommendation.getJobPosting().getDeadline();
        if (deadline == null || deadline.isBefore(startDate)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "마감된 공고입니다.");
        }
        ActionPlan plan = actionPlanRepository.save(new ActionPlan(recommendation, startDate, deadline));
        List<LocalDate> dates = startDate.datesUntil(deadline.plusDays(1)).toList();
        for (int index = 0; index < dates.size(); index++) {
            String title = taskTitle(index, dates.size());
            checklistRepository.save(new ChecklistItem(plan, title,
                    recommendation.getJobPosting().getTitle() + " 지원 준비", dates.get(index), index));
        }
        return response(plan);
    }

    public CalendarResponse calendar(String loginId, LocalDate from, LocalDate to) {
        List<ChecklistItemResponse> items = checklistRepository
                .findAllByActionPlanJobRecommendationUserLoginIdAndDueDateBetweenOrderByDueDateAscSortOrderAsc(
                        loginId, from, to).stream().map(ChecklistItemResponse::from).toList();
        List<CalendarResponse.JobDeadline> deadlines = actionPlanRepository
                .findAllByJobRecommendationUserLoginId(loginId).stream()
                .filter(plan -> !plan.getDeadline().isBefore(from) && !plan.getDeadline().isAfter(to))
                .map(plan -> {
                    var posting = plan.getJobRecommendation().getJobPosting();
                    return new CalendarResponse.JobDeadline(plan.getId(), posting.getId(), posting.getCompanyName(),
                            posting.getTitle(), plan.getDeadline());
                }).toList();
        return new CalendarResponse(deadlines, items);
    }

    public List<ActionPlanResponse> getAll(String loginId) {
        return actionPlanRepository.findAllByJobRecommendationUserLoginId(loginId).stream()
                .map(this::response).toList();
    }

    @Transactional
    public ChecklistItemResponse updateChecklist(String loginId, Long itemId, boolean completed) {
        ChecklistItem item = checklistRepository.findByIdAndActionPlanJobRecommendationUserLoginId(itemId, loginId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND));
        item.setCompleted(completed);
        return ChecklistItemResponse.from(item);
    }

    @Transactional
    public void deleteChecklist(String loginId, Long itemId) {
        ChecklistItem item = checklistRepository.findByIdAndActionPlanJobRecommendationUserLoginId(itemId, loginId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND));
        checklistRepository.delete(item);
    }

    @Transactional
    public void delete(String loginId, Long actionPlanId) {
        ActionPlan plan = actionPlanRepository.findById(actionPlanId)
                .filter(item -> item.getJobRecommendation().getUser().getLoginId().equals(loginId))
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND));
        actionPlanRepository.delete(plan);
    }

    private String taskTitle(int index, int totalDays) {
        if (index == totalDays - 1) return TASKS.get(TASKS.size() - 1);
        int taskIndex = Math.min(index, TASKS.size() - 2);
        if (index >= TASKS.size() - 1) return "지원 준비 내용 보완 및 검토 " + (index - TASKS.size() + 2);
        return TASKS.get(taskIndex);
    }

    private ActionPlanResponse response(ActionPlan plan) {
        List<ChecklistItemResponse> items = checklistRepository
                .findAllByActionPlanIdOrderByDueDateAscSortOrderAsc(plan.getId()).stream()
                .map(ChecklistItemResponse::from).toList();
        return ActionPlanResponse.from(plan, items);
    }
}
