package com.team03.careerlog.profile;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

@Service
@Transactional(readOnly = true)
public class CareerProfileService {

    private final CareerProfileRepository repository;

    public CareerProfileService(CareerProfileRepository repository) {
        this.repository = repository;
    }

    public CareerProfileResponse get(String loginId) {
        return repository.findByUserLoginId(loginId)
                .map(CareerProfileResponse::from)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "아직 생성된 커리어 프로필이 없습니다."));
    }
}
