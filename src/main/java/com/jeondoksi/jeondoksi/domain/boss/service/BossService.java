package com.jeondoksi.jeondoksi.domain.boss.service;

import com.jeondoksi.jeondoksi.domain.boss.dto.BossResponse;
import com.jeondoksi.jeondoksi.domain.boss.dto.CreateBossRequest;
import com.jeondoksi.jeondoksi.domain.boss.entity.Boss;
import com.jeondoksi.jeondoksi.domain.boss.entity.BossRaidAttempt;
import com.jeondoksi.jeondoksi.domain.boss.repository.BossRaidAttemptRepository;
import com.jeondoksi.jeondoksi.domain.boss.repository.BossRepository;
import com.jeondoksi.jeondoksi.domain.gamification.entity.Character;
import com.jeondoksi.jeondoksi.domain.gamification.entity.CharacterRarity;
import com.jeondoksi.jeondoksi.domain.gamification.repository.CharacterRepository;
import com.jeondoksi.jeondoksi.domain.guild.entity.Guild;
import com.jeondoksi.jeondoksi.domain.guild.entity.GuildMember;
import com.jeondoksi.jeondoksi.domain.guild.repository.GuildMemberRepository;
import com.jeondoksi.jeondoksi.domain.report.entity.Report;
import com.jeondoksi.jeondoksi.domain.user.entity.User;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class BossService {

    private final BossRepository bossRepository;
    private final BossRaidAttemptRepository bossRaidAttemptRepository;
    private final CharacterRepository characterRepository;
    private final GuildMemberRepository guildMemberRepository;

    private static final Map<CharacterRarity, Integer> BASE_DAMAGE = Map.of(
            CharacterRarity.COMMON, 10000,
            CharacterRarity.RARE, 15000,
            CharacterRarity.EPIC, 22000,
            CharacterRarity.UNIQUE, 30000);

    @Transactional
    public BossResponse createBoss(CreateBossRequest request) {
        Boss boss = Boss.builder()
                .name(request.getName())
                .description(request.getDescription())
                .level(request.getLevel())
                .maxHp(request.getMaxHp())
                .imageUrl(request.getImageUrl())
                .startAt(request.getStartAt())
                .endAt(request.getEndAt())
                .build();
        bossRepository.save(boss);
        return new BossResponse(boss);
    }

    public List<BossResponse> getActiveBosses() {
        return bossRepository.findAllActive().stream()
                .map(BossResponse::new)
                .collect(Collectors.toList());
    }

    public BossResponse getBossDetail(Long bossId, User user) {
        // 1. Get User's Guild
        Guild guild = guildMemberRepository.findByUser(user)
                .map(GuildMember::getGuild)
                .orElse(null);

        Boss boss = bossRepository.findById(bossId)
                .orElseThrow(() -> new IllegalArgumentException("Boss not found"));

        if (guild == null) {
            return new BossResponse(boss);
        }

        // If the requested boss is NOT the guild's current boss, just return basic info
        // or maybe we should enforce that they can only see their current boss?
        // For now, let's calculate virtual HP only if it matches current boss.
        Boss currentBoss = guild.getCurrentBoss();
        if (currentBoss == null || !currentBoss.getId().equals(bossId)) {
            // If guild has no boss or looking at different boss, return basic info
            // But wait, if they are looking at a "Defeated" boss history, maybe we show it?
            // For simplicity, let's just return basic info if it's not the current active
            // raid.
            return new BossResponse(boss);
        }

        // 2. Calculate Virtual Max HP for Guild
        long memberCount = guildMemberRepository.countByGuild(guild);
        long virtualMaxHp = memberCount * 150000;
        if (virtualMaxHp == 0)
            virtualMaxHp = 150000; // Fallback

        // 3. Calculate Guild's Total Damage
        Long guildDamage = bossRaidAttemptRepository.sumDamageByBossAndGuild(boss, guild);
        if (guildDamage == null)
            guildDamage = 0L;

        // 4. Calculate Virtual Current HP
        long virtualCurrentHp = virtualMaxHp - guildDamage;
        if (virtualCurrentHp < 0)
            virtualCurrentHp = 0;

        // 5. Return Response with Virtual Stats
        BossResponse response = new BossResponse(boss);
        response.setMaxHp(virtualMaxHp);
        response.setCurrentHp(virtualCurrentHp);

        // If virtual HP is 0, mark as inactive (defeated) for this guild
        if (virtualCurrentHp <= 0) {
            response.setActive(false);
        }

        return response;
    }

    @Transactional
    public void attackBoss(User user, Report report) {
        // 1. Find User's Guild
        Optional<GuildMember> guildMember = guildMemberRepository.findByUser(user);
        Guild guild = guildMember.map(GuildMember::getGuild).orElse(null);

        if (guild == null) {
            return; // User not in guild, cannot attack
        }

        // 2. Find Guild's Current Boss
        Boss boss = guild.getCurrentBoss();
        if (boss == null) {
            return; // No active raid for this guild
        }

        // 3. Find Equipped Character
        Character character = characterRepository.findByUserAndIsEquippedTrue(user)
                .orElseThrow(() -> new IllegalStateException("No equipped character found"));

        // 4. Calculate Damage
        long damage = calculateDamage(character);

        // 5. Create Attempt
        BossRaidAttempt attempt = BossRaidAttempt.builder()
                .boss(boss)
                .guild(guild)
                .user(user)
                .character(character)
                .report(report)
                .damage(damage)
                .build();
        bossRaidAttemptRepository.save(attempt);

        // 6. Apply Damage to Boss
        // boss.takeDamage(damage); // REMOVED: Global boss should not take damage in
        // this virtual system.
    }

    private long calculateDamage(Character character) {
        int base = BASE_DAMAGE.getOrDefault(character.getRarity(), 10);
        double multiplier = 1 + (character.getLevel() * 0.1);
        return (long) (base * multiplier);
    }
}
