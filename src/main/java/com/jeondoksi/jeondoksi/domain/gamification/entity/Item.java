package com.jeondoksi.jeondoksi.domain.gamification.entity;

import com.jeondoksi.jeondoksi.domain.common.BaseTimeEntity;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Getter
@NoArgsConstructor
@Table(name = "item")
public class Item extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "item_id")
    private Long itemId;

    @Column(nullable = false, length = 50)
    private String name;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private ItemCategory category;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private ItemRarity rarity;

    @Column(name = "image_url", nullable = false, length = 500)
    private String imageUrl;

    @Builder
    public Item(String name, ItemCategory category, ItemRarity rarity, String imageUrl) {
        this.name = name;
        this.category = category;
        this.rarity = rarity;
        this.imageUrl = imageUrl;
    }
}