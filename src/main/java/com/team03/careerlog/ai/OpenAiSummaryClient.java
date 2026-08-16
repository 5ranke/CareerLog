package com.team03.careerlog.ai;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import tools.jackson.databind.json.JsonMapper;

import java.util.List;
import java.util.Map;
import java.util.Optional;

@Component
public class OpenAiSummaryClient {

    private static final int MAX_INPUT_LENGTH = 6_000;

    private static final String INSTRUCTIONS = """
            당신은 사용자의 취업 준비 기록을 사실 그대로 구조화하는 도우미입니다.
            사용자의 성향, 능력, 적합 직무를 추론하지 마세요.
            입력에 명시되지 않은 사실, 감정, 이유를 임의로 만들지 마세요.
            experience는 사용자가 수행하거나 접한 경험입니다.
            activities는 그 경험에서 수행하거나 주목한 구체적인 활동만 문자열 배열로 작성합니다.
            reaction은 활동에 대해 사용자가 직접 밝힌 생각이나 반응입니다. 확인할 수 없으면 null입니다.
            reason은 사용자가 직접 작성한 반응 이유입니다. 세 번째 답변이 없으면 반드시 null입니다.
            마크다운 없이 다음 키만 포함한 JSON 객체 하나를 반환하세요.
            {"experience":"경험 또는 null","activities":["구체적 활동"],"reaction":"반응 또는 null","reason":"직접 작성한 이유 또는 null"}
            """;

    private final RestClient restClient;
    private final String apiKey;
    private final String model;
    private final JsonMapper jsonMapper = JsonMapper.builder().build();

    public OpenAiSummaryClient(
            @Value("${ai.openai.api-key:}") String apiKey,
            @Value("${ai.openai.model:gpt-4o-mini}") String model,
            @Value("${ai.openai.base-url:https://api.openai.com}") String baseUrl) {
        this.restClient = RestClient.builder().baseUrl(baseUrl).build();
        this.apiKey = apiKey;
        this.model = model;
    }

    public boolean isConfigured() {
        return apiKey != null && !apiKey.isBlank();
    }

    public Optional<CareerNoteAnalysis> analyze(String whatDidYouDo, String memorablePoint, String reason) {
        if (!isConfigured()) {
            return Optional.empty();
        }

        String input = "1. 오늘 취준과 관련해서 무엇을 했거나 접했나요?\n"
                + limit(whatDidYouDo) + "\n\n"
                + "2. 그중 어떤 점이 가장 기억에 남았나요?\n"
                + limit(memorablePoint) + "\n\n"
                + "3. 왜 그렇게 느꼈던 것 같나요? (선택)\n"
                + (reason == null || reason.isBlank() ? "[작성하지 않음]" : limit(reason));
        Map<String, Object> request = Map.of(
                "model", model,
                "instructions", INSTRUCTIONS,
                "input", input,
                "max_output_tokens", 250
        );

        Map<?, ?> response = restClient.post()
                .uri("/v1/responses")
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + apiKey)
                .body(request)
                .retrieve()
                .body(Map.class);

        if (response == null || !(response.get("output") instanceof List<?> outputs)) {
            return Optional.empty();
        }

        for (Object outputValue : outputs) {
            if (!(outputValue instanceof Map<?, ?> output)
                    || !(output.get("content") instanceof List<?> contents)) {
                continue;
            }
            for (Object contentValue : contents) {
                if (contentValue instanceof Map<?, ?> item
                        && "output_text".equals(item.get("type"))
                        && item.get("text") instanceof String text
                        && !text.isBlank()) {
                    try {
                        return Optional.of(jsonMapper.readValue(text, CareerNoteAnalysis.class));
                    } catch (Exception exception) {
                        throw new IllegalStateException("AI 분석 JSON을 해석하지 못했습니다.", exception);
                    }
                }
            }
        }
        return Optional.empty();
    }

    private String limit(String value) {
        if (value == null) return "";
        return value.length() > MAX_INPUT_LENGTH ? value.substring(0, MAX_INPUT_LENGTH) : value;
    }
}
