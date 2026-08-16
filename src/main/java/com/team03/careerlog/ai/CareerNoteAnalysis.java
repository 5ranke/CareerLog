package com.team03.careerlog.ai;

import java.util.List;

public record CareerNoteAnalysis(
        String summary,
        List<String> preferredJobs,
        List<String> interests,
        List<String> workPreferences
) {
}
