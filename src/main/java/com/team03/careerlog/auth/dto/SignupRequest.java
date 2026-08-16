package com.team03.careerlog.auth.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public record SignupRequest(
        @NotBlank
        @Size(min = 2, max = 50)
        @Pattern(regexp = "^[가-힣a-zA-Z0-9._-]+$", message = "로그인 ID는 한글, 영문, 숫자, 점, 밑줄, 하이픈만 사용할 수 있습니다.")
        String loginId,

        @NotBlank
        @Size(min = 8, max = 72)
        String password
) {
}
