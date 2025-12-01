package com.jeondoksi.jeondoksi.domain.gamification.repository;

import com.jeondoksi.jeondoksi.domain.gamification.entity.CharacterInfo;
import com.jeondoksi.jeondoksi.domain.gamification.entity.CharacterRarity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface CharacterInfoRepository extends JpaRepository<CharacterInfo, Long> {
    List<CharacterInfo> findByRarity(CharacterRarity rarity);

    CharacterInfo findByName(String name);
}
