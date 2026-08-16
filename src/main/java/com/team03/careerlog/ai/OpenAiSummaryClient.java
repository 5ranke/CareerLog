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
            당신은 취업 준비 기록을 정리하는 도우미입니다.
            입력에 없는 사실은 만들지 말고 반드시 아래 JSON 형식만 반환하세요.
            summary는 오늘 수행한 활동, 배운 점과 다음 행동을 한국어 2문장 이내로 요약하세요.
            나머지 배열에는 노트에서 명확히 드러난 항목만 최대 5개까지 넣으세요.
            {"summary":"요약","preferredJobs":["관심 직무"],"interests":["관심 요소"],"workPreferences":["업무 환경 선호"]}
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

    public Optional<CareerNoteAnalysis> analyze(String title, String content) {
        if (!isConfigured()) {
            return Optional.empty();
        }

        String limitedContent = content.length() > MAX_INPUT_LENGTH
                ? content.substring(0, MAX_INPUT_LENGTH)
                : content;
        String input = (title == null || title.isBlank() ? "" : "제목: " + title + "\n")
                + "내용: " + limitedContent;
        Map<String, Object> request = Map.of(
                "model", model,
                "instructions", INSTRUCTIONS,
                "input", input,
                "max_output_tokens", 350
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
}
