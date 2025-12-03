package com.jeondoksi.jeondoksi.domain.guild.dto;

import com.jeondoksi.jeondoksi.domain.guild.entity.GuildMember;
import com.jeondoksi.jeondoksi.domain.guild.entity.GuildRole;
import lombok.Getter;

@Getter
public class GuildMemberResponse {
    private Long userId;
    private String nickname;
    private GuildRole role;
    private String joinedAt;

    public GuildMemberResponse(GuildMember member) {
        this.userId = member.getUser().getUserId();
        this.nickname = member.getUser().getNickname();
        this.role = member.getRole();
        this.joinedAt = member.getCreatedAt().toString();
    }
}
