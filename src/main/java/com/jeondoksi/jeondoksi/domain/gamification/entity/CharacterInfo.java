package com.jeondoksi.jeondoksi.domain.gamification.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Getter
@NoArgsConstructor
@Table(name = "character_info")
public class CharacterInfo {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "info_id")
    private Long id;

    @Column(nullable = false)
    private String name;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private CharacterRarity rarity;

    @Column(name = "image_url", nullable = false)
    private String imageUrl;

    @Column(length = 500)
    private String description;

    public CharacterInfo(String name, CharacterRarity rarity, String imageUrl, String description) {
        this.name = name;
        this.rarity = rarity;
        this.imageUrl = imageUrl;
        this.description = description;
    }
}
