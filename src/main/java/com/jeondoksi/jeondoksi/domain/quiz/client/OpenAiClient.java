package com.jeondoksi.jeondoksi.domain.quiz.client;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.jeondoksi.jeondoksi.global.error.BusinessException;
import com.jeondoksi.jeondoksi.global.error.ErrorCode;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

@Slf4j
@Component
@RequiredArgsConstructor
public class OpenAiClient {

    @Value("${openai.api-key}")
    private String apiKey;

    @Value("${openai.model}")
    private String model;

    private final RestTemplate restTemplate = new RestTemplate();
    private final Gson gson = new Gson();

    private static final String OPENAI_URL = "https://api.openai.com/v1/chat/completions";

    public String generateQuiz(String bookTitle, String description) {
        try {
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            headers.setBearerAuth(apiKey);

            JsonObject userMessage = new JsonObject();
            userMessage.addProperty("role", "user");
            userMessage.addProperty("content", createPrompt(bookTitle, description));

            JsonArray messages = new JsonArray();
            messages.add(userMessage);

            JsonObject requestBody = new JsonObject();
            requestBody.addProperty("model", model);
            requestBody.add("messages", messages);
            requestBody.addProperty("temperature", 0.7);

            HttpEntity<String> request = new HttpEntity<>(gson.toJson(requestBody), headers);
            String response = restTemplate.postForObject(OPENAI_URL, request, String.class);

            return parseResponse(response);
        } catch (Exception e) {
            log.error("OpenAI API call failed", e);
            throw new BusinessException(ErrorCode.INTERNAL_SERVER_ERROR);
        }
    }

    private String createPrompt(String title, String description) {
        return String.format(
                "책 제목: %s\n줄거리: %s\n\n" +
                        "위 책의 내용을 바탕으로 퀴즈 5문제를 만들어줘. " +
                        "JSON 형식으로 반환해줘. 형식은 다음과 같아:\n" +
                        "[\n" +
                        "  {\n" +
                        "    \"questionNo\": 1,\n" +
                        "    \"type\": \"MULTIPLE\",\n" +
                        "    \"question\": \"질문 내용\",\n" +
                        "    \"options\": [\"보기1\", \"보기2\", \"보기3\", \"보기4\", \"보기5\"],\n" +
                        "    \"answer\": \"정답 보기\"\n" +
                        "  },\n" +
                        "  {\n" +
                        "    \"questionNo\": 2,\n" +
                        "    \"type\": \"MULTIPLE\",\n" +
                        "    \"question\": \"질문 내용\",\n" +
                        "    \"options\": [\"보기1\", \"보기2\", \"보기3\", \"보기4\", \"보기5\"],\n" +
                        "    \"answer\": \"정답 보기\"\n" +
                        "  },\n" +
                        "  {\n" +
                        "    \"questionNo\": 3,\n" +
                        "    \"type\": \"OX\",\n" +
                        "    \"question\": \"질문 내용\",\n" +
                        "    \"options\": [\"O\", \"X\"],\n" +
                        "    \"answer\": \"O\"\n" +
                        "  },\n" +
                        "  {\n" +
                        "    \"questionNo\": 4,\n" +
                        "    \"type\": \"OX\",\n" +
                        "    \"question\": \"질문 내용\",\n" +
                        "    \"options\": [\"O\", \"X\"],\n" +
                        "    \"answer\": \"X\"\n" +
                        "  },\n" +
                        "  {\n" +
                        "    \"questionNo\": 5,\n" +
                        "    \"type\": \"SHORT\",\n" +
                        "    \"question\": \"질문 내용\",\n" +
                        "    \"options\": [],\n" +
                        "    \"answer\": \"단답형 정답\"\n" +
                        "  }\n" +
                        "]\n" +
                        "총 5문제이고, 다음 규칙을 반드시 지켜줘:\n" +
                        "1. 1번, 2번 문제는 객관식(MULTIPLE)이며, 보기는 반드시 5개여야 해.\n" +
                        "2. 3번, 4번 문제는 OX퀴즈(OX)여야 해.\n" +
                        "3. 5번 문제는 단답형(SHORT)이어야 해.\n" +
                        "한국어로 작성해줘.",
                title, description);
    }

    public String recommendBooks(String readBooks) {
        try {
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            headers.setBearerAuth(apiKey);

            JsonObject userMessage = new JsonObject();
            userMessage.addProperty("role", "user");
            userMessage.addProperty("content", createRecommendationPrompt(readBooks));

            JsonArray messages = new JsonArray();
            messages.add(userMessage);

            JsonObject requestBody = new JsonObject();
            requestBody.addProperty("model", model);
            requestBody.add("messages", messages);
            requestBody.addProperty("temperature", 0.7);

            HttpEntity<String> request = new HttpEntity<>(gson.toJson(requestBody), headers);
            String response = restTemplate.postForObject(OPENAI_URL, request, String.class);

            return parseResponse(response);
        } catch (Exception e) {
            log.error("OpenAI API call failed", e);
            throw new BusinessException(ErrorCode.INTERNAL_SERVER_ERROR);
        }
    }

    private String createRecommendationPrompt(String readBooks) {
        return String.format(
                "사용자가 읽은 책 목록이야: %s\n\n" +
                        "이 사용자가 좋아할 만한 책 3권을 추천해줘. " +
                        "JSON 형식으로 반환해줘. 형식은 다음과 같아:\n" +
                        "[\n" +
                        "  {\n" +
                        "    \"title\": \"책 제목\",\n" +
                        "    \"author\": \"저자\",\n" +
                        "    \"reason\": \"추천 이유\"\n" +
                        "  }\n" +
                        "]\n" +
                        "한국어로 작성해줘.",
                readBooks);
    }

    private String parseResponse(String jsonResponse) {
        JsonObject jsonObject = gson.fromJson(jsonResponse, JsonObject.class);
        return jsonObject.getAsJsonArray("choices")
                .get(0).getAsJsonObject()
                .getAsJsonObject("message")
                .get("content").getAsString();
    }

    public double detectAiGenerated(String content) {
        try {
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            headers.setBearerAuth(apiKey);

            JsonObject systemMessage = new JsonObject();
            systemMessage.addProperty("role", "system");
            systemMessage.addProperty("content",
                    "당신은 텍스트 분석 전문가입니다. 다음 글이 AI에 의해 작성되었을 확률(0.0~1.0)을 분석하세요. " +
                            "반드시 JSON 포맷으로만 응답하세요: {\"probability\": 0.00}. " +
                            "다른 설명 없이 JSON만 반환하세요.");

            JsonObject userMessage = new JsonObject();
            userMessage.addProperty("role", "user");
            userMessage.addProperty("content", content);

            JsonArray messages = new JsonArray();
            messages.add(systemMessage);
            messages.add(userMessage);

            JsonObject requestBody = new JsonObject();
            requestBody.addProperty("model", model);
            requestBody.add("messages", messages);
            requestBody.addProperty("temperature", 0.3); // 낮은 temperature로 일관성 확보

            HttpEntity<String> request = new HttpEntity<>(gson.toJson(requestBody), headers);
            String response = restTemplate.postForObject(OPENAI_URL, request, String.class);

            String content_response = parseResponse(response);

            // JSON 파싱하여 probability 값 추출
            JsonObject result = gson.fromJson(content_response, JsonObject.class);
            return result.get("probability").getAsDouble();
        } catch (Exception e) {
            log.warn("AI detection failed, defaulting to pass (0.0)", e);
            // Fallback: API 실패 시 통과 처리 (User Experience 우선)
            return 0.0;
        }
    }
}
