package com.jeondoksi.jeondoksi.domain.gamification.repository;

import com.jeondoksi.jeondoksi.domain.gamification.entity.Item;
import com.jeondoksi.jeondoksi.domain.gamification.entity.ItemRarity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface ItemRepository extends JpaRepository<Item, Long> {
    List<Item> findByRarity(ItemRarity rarity);

    @Query(value = "SELECT * FROM item WHERE rarity = :rarity ORDER BY RAND() LIMIT 1", nativeQuery = true)
    Item findRandomByRarity(String rarity);
}
