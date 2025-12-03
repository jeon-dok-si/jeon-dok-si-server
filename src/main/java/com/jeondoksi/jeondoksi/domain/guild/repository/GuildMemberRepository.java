package com.jeondoksi.jeondoksi.domain.guild.repository;

import com.jeondoksi.jeondoksi.domain.guild.entity.Guild;
import com.jeondoksi.jeondoksi.domain.guild.entity.GuildMember;
import com.jeondoksi.jeondoksi.domain.user.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface GuildMemberRepository extends JpaRepository<GuildMember, Long> {
    long countByGuild(Guild guild);

    Optional<GuildMember> findByUser(User user);

    boolean existsByGuildAndUser(Guild guild, User user);

    Optional<GuildMember> findByUserAndGuild(User user, Guild guild);

    List<GuildMember> findByGuild(Guild guild);
}
