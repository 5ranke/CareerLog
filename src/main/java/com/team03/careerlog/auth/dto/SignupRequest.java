package com.team03.careerlog.auth.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public record SignupRequest(
        @NotBlank
        @Size(min = 4, max = 50)
        @Pattern(regexp = "^[a-zA-Z0-9._-]+$", message = "로그인 ID는 영문, 숫자, 점, 밑줄, 하이픈만 사용할 수 있습니다.")
        String loginId,

        @NotBlank
        @Size(min = 8, max = 72)
        String password
) {
}
