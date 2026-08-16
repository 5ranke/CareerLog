package com.team03.careerlog.reference.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record ReferenceRequest(
        @NotBlank @Size(max = 300) String title,
        @NotBlank String url,
        @NotBlank @Size(max = 30) String referenceType,
        String thumbnailUrl,
        @Size(max = 100) String source,
        String description
) {
}
