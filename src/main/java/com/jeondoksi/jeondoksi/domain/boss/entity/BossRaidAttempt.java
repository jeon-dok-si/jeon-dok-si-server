package com.jeondoksi.jeondoksi.domain.boss.entity;

import com.jeondoksi.jeondoksi.domain.common.BaseTimeEntity;
import com.jeondoksi.jeondoksi.domain.gamification.entity.Character;
import com.jeondoksi.jeondoksi.domain.guild.entity.Guild;
import com.jeondoksi.jeondoksi.domain.report.entity.Report;
import com.jeondoksi.jeondoksi.domain.user.entity.User;
import jakarta.persistence.*;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Getter
@NoArgsConstructor
@Table(name = "boss_raid_attempt")
public class BossRaidAttempt extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "attempt_id")
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "boss_id", nullable = false)
    private Boss boss;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "guild_id")
    private Guild guild;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "character_id", nullable = false)
    private Character character;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "report_id", nullable = false)
    private Report report;

    @Column(nullable = false)
    private long damage;

    @Builder
    public BossRaidAttempt(Boss boss, Guild guild, User user, Character character, Report report, long damage) {
        this.boss = boss;
        this.guild = guild;
        this.user = user;
        this.character = character;
        this.report = report;
        this.damage = damage;
    }
}
