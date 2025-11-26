package com.jeondoksi.jeondoksi.domain.book.client;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.jeondoksi.jeondoksi.domain.book.dto.BookSearchResponse;
import com.jeondoksi.jeondoksi.global.error.BusinessException;
import com.jeondoksi.jeondoksi.global.error.ErrorCode;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import java.net.URI;
import java.util.ArrayList;
import java.util.List;

@Slf4j
@Component
@RequiredArgsConstructor
public class NaverBookClient {

    @Value("${api.naver.client-id}")
    private String clientId;

    @Value("${api.naver.client-secret}")
    private String clientSecret;

    private final RestTemplate restTemplate = new RestTemplate();
    private final ObjectMapper objectMapper = new ObjectMapper();

    private static final String NAVER_API_URL = "https://openapi.naver.com/v1/search/book.json";

    public List<BookSearchResponse> searchBooks(String query) {
        URI uri = UriComponentsBuilder.fromUriString(NAVER_API_URL)
                .queryParam("query", query)
                .queryParam("display", 10)
                .queryParam("start", 1)
                .build()
                .encode()
                .toUri();

        HttpHeaders headers = new HttpHeaders();
        headers.set("X-Naver-Client-Id", clientId);
        headers.set("X-Naver-Client-Secret", clientSecret);

        HttpEntity<String> entity = new HttpEntity<>(headers);

        try {
            ResponseEntity<String> response = restTemplate.exchange(uri, HttpMethod.GET, entity, String.class);
            JsonNode root = objectMapper.readTree(response.getBody());
            JsonNode items = root.path("items");

            List<BookSearchResponse> books = new ArrayList<>();
            if (items.isArray()) {
                for (JsonNode item : items) {
                    books.add(BookSearchResponse.builder()
                            .isbn(item.path("isbn").asText())
                            .title(item.path("title").asText())
                            .author(item.path("author").asText())
                            .thumbnail(item.path("image").asText())
                            .description(item.path("description").asText())
                            .build());
                }
            }
            return books;
        } catch (Exception e) {
            log.error("Naver API Error", e);
            throw new BusinessException(ErrorCode.INTERNAL_SERVER_ERROR);
        }
    }
}
