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
            CharacterRarity.COMMON, 10,
            CharacterRarity.RARE, 20,
            CharacterRarity.EPIC, 35,
            CharacterRarity.UNIQUE, 50);

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

    public BossResponse getBossDetail(Long bossId) {
        Boss boss = bossRepository.findById(bossId)
                .orElseThrow(() -> new IllegalArgumentException("Boss not found"));
        return new BossResponse(boss);
    }

    @Transactional
    public void attackBoss(User user, Report report) {
        // 1. Find active boss (Pick first one for now)
        List<Boss> activeBosses = bossRepository.findAllActive();
        if (activeBosses.isEmpty()) {
            return; // No active boss, nothing to attack
        }
        Boss boss = activeBosses.get(0);

        // 2. Find User's Guild
        Optional<GuildMember> guildMember = guildMemberRepository.findByUser(user);
        Guild guild = guildMember.map(GuildMember::getGuild).orElse(null);

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
        boss.takeDamage(damage);
    }

    private long calculateDamage(Character character) {
        int base = BASE_DAMAGE.getOrDefault(character.getRarity(), 10);
        double multiplier = 1 + (character.getLevel() * 0.1);
        return (long) (base * multiplier);
    }
}
