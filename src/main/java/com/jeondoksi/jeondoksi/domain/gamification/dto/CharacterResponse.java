package com.jeondoksi.jeondoksi.domain.gamification.dto;

import com.jeondoksi.jeondoksi.domain.gamification.entity.Character;
import com.jeondoksi.jeondoksi.domain.gamification.entity.CharacterRarity;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class CharacterResponse {
    private Long characterId;
    private String name;
    private CharacterRarity rarity;
    private int level;
    private int currentXp;
    private int requiredXp;
    private String imageUrl;

    public static CharacterResponse from(Character character) {
        return CharacterResponse.builder()
                .characterId(character.getId())
                .name(character.getName())
                .rarity(character.getRarity())
                .level(character.getLevel())
                .currentXp(character.getCurrentXp())
                .requiredXp(character.getLevel() * 100)
                .imageUrl(character.getImageUrl())
                .build();
    }
}
