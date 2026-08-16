package com.team03.careerlog.note;

import com.team03.careerlog.note.dto.CareerNoteRequest;
import com.team03.careerlog.note.dto.CareerNoteResponse;
import jakarta.validation.Valid;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/api/career-notes")
public class CareerNoteController {

    private final CareerNoteService careerNoteService;

    public CareerNoteController(CareerNoteService careerNoteService) {
        this.careerNoteService = careerNoteService;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public CareerNoteResponse create(Authentication authentication,
                                     @Valid @RequestBody CareerNoteRequest request) {
        return careerNoteService.create(authentication.getName(), request);
    }

    @GetMapping
    public List<CareerNoteResponse> getAll(
            Authentication authentication,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate from,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate to) {
        return careerNoteService.getAll(authentication.getName(), from, to);
    }

    @GetMapping("/{noteId}")
    public CareerNoteResponse get(Authentication authentication, @PathVariable Long noteId) {
        return careerNoteService.get(authentication.getName(), noteId);
    }

    @PutMapping("/{noteId}")
    public CareerNoteResponse update(Authentication authentication,
                                     @PathVariable Long noteId,
                                     @Valid @RequestBody CareerNoteRequest request) {
        return careerNoteService.update(authentication.getName(), noteId, request);
    }

    @DeleteMapping("/{noteId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(Authentication authentication, @PathVariable Long noteId) {
        careerNoteService.delete(authentication.getName(), noteId);
    }

    @PostMapping("/{noteId}/ai-summary")
    public CareerNoteResponse generateAiSummary(Authentication authentication, @PathVariable Long noteId) {
        return careerNoteService.generateAiSummary(authentication.getName(), noteId);
    }
}
