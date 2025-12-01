package com.jeondoksi.jeondoksi.domain.gamification.service;

import com.jeondoksi.jeondoksi.domain.gamification.entity.Character;
import com.jeondoksi.jeondoksi.domain.gamification.entity.CharacterRarity;
import com.jeondoksi.jeondoksi.domain.gamification.repository.CharacterRepository;
import com.jeondoksi.jeondoksi.domain.user.entity.User;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Random;

@Service
@RequiredArgsConstructor
@Transactional
public class CharacterService {

    private final CharacterRepository characterRepository;
    private final Random random = new Random();

    public Character drawCharacter(User user) {
        // Simple Gacha Logic
        // Common: 50%, Rare: 30%, Epic: 15%, Unique: 5%
        int chance = random.nextInt(100);
        CharacterRarity rarity;
        if (chance < 50) {
            rarity = CharacterRarity.COMMON;
        } else if (chance < 80) {
            rarity = CharacterRarity.RARE;
        } else if (chance < 95) {
            rarity = CharacterRarity.EPIC;
        } else {
            rarity = CharacterRarity.UNIQUE;
        }

        // In a real app, we would fetch a random template from DB or config based on
        // rarity
        // For now, we generate a placeholder
        String name = rarity.name() + " Character";
        String imageUrl = "https://example.com/default.png"; // Placeholder

        Character character = Character.builder()
                .user(user)
                .name(name)
                .rarity(rarity)
                .imageUrl(imageUrl)
                .build();

        return characterRepository.save(character);
    }

    public void gainExp(Long characterId, int amount) {
        Character character = characterRepository.findById(characterId)
                .orElseThrow(() -> new IllegalArgumentException("Character not found"));
        character.gainExp(amount);
    }

    @Transactional(readOnly = true)
    public List<Character> getMyCharacters(User user) {
        return characterRepository.findAllByUser(user);
    }
}
