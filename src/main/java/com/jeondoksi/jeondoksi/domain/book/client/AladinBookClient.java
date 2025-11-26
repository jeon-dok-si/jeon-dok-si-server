package com.jeondoksi.jeondoksi.domain.book.client;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.jeondoksi.jeondoksi.domain.book.dto.BookSearchResponse;
import com.jeondoksi.jeondoksi.global.error.BusinessException;
import com.jeondoksi.jeondoksi.global.error.ErrorCode;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import java.net.URI;
import java.util.ArrayList;
import java.util.List;

@Slf4j
@Component
@RequiredArgsConstructor
public class AladinBookClient {

    @Value("${api.aladin.key}")
    private String ttbKey;

    private final RestTemplate restTemplate = new RestTemplate();
    private final ObjectMapper objectMapper = new ObjectMapper();

    private static final String ALADIN_API_URL = "http://www.aladin.co.kr/ttb/api/ItemSearch.aspx";

    public List<BookSearchResponse> searchBooks(String query) {
        URI uri = UriComponentsBuilder.fromUriString(ALADIN_API_URL)
                .queryParam("ttbkey", ttbKey)
                .queryParam("Query", query)
                .queryParam("QueryType", "Title")
                .queryParam("MaxResults", 10)
                .queryParam("start", 1)
                .queryParam("SearchTarget", "Book")
                .queryParam("output", "js") // JSON format
                .queryParam("Version", "20131101")
                .build()
                .encode()
                .toUri();

        try {
            String response = restTemplate.getForObject(uri, String.class);
            JsonNode root = objectMapper.readTree(response);
            JsonNode items = root.path("item");

            List<BookSearchResponse> books = new ArrayList<>();
            if (items.isArray()) {
                for (JsonNode item : items) {
                    books.add(BookSearchResponse.builder()
                            .isbn(item.path("isbn13").asText())
                            .title(item.path("title").asText())
                            .author(item.path("author").asText())
                            .thumbnail(item.path("cover").asText())
                            .description(item.path("description").asText())
                            .build());
                }
            }
            return books;
        } catch (Exception e) {
            log.error("Aladin API Error", e);
            throw new BusinessException(ErrorCode.INTERNAL_SERVER_ERROR);
        }
    }
}
