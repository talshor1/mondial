package com.mondial.api.dto;

import java.util.List;

public record LeagueResponse(
        Long id,
        String name,
        String leagueCode,
        List<MemberInfo> members
) {
    public record MemberInfo(Long id, String email, String teamName, int points) {}
}