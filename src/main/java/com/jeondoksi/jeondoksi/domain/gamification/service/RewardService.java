package com.jeondoksi.jeondoksi.domain.gamification.service;

import com.jeondoksi.jeondoksi.domain.gamification.dto.GachaResponse;
import com.jeondoksi.jeondoksi.domain.gamification.dto.InventoryResponse;
import com.jeondoksi.jeondoksi.domain.gamification.entity.Inventory;
import com.jeondoksi.jeondoksi.domain.gamification.entity.Item;
import com.jeondoksi.jeondoksi.domain.gamification.entity.ItemRarity;
import com.jeondoksi.jeondoksi.domain.gamification.repository.InventoryRepository;
import com.jeondoksi.jeondoksi.domain.gamification.repository.ItemRepository;
import com.jeondoksi.jeondoksi.domain.user.entity.User;
import com.jeondoksi.jeondoksi.domain.user.repository.UserRepository;
import com.jeondoksi.jeondoksi.global.error.BusinessException;
import com.jeondoksi.jeondoksi.global.error.ErrorCode;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class RewardService {

    private final ItemRepository itemRepository;
    private final InventoryRepository inventoryRepository;
    private final UserRepository userRepository;

    private static final int GACHA_COST = 100; // 뽑기 비용 (XP)

    @Transactional
    public GachaResponse drawItem(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new BusinessException(ErrorCode.USER_NOT_FOUND));

        if (user.getCurrentXp() < GACHA_COST) {
            throw new BusinessException(ErrorCode.NOT_ENOUGH_XP);
        }

        user.useExp(GACHA_COST);

        ItemRarity rarity = determineRarity();
        Item item = itemRepository.findRandomByRarity(rarity.name());

        // 아이템이 없으면 기본 아이템(COMMON) 지급 (예외 처리)
        if (item == null) {
            item = itemRepository.findRandomByRarity(ItemRarity.COMMON.name());
        }

        // 인벤토리에 추가 (중복 허용 여부는 기획에 따라 다르지만 여기선 중복 허용)
        Inventory inventory = Inventory.builder()
                .user(user)
                .item(item)
                .build();
        inventoryRepository.save(inventory);

        return GachaResponse.from(item);
    }

    public List<InventoryResponse> getMyInventory(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new BusinessException(ErrorCode.USER_NOT_FOUND));

        return inventoryRepository.findAllByUser(user).stream()
                .map(InventoryResponse::from)
                .collect(Collectors.toList());
    }

    @Transactional
    public void equipItem(Long userId, Long invenId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new BusinessException(ErrorCode.USER_NOT_FOUND));

        Inventory inventory = inventoryRepository.findById(invenId)
                .orElseThrow(() -> new BusinessException(ErrorCode.ITEM_NOT_FOUND));

        if (!inventory.getUser().getUserId().equals(userId)) {
            throw new BusinessException(ErrorCode.FORBIDDEN);
        }

        // 같은 카테고리의 기존 장착 아이템 해제
        inventoryRepository.findEquippedItemByCategory(user, inventory.getItem().getCategory())
                .ifPresent(Inventory::unequip);

        inventory.equip();
    }

    private ItemRarity determineRarity() {
        double random = Math.random();
        if (random < 0.6) {
            return ItemRarity.COMMON;
        } else if (random < 0.9) {
            return ItemRarity.RARE;
        } else {
            return ItemRarity.EPIC;
        }
    }
}
