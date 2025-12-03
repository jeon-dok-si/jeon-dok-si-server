package com.jeondoksi.jeondoksi.domain.boss.repository;

import com.jeondoksi.jeondoksi.domain.boss.entity.Boss;
import com.jeondoksi.jeondoksi.domain.boss.entity.BossRaidAttempt;
import com.jeondoksi.jeondoksi.domain.guild.entity.Guild;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface BossRaidAttemptRepository extends JpaRepository<BossRaidAttempt, Long> {
    List<BossRaidAttempt> findByBoss(Boss boss);

    @Query("SELECT sum(bra.damage) FROM BossRaidAttempt bra WHERE bra.boss = :boss AND bra.guild = :guild")
    Long sumDamageByBossAndGuild(@Param("boss") Boss boss, @Param("guild") Guild guild);

    void deleteByGuildAndBoss(Guild guild, Boss boss);
}
