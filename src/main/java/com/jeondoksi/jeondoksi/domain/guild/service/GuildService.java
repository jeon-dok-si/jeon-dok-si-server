package com.jeondoksi.jeondoksi.domain.guild.service;

import com.jeondoksi.jeondoksi.domain.guild.dto.CreateGuildRequest;
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
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class GuildService {

    private final GuildRepository guildRepository;
    private final GuildMemberRepository guildMemberRepository;
    private final PasswordEncoder passwordEncoder;

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
        String encodedPassword = null;
        if (request.isPrivate() && request.getPassword() != null && !request.getPassword().isBlank()) {
            encodedPassword = passwordEncoder.encode(request.getPassword());
        }

        String joinCode = null;
        if (request.isGenerateJoinCode()) {
            joinCode = UUID.randomUUID().toString().substring(0, 8).toUpperCase();
        }

        Guild guild = Guild.builder()
                .name(request.getName())
                .description(request.getDescription())
                .maxMembers(request.getMaxMembers())
                .isPrivate(request.isPrivate())
                .password(encodedPassword)
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
    public void joinGuild(User user, Long guildId, String password) {
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

        // 3. Check Password if private
        if (guild.isPrivate()) {
            if (guild.getPassword() != null && !guild.getPassword().isEmpty()) {
                if (password == null || !passwordEncoder.matches(password, guild.getPassword())) {
                    throw new IllegalArgumentException("Invalid password");
                }
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
    public void joinGuildByCode(User user, String joinCode) {
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
    }

    @Transactional
    public void leaveGuild(User user, Long guildId) {
        GuildMember member = guildMemberRepository.findByUser(user)
                .orElseThrow(() -> new IllegalArgumentException("User is not in any guild"));

        if (!member.getGuild().getId().equals(guildId)) {
            throw new IllegalArgumentException("User is not a member of this guild");
        }

        if (member.getRole() == GuildRole.LEADER) {
            // Leader leaving logic:
            // For now, if leader leaves, we can either:
            // 1. Disband guild if no one else
            // 2. Prevent leaving if members exist
            // 3. Auto-assign new leader
            // Prompt says: "Define policy... e.g. if members remain, transfer leader, else
            // delete"

            long count = guildMemberRepository.countByGuild(member.getGuild());
            if (count > 1) {
                throw new IllegalStateException(
                        "Leader cannot leave while other members exist. Transfer leadership first.");
            } else {
                // Delete guild
                guildMemberRepository.delete(member);
                guildRepository.delete(member.getGuild());
                return;
            }
        }

        guildMemberRepository.delete(member);
    }
}
