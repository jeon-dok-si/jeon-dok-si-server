package com.jeondoksi.jeondoksi.domain.guild.dto;

import com.jeondoksi.jeondoksi.domain.guild.entity.Guild;
import lombok.Getter;

@Getter
public class GuildResponse {
    private Long id;
    private String name;
    private String description;
    private int maxMembers;
    private long currentMemberCount;
    private boolean isPrivate;
    private boolean hasPassword;
    private String leaderName;
    private String joinCode;
    private Long currentBossId;

    public GuildResponse(Guild guild, long currentMemberCount) {
        this.id = guild.getId();
        this.name = guild.getName();
        this.description = guild.getDescription();
        this.maxMembers = guild.getMaxMembers();
        this.currentMemberCount = currentMemberCount;
        this.isPrivate = guild.isPrivate();
        this.hasPassword = guild.getPassword() != null && !guild.getPassword().isEmpty();
        this.leaderName = guild.getLeader().getNickname();
        this.joinCode = guild.getJoinCode();
        this.currentBossId = guild.getCurrentBoss() != null ? guild.getCurrentBoss().getId() : null;
    }
}
