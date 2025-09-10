package com.gamesUP.gamesUP.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;

@Entity
@Table(name = "games")
@Getter @Setter
public class Game {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String title;

    @Column(length = 2000)
    private String description;

    private Integer minPlayers, maxPlayers, minAge, durationMinutes;

    @Column(precision = 10, scale = 2)
    private BigDecimal price;

    @ManyToOne(optional = false)
    private Publisher publisher;
}
