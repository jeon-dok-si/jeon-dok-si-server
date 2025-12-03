package com.jeondoksi.jeondoksi.domain.guild.entity;

import com.jeondoksi.jeondoksi.domain.common.BaseTimeEntity;
import com.jeondoksi.jeondoksi.domain.user.entity.User;
import jakarta.persistence.*;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Getter
@NoArgsConstructor
@Table(name = "guild_member")
public class GuildMember extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "guild_member_id")
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "guild_id", nullable = false)
    private Guild guild;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private GuildRole role;

    @Builder
    public GuildMember(Guild guild, User user, GuildRole role) {
        this.guild = guild;
        this.user = user;
        this.role = role;
    }

    public void updateRole(GuildRole role) {
        this.role = role;
    }
}
