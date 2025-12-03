package com.jeondoksi.jeondoksi.domain.guild.dto;

import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class CreateGuildRequest {
    private String name;
    private String description;
    private int maxMembers;
    @com.fasterxml.jackson.annotation.JsonProperty("isPrivate")
    private boolean isPrivate;
    private String password;
    private boolean generateJoinCode;
}
