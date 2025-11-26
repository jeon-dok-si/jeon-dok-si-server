package com.jeondoksi.jeondoksi.domain.gamification.dto;

import com.jeondoksi.jeondoksi.domain.gamification.entity.Item;
import com.jeondoksi.jeondoksi.domain.gamification.entity.ItemCategory;
import com.jeondoksi.jeondoksi.domain.gamification.entity.ItemRarity;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class GachaResponse {
    private Long itemId;
    private String name;
    private ItemCategory category;
    private ItemRarity rarity;
    private String imageUrl;

    public static GachaResponse from(Item item) {
        return GachaResponse.builder()
                .itemId(item.getItemId())
                .name(item.getName())
                .category(item.getCategory())
                .rarity(item.getRarity())
                .imageUrl(item.getImageUrl())
                .build();
    }
}
