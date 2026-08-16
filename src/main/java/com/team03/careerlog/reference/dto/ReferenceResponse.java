package com.team03.careerlog.reference.dto;

import com.team03.careerlog.reference.ReferenceContent;

import java.time.LocalDateTime;

public record ReferenceResponse(
        Long id, String title, String url, String referenceType,
        String thumbnailUrl, String source, String description, LocalDateTime createdAt
) {
    public static ReferenceResponse from(ReferenceContent reference) {
        return new ReferenceResponse(reference.getId(), reference.getTitle(), reference.getUrl(),
                reference.getReferenceType(), reference.getThumbnailUrl(), reference.getSource(),
                reference.getDescription(), reference.getCreatedAt());
    }
}
