package com.jeondoksi.jeondoksi.domain.gamification.service;

import com.jeondoksi.jeondoksi.domain.gamification.entity.Character;
import com.jeondoksi.jeondoksi.domain.gamification.entity.CharacterInfo;
import com.jeondoksi.jeondoksi.domain.gamification.entity.CharacterRarity;
import com.jeondoksi.jeondoksi.domain.gamification.repository.CharacterInfoRepository;
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
    private final CharacterInfoRepository characterInfoRepository;
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

        List<CharacterInfo> availableCharacters = characterInfoRepository.findByRarity(rarity);

        // If no characters of this rarity exist, fallback to COMMON or throw error
        // For robustness, if list is empty, try finding ANY character or use default
        if (availableCharacters.isEmpty()) {
            // Fallback to Common if specific rarity not found
            if (rarity != CharacterRarity.COMMON) {
                availableCharacters = characterInfoRepository.findByRarity(CharacterRarity.COMMON);
            }

            // If still empty (DB is empty), create a dummy
            if (availableCharacters.isEmpty()) {
                Character character = Character.builder()
                        .user(user)
                        .name("Unknown " + rarity)
                        .rarity(rarity)
                        .imageUrl(
                                "https://jeondoksi-files-20251127.s3.ap-southeast-2.amazonaws.com/basic_character.png")
                        .build();
                return characterRepository.save(character);
            }
        }

        CharacterInfo selectedInfo = availableCharacters.get(random.nextInt(availableCharacters.size()));

        Character character = Character.builder()
                .user(user)
                .name(selectedInfo.getName())
                .rarity(selectedInfo.getRarity())
                .imageUrl(selectedInfo.getImageUrl())
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
