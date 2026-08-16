package com.team03.careerlog.action;

import com.team03.careerlog.action.dto.ActionPlanResponse;
import com.team03.careerlog.action.dto.CalendarResponse;
import com.team03.careerlog.action.dto.ChecklistItemResponse;
import com.team03.careerlog.action.dto.ChecklistUpdateRequest;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/api")
public class ActionPlanController {
    private final ActionPlanService service;

    public ActionPlanController(ActionPlanService service) { this.service = service; }

    @PostMapping("/job-postings/{jobPostingId}/action-plan")
    @ResponseStatus(HttpStatus.CREATED)
    public ActionPlanResponse create(Authentication auth, @PathVariable Long jobPostingId) {
        return service.create(auth.getName(), jobPostingId);
    }

    @GetMapping("/action-plans/calendar")
    public CalendarResponse calendar(Authentication auth,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate from,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate to) {
        return service.calendar(auth.getName(), from, to);
    }

    @GetMapping("/action-plans")
    public List<ActionPlanResponse> getAll(Authentication auth) {
        return service.getAll(auth.getName());
    }

    @PatchMapping("/checklist-items/{itemId}")
    public ChecklistItemResponse update(Authentication auth, @PathVariable Long itemId,
                                         @RequestBody ChecklistUpdateRequest request) {
        return service.updateChecklist(auth.getName(), itemId, request.completed());
    }

    @DeleteMapping("/checklist-items/{itemId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteChecklist(Authentication auth, @PathVariable Long itemId) {
        service.deleteChecklist(auth.getName(), itemId);
    }

    @DeleteMapping("/action-plans/{actionPlanId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(Authentication auth, @PathVariable Long actionPlanId) {
        service.delete(auth.getName(), actionPlanId);
    }
}
