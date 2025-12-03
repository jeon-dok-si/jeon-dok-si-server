package com.jeondoksi.jeondoksi.domain.guild.dto;

import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class JoinGuildRequest {
    private String password;
    private String joinCode;
}
