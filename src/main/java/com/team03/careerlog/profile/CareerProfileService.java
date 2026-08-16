package com.team03.careerlog.profile;

import com.team03.careerlog.ai.CareerNoteAnalysis;
import com.team03.careerlog.user.User;
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

    @Transactional
    public void apply(User user, CareerNoteAnalysis analysis) {
        CareerProfile profile = repository.findByUserLoginId(user.getLoginId())
                .orElseGet(() -> new CareerProfile(user));
        profile.apply(analysis);
        repository.save(profile);
    }

    public CareerProfileResponse get(String loginId) {
        return repository.findByUserLoginId(loginId)
                .map(CareerProfileResponse::from)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "아직 생성된 커리어 프로필이 없습니다."));
    }
}
