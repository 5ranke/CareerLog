package com.team03.careerlog.reference;

import com.team03.careerlog.reference.dto.ReferenceRecommendationResponse;
import com.team03.careerlog.reference.dto.ReferenceRequest;
import com.team03.careerlog.reference.dto.ReferenceResponse;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/references")
public class ReferenceController {

    private final ReferenceService service;

    public ReferenceController(ReferenceService service) {
        this.service = service;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public ReferenceResponse create(@Valid @RequestBody ReferenceRequest request) {
        return service.create(request);
    }

    @GetMapping
    public List<ReferenceResponse> getAll() {
        return service.getAll();
    }

    @PostMapping("/recommend/{noteId}")
    public List<ReferenceRecommendationResponse> recommend(
            Authentication authentication,
            @PathVariable Long noteId,
            @RequestParam(defaultValue = "5") int limit) {
        return service.recommend(authentication.getName(), noteId, limit);
    }
}
