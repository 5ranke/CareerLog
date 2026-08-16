package com.team03.careerlog.job;

import com.team03.careerlog.job.dto.JobRecommendationResponse;
import com.team03.careerlog.job.dto.JobSearchRequest;
import jakarta.validation.Valid;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/job-recommendations")
public class JobRecommendationController {

    private final JobRecommendationService service;

    public JobRecommendationController(JobRecommendationService service) {
        this.service = service;
    }

    @PostMapping("/search")
    public List<JobRecommendationResponse> search(
            Authentication authentication,
            @Valid @RequestBody JobSearchRequest request) {
        return service.search(authentication.getName(), request);
    }
}
