package com.team03.careerlog.profile;

import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/career-profile")
public class CareerProfileController {

    private final CareerProfileService service;

    public CareerProfileController(CareerProfileService service) {
        this.service = service;
    }

    @GetMapping
    public CareerProfileResponse get(Authentication authentication) {
        return service.get(authentication.getName());
    }
}
