package com.team03.careerlog.ai;

import java.util.List;

public record CareerNoteAnalysis(
        String experience,
        List<String> activities,
        String reaction,
        String reason
) {
}
