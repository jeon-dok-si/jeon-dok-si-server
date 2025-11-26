package com.jeondoksi.jeondoksi.domain.gamification.service;

import com.jeondoksi.jeondoksi.domain.gamification.dto.GachaResponse;
import com.jeondoksi.jeondoksi.domain.gamification.entity.Inventory;
import com.jeondoksi.jeondoksi.domain.gamification.entity.Item;
import com.jeondoksi.jeondoksi.domain.gamification.entity.ItemCategory;
import com.jeondoksi.jeondoksi.domain.gamification.entity.ItemRarity;
import com.jeondoksi.jeondoksi.domain.gamification.repository.InventoryRepository;
import com.jeondoksi.jeondoksi.domain.gamification.repository.ItemRepository;
import com.jeondoksi.jeondoksi.domain.user.entity.User;
import com.jeondoksi.jeondoksi.domain.user.repository.UserRepository;
import com.jeondoksi.jeondoksi.global.error.BusinessException;
import com.jeondoksi.jeondoksi.global.error.ErrorCode;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class RewardServiceTest {

    @InjectMocks
    private RewardService rewardService;

    @Mock
    private ItemRepository itemRepository;

    @Mock
    private InventoryRepository inventoryRepository;

    @Mock
    private UserRepository userRepository;

    @Test
    @DisplayName("아이템 뽑기 성공")
    void drawItem_success() {
        // given
        Long userId = 1L;
        User user = User.builder().email("test@test.com").build();
        ReflectionTestUtils.setField(user, "currentXp", 200); // 충분한 XP

        Item item = Item.builder()
                .name("Test Item")
                .category(ItemCategory.HEAD)
                .rarity(ItemRarity.COMMON)
                .imageUrl("test.jpg")
                .build();

        given(userRepository.findById(userId)).willReturn(Optional.of(user));
        given(itemRepository.findRandomByRarity(anyString())).willReturn(item);

        // when
        GachaResponse response = rewardService.drawItem(userId);

        // then
        assertThat(response.getName()).isEqualTo("Test Item");
        assertThat(user.getCurrentXp()).isEqualTo(100); // 200 - 100
        verify(inventoryRepository).save(any(Inventory.class));
    }

    @Test
    @DisplayName("아이템 뽑기 실패 - XP 부족")
    void drawItem_notEnoughXp() {
        // given
        Long userId = 1L;
        User user = User.builder().email("test@test.com").build();
        ReflectionTestUtils.setField(user, "currentXp", 50); // 부족한 XP

        given(userRepository.findById(userId)).willReturn(Optional.of(user));

        // when & then
        assertThatThrownBy(() -> rewardService.drawItem(userId))
                .isInstanceOf(BusinessException.class)
                .hasFieldOrPropertyWithValue("errorCode", ErrorCode.NOT_ENOUGH_XP);
    }
}
