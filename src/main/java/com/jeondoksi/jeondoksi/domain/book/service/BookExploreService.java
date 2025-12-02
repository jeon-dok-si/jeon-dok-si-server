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

    public List<AladinBookDto> getBestsellers(Integer categoryId, int page) {
        try {
            // Aladin API 'Start' parameter seems to be 1-based index, not page number,
            // causing duplicates if we just pass page.
            // Formula: (page - 1) * MaxResults + 1
            int maxResults = 20;
            int start = (page - 1) * maxResults + 1;

            UriComponentsBuilder builder = UriComponentsBuilder.fromUriString(ALADIN_API_URL)
                    .queryParam("ttbkey", aladinKey)
                    .queryParam("QueryType", "Bestseller")
                    .queryParam("MaxResults", maxResults)
                    .queryParam("start", start) // Use calculated start index
                    .queryParam("SearchTarget", "Book")
                    .queryParam("output", "js")
                    .queryParam("Version", "20131101")
                    .queryParam("Cover", "Big");

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
                    String originalCover = item.path("cover").asText();
                    // 1. Ensure HTTPS
                    String coverUrl = originalCover.replace("http://", "https://");
                    // 2. Try to use higher resolution image (cover500) if it's a standard cover URL
                    // Example: .../cover/k123456789_1.jpg -> .../cover500/k123456789_1.jpg
                    if (coverUrl.contains("/cover/")) {
                        coverUrl = coverUrl.replace("/cover/", "/cover500/");
                    }
                    // Use high-res cover if available (Aladin sometimes provides 'cover500' or
                    // similar, but standard 'cover' is often small)
                    // Let's try to modify the URL to get a larger image if possible, or just ensure
                    // HTTPS.
                    // Aladin cover URLs:
                    // https://image.aladin.co.kr/product/35052/66/cover/k562936647_1.jpg
                    // Often replacing 'cover' with 'cover500' works for higher res, but let's stick
                    // to HTTPS fix first.

                    books.add(AladinBookDto.builder()
                            .title(item.path("title").asText())
                            .author(item.path("author").asText())
                            .cover(coverUrl)
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
