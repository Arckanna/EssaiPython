package com.gamesUP.gamesUP.dto;

import java.math.BigDecimal;

public record GameDto(Long id, String title, String description,
                      Integer minPlayers, Integer maxPlayers, Integer minAge, Integer durationMinutes,
                      BigDecimal price, Long publisherId) {}
