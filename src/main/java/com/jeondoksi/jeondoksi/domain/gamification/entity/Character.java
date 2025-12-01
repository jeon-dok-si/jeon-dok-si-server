package com.jeondoksi.jeondoksi.domain.gamification.entity;

import com.jeondoksi.jeondoksi.domain.common.BaseTimeEntity;
import com.jeondoksi.jeondoksi.domain.user.entity.User;
import jakarta.persistence.*;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.ColumnDefault;

@Entity
@Getter
@NoArgsConstructor
@Table(name = "characters")
public class Character extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "character_id")
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(nullable = false)
    private String name;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private CharacterRarity rarity;

    @Column(nullable = false)
    @ColumnDefault("1")
    private int level = 1;

    @Column(name = "current_xp", nullable = false)
    @ColumnDefault("0")
    private int currentXp = 0;

    @Column(name = "image_url", nullable = false)
    private String imageUrl;

    @Column(name = "is_equipped", nullable = false)
    @ColumnDefault("false")
    private boolean isEquipped = false;

    public void equip() {
        this.isEquipped = true;
    }

    public void unequip() {
        this.isEquipped = false;
    }

    @Builder
    public Character(User user, String name, CharacterRarity rarity, String imageUrl) {
        this.user = user;
        this.name = name;
        this.rarity = rarity;
        this.imageUrl = imageUrl;
        this.level = 1;
        this.currentXp = 0;
    }

    public void gainExp(int amount) {
        this.currentXp += amount;
        checkLevelUp();
    }

    private void checkLevelUp() {
        int requiredXp = getRequiredXp();
        while (this.currentXp >= requiredXp) {
            this.currentXp -= requiredXp;
            this.level++;
            checkEvolution();
            requiredXp = getRequiredXp();
        }
    }

    private int getRequiredXp() {
        return this.level * 100;
    }

    private void checkEvolution() {
        // Evolution logic can be expanded here
        // For now, maybe change name or image based on level if needed
        // Or just trigger an event
    }
}
