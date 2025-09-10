package com.gamesUP.gamesUP.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.math.BigDecimal;

public record GameCreateDto(
        @NotBlank String title,
        String description,
        Integer minPlayers,
        Integer maxPlayers,
        Integer minAge,
        Integer durationMinutes,
        BigDecimal price,
        @NotNull Long publisherId
) {}
