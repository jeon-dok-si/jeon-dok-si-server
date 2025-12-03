package com.jeondoksi.jeondoksi.domain.guild.service;

import com.jeondoksi.jeondoksi.domain.boss.repository.BossRaidAttemptRepository;
import com.jeondoksi.jeondoksi.domain.boss.repository.BossRepository;
import com.jeondoksi.jeondoksi.domain.guild.dto.CreateGuildRequest;
import com.jeondoksi.jeondoksi.domain.guild.dto.GuildMemberResponse;
import com.jeondoksi.jeondoksi.domain.guild.dto.GuildResponse;
import com.jeondoksi.jeondoksi.domain.guild.entity.Guild;
import com.jeondoksi.jeondoksi.domain.guild.entity.GuildMember;
import com.jeondoksi.jeondoksi.domain.guild.entity.GuildRole;
import com.jeondoksi.jeondoksi.domain.guild.repository.GuildMemberRepository;
import com.jeondoksi.jeondoksi.domain.guild.repository.GuildRepository;
import com.jeondoksi.jeondoksi.domain.user.entity.User;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class GuildService {

    private final GuildRepository guildRepository;
    private final GuildMemberRepository guildMemberRepository;
    private final BossRepository bossRepository;
    private final BossRaidAttemptRepository bossRaidAttemptRepository;

    @Transactional
    public GuildResponse createGuild(User user, CreateGuildRequest request) {
        // 1. Check if user is already in a guild
        if (guildMemberRepository.findByUser(user).isPresent()) {
            throw new IllegalStateException("User is already in a guild");
        }

        // 2. Check name uniqueness
        if (guildRepository.existsByName(request.getName())) {
            throw new IllegalArgumentException("Guild name already exists");
        }

        // 3. Create Guild
        String joinCode = null;
        if (request.isGenerateJoinCode()) {
            joinCode = UUID.randomUUID().toString().substring(0, 8).toUpperCase();
        }

        Guild guild = Guild.builder()
                .name(request.getName())
                .description(request.getDescription())
                .maxMembers(request.getMaxMembers())
                .isPrivate(request.isPrivate())
                .password(null) // Password removed
                .joinCode(joinCode)
                .leader(user)
                .build();

        guildRepository.save(guild);

        // 4. Add Leader as Member
        GuildMember member = GuildMember.builder()
                .guild(guild)
                .user(user)
                .role(GuildRole.LEADER)
                .build();

        guildMemberRepository.save(member);

        return new GuildResponse(guild, 1);
    }

    public Page<GuildResponse> getGuilds(String keyword, Pageable pageable) {
        Page<Guild> guilds;
        if (keyword != null && !keyword.isBlank()) {
            guilds = guildRepository.searchByName(keyword, pageable);
        } else {
            guilds = guildRepository.findAll(pageable);
        }
        return guilds.map(guild -> new GuildResponse(guild, guildMemberRepository.countByGuild(guild)));
    }

    public GuildResponse getGuildDetail(Long guildId) {
        Guild guild = guildRepository.findById(guildId)
                .orElseThrow(() -> new IllegalArgumentException("Guild not found"));
        long count = guildMemberRepository.countByGuild(guild);
        return new GuildResponse(guild, count);
    }

    @Transactional
    public void joinGuild(User user, Long guildId, String joinCode) {
        // 1. Check if user is already in a guild
        if (guildMemberRepository.findByUser(user).isPresent()) {
            throw new IllegalStateException("User is already in a guild");
        }

        Guild guild = guildRepository.findById(guildId)
                .orElseThrow(() -> new IllegalArgumentException("Guild not found"));

        // 2. Check Capacity
        long currentMembers = guildMemberRepository.countByGuild(guild);
        if (currentMembers >= guild.getMaxMembers()) {
            throw new IllegalStateException("Guild is full");
        }

        // 3. Check Join Code if private
        if (guild.isPrivate()) {
            if (joinCode == null || !joinCode.equals(guild.getJoinCode())) {
                throw new IllegalArgumentException("Invalid invite code");
            }
        }

        // 4. Join
        GuildMember member = GuildMember.builder()
                .guild(guild)
                .user(user)
                .role(GuildRole.MEMBER)
                .build();
        guildMemberRepository.save(member);
    }

    @Transactional
    public GuildResponse joinGuildByCode(User user, String joinCode) {
        if (guildMemberRepository.findByUser(user).isPresent()) {
            throw new IllegalStateException("User is already in a guild");
        }

        Guild guild = guildRepository.findByJoinCode(joinCode)
                .orElseThrow(() -> new IllegalArgumentException("Invalid join code"));

        long currentMembers = guildMemberRepository.countByGuild(guild);
        if (currentMembers >= guild.getMaxMembers()) {
            throw new IllegalStateException("Guild is full");
        }

        GuildMember member = GuildMember.builder()
                .guild(guild)
                .user(user)
                .role(GuildRole.MEMBER)
                .build();
        guildMemberRepository.save(member);

        return new GuildResponse(guild, currentMembers + 1);
    }

    @Transactional
    public void leaveGuild(User user) {
        GuildMember member = guildMemberRepository.findByUser(user)
                .orElseThrow(() -> new IllegalArgumentException("가입된 길드가 없습니다."));

        if (member.getRole() == GuildRole.LEADER) {
            long count = guildMemberRepository.countByGuild(member.getGuild());
            if (count > 1) {
                // 1. Find next leader (e.g., oldest member who is not the current leader)
                List<GuildMember> members = guildMemberRepository.findByGuild(member.getGuild());
                GuildMember nextLeader = members.stream()
                        .filter(m -> !m.getUser().getUserId().equals(user.getUserId()))
                        .findFirst() // Since it's sorted by ID usually, this picks the earliest joiner
                        .orElseThrow(() -> new IllegalStateException("No candidate for leadership found"));

                // 2. Promote next leader
                nextLeader.updateRole(GuildRole.LEADER);
                member.getGuild().changeLeader(nextLeader.getUser());

                // 3. Remove old leader
                guildMemberRepository.delete(member);
            } else {
                // Delete guild if leader is the only member
                bossRaidAttemptRepository.deleteByGuild(member.getGuild());
                guildMemberRepository.delete(member);
                guildRepository.delete(member.getGuild());
                return;
            }
        } else {
            guildMemberRepository.delete(member);
        }
    }

    @Transactional
    public void startRaid(Long guildId, User user) {
        Guild guild = guildRepository.findById(guildId)
                .orElseThrow(() -> new IllegalArgumentException("길드를 찾을 수 없습니다."));

        // Check if user is a member of the guild
        guildMemberRepository.findByUserAndGuild(user, guild)
                .orElseThrow(() -> new IllegalArgumentException("길드원이 아닙니다."));

        // Check if raid is already active (and boss is not dead)
        if (guild.getCurrentBoss() != null) {
            // Calculate current HP to see if it's dead
            long virtualMaxHp;
            if (guild.getCurrentRaidMaxHp() != null) {
                virtualMaxHp = guild.getCurrentRaidMaxHp();
            } else {
                // Fallback for old data
                long memberCount = guildMemberRepository.countByGuild(guild);
                virtualMaxHp = memberCount * 150000;
                if (virtualMaxHp == 0)
                    virtualMaxHp = 150000;
            }

            Long guildDamage = bossRaidAttemptRepository.sumDamageByBossAndGuild(guild.getCurrentBoss(), guild);
            if (guildDamage == null)
                guildDamage = 0L;

            long virtualCurrentHp = virtualMaxHp - guildDamage;

            if (virtualCurrentHp > 0) {
                throw new IllegalStateException("현재 진행 중인 레이드가 있습니다. 보스를 처치한 후 새로운 레이드를 시작하세요.");
            }
        }

        // 1. Pick Random Active Boss
        List<com.jeondoksi.jeondoksi.domain.boss.entity.Boss> activeBosses = bossRepository.findAllActive();
        if (activeBosses.isEmpty()) {
            throw new IllegalStateException("활성화된 보스가 없습니다.");
        }
        com.jeondoksi.jeondoksi.domain.boss.entity.Boss nextBoss = activeBosses
                .get((int) (Math.random() * activeBosses.size()));

        // 2. Calculate Fixed Max HP for this Raid
        long memberCount = guildMemberRepository.countByGuild(guild);
        long raidMaxHp = memberCount * 150000;
        if (raidMaxHp == 0)
            raidMaxHp = 150000;

        // 3. Set Current Boss and Fixed HP
        guild.setCurrentBoss(nextBoss, raidMaxHp);

        // 4. Reset History (Delete old attempts for this guild and this boss)
        bossRaidAttemptRepository.deleteByGuildAndBoss(guild, nextBoss);
    }

    public GuildResponse getMyGuild(User user) {
        return guildMemberRepository.findByUser(user)
                .map(member -> {
                    Guild guild = member.getGuild();
                    long count = guildMemberRepository.countByGuild(guild);
                    return new GuildResponse(guild, count);
                })
                .orElse(null);
    }

    public List<GuildMemberResponse> getGuildMembers(Long guildId) {
        Guild guild = guildRepository.findById(guildId)
                .orElseThrow(() -> new IllegalArgumentException("Guild not found"));

        return guildMemberRepository.findByGuild(guild).stream()
                .map(GuildMemberResponse::new)
                .collect(Collectors.toList());
    }
}
