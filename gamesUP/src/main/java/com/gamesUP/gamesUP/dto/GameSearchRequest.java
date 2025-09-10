package com.gamesUP.gamesUP.dto;

import java.math.BigDecimal;

public record GameSearchRequest(
        String q,          // texte libre: titre/description
        Long publisherId,  // filtre éditeur
        Integer players,   // nb joueurs souhaité
        Integer durationMax, // durée max en minutes
        BigDecimal priceMin,
        BigDecimal priceMax
) {}
