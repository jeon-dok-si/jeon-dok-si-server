package com.jeondoksi.jeondoksi.domain.gamification.dto;

import com.jeondoksi.jeondoksi.domain.gamification.entity.Inventory;
import com.jeondoksi.jeondoksi.domain.gamification.entity.ItemCategory;
import com.jeondoksi.jeondoksi.domain.gamification.entity.ItemRarity;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class InventoryResponse {
    private Long invenId;
    private Long itemId;
    private String name;
    private ItemCategory category;
    private ItemRarity rarity;
    private String imageUrl;
    private boolean isEquipped;

    public static InventoryResponse from(Inventory inventory) {
        return InventoryResponse.builder()
                .invenId(inventory.getInvenId())
                .itemId(inventory.getItem().getItemId())
                .name(inventory.getItem().getName())
                .category(inventory.getItem().getCategory())
                .rarity(inventory.getItem().getRarity())
                .imageUrl(inventory.getItem().getImageUrl())
                .isEquipped(inventory.isEquipped())
                .build();
    }
}
