package com.jeondoksi.jeondoksi.domain.guild.entity;

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
@Table(name = "guild")
public class Guild extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "guild_id")
    private Long id;

    @Column(nullable = false, unique = true, length = 50)
    private String name;

    @Column(length = 255)
    private String description;

    @Column(name = "max_members", nullable = false)
    @ColumnDefault("30")
    private int maxMembers;

    @Column(name = "is_private", nullable = false)
    @ColumnDefault("false")
    private boolean isPrivate;

    @Column(length = 255)
    private String password; // Nullable, hashed

    @Column(name = "join_code", length = 20)
    private String joinCode;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "leader_id", nullable = false)
    private User leader;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "current_boss_id")
    private com.jeondoksi.jeondoksi.domain.boss.entity.Boss currentBoss;

    @Column(name = "current_raid_max_hp")
    private Long currentRaidMaxHp;

    public void setCurrentBoss(com.jeondoksi.jeondoksi.domain.boss.entity.Boss boss, Long maxHp) {
        this.currentBoss = boss;
        this.currentRaidMaxHp = maxHp;
    }

    @Builder
    public Guild(String name, String description, int maxMembers, boolean isPrivate, String password, String joinCode,
            User leader) {
        this.name = name;
        this.description = description;
        this.maxMembers = maxMembers;
        this.isPrivate = isPrivate;
        this.password = password;
        this.joinCode = joinCode;
        this.leader = leader;
    }

    public void updateInfo(String description, boolean isPrivate, String password) {
        this.description = description;
        this.isPrivate = isPrivate;
        this.password = password;
    }

    public void changeLeader(User newLeader) {
        this.leader = newLeader;
    }
}
