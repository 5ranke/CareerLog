package com.team03.careerlog.profile;

import java.time.LocalDateTime;

public record CareerProfileResponse(
        Long id,
        String preferredJobs,
        String interests,
        String workPreferences,
        String summary,
        LocalDateTime updatedAt
) {
    public static CareerProfileResponse from(CareerProfile profile) {
        return new CareerProfileResponse(profile.getId(), profile.getPreferredJobs(), profile.getInterests(),
                profile.getWorkPreferences(), profile.getSummary(), profile.getUpdatedAt());
    }
}
