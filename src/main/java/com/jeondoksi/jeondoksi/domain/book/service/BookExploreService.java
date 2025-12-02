package com.jeondoksi.jeondoksi.domain.book.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.jeondoksi.jeondoksi.domain.book.dto.AladinBookDto;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class BookExploreService {

    @Value("${api.aladin.key}")
    private String aladinKey;

    private final RestTemplate restTemplate = new RestTemplate();
    private final ObjectMapper objectMapper = new ObjectMapper();

    private static final String ALADIN_API_URL = "http://www.aladin.co.kr/ttb/api/ItemList.aspx";

    public List<AladinBookDto> getBestsellers(Integer categoryId) {
        try {
            UriComponentsBuilder builder = UriComponentsBuilder.fromUriString(ALADIN_API_URL)
                    .queryParam("ttbkey", aladinKey)
                    .queryParam("QueryType", "Bestseller")
                    .queryParam("MaxResults", 10)
                    .queryParam("start", 1)
                    .queryParam("SearchTarget", "Book")
                    .queryParam("output", "js")
                    .queryParam("Version", "20131101");

            if (categoryId != null) {
                builder.queryParam("CategoryId", categoryId);
            }

            String url = builder.toUriString();
            log.info("Calling Aladin API: {}", url);

            String response = restTemplate.getForObject(url, String.class);
            JsonNode root = objectMapper.readTree(response);
            JsonNode items = root.path("item");

            List<AladinBookDto> books = new ArrayList<>();
            if (items.isArray()) {
                for (JsonNode item : items) {
                    books.add(AladinBookDto.builder()
                            .title(item.path("title").asText())
                            .author(item.path("author").asText())
                            .cover(item.path("cover").asText())
                            .link(item.path("link").asText())
                            .isbn(item.path("isbn13").asText())
                            .description(item.path("description").asText())
                            .pubDate(item.path("pubDate").asText())
                            .categoryName(item.path("categoryName").asText())
                            .bestRank(item.path("bestRank").asInt())
                            .build());
                }
            }
            return books;

        } catch (Exception e) {
            log.error("Failed to fetch bestsellers from Aladin", e);
            return new ArrayList<>();
        }
    }
}
