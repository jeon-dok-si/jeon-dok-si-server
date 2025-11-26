package com.jeondoksi.jeondoksi.domain.gamification.repository;

import com.jeondoksi.jeondoksi.domain.gamification.entity.Inventory;
import com.jeondoksi.jeondoksi.domain.gamification.entity.ItemCategory;
import com.jeondoksi.jeondoksi.domain.user.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.Optional;

public interface InventoryRepository extends JpaRepository<Inventory, Long> {
    List<Inventory> findAllByUser(User user);

    Optional<Inventory> findByUserAndItem_ItemId(User user, Long itemId);

    @Query("SELECT i FROM Inventory i WHERE i.user = :user AND i.isEquipped = true AND i.item.category = :category")
    Optional<Inventory> findEquippedItemByCategory(User user, ItemCategory category);
}
