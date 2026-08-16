package com.team03.careerlog.auth;

import com.team03.careerlog.auth.dto.SignupRequest;
import com.team03.careerlog.auth.dto.UserResponse;
import com.team03.careerlog.user.User;
import com.team03.careerlog.user.UserRepository;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

@Service
@Transactional(readOnly = true)
public class AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public AuthService(UserRepository userRepository, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Transactional
    public UserResponse signup(SignupRequest request) {
        String loginId = request.loginId().trim();
        if (userRepository.existsByLoginId(loginId)) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "이미 사용 중인 로그인 ID입니다.");
        }

        User user = new User(loginId, passwordEncoder.encode(request.password()));
        return UserResponse.from(userRepository.save(user));
    }

    public UserResponse getUser(String loginId) {
        return userRepository.findByLoginId(loginId)
                .map(UserResponse::from)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED));
    }
}
