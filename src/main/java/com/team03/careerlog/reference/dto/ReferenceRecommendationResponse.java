package com.team03.careerlog.reference.dto;

import com.team03.careerlog.reference.NoteReference;

public record ReferenceRecommendationResponse(ReferenceResponse reference, String recommendationReason) {
    public static ReferenceRecommendationResponse from(NoteReference recommendation) {
        return new ReferenceRecommendationResponse(
                ReferenceResponse.from(recommendation.getReference()), recommendation.getRecommendationReason());
    }
}
