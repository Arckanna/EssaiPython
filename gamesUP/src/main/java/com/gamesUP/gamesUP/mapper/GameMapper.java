package com.gamesUP.gamesUP.mapper;

import com.gamesUP.gamesUP.dto.*;
import com.gamesUP.gamesUP.model.Game;
import com.gamesUP.gamesUP.model.Publisher;
import com.gamesUP.gamesUP.repository.PublisherRepository;
import org.springframework.stereotype.Component;

@Component
public class GameMapper {
    private final PublisherRepository publishers;
    public GameMapper(PublisherRepository publishers){ this.publishers = publishers; }

    public GameDto toDto(Game g){
        return new GameDto(
                g.getId(), g.getTitle(), g.getDescription(),
                g.getMinPlayers(), g.getMaxPlayers(), g.getMinAge(), g.getDurationMinutes(),
                g.getPrice(), g.getPublisher()!=null ? g.getPublisher().getId() : null
        );
    }
    public Game fromCreate(GameCreateDto in){
        Game g = new Game();
        g.setTitle(in.title());
        g.setDescription(in.description());
        g.setMinPlayers(in.minPlayers());
        g.setMaxPlayers(in.maxPlayers());
        g.setMinAge(in.minAge());
        g.setDurationMinutes(in.durationMinutes());
        g.setPrice(in.price());
        Publisher p = publishers.findById(in.publisherId()).orElseThrow();
        g.setPublisher(p);
        return g;
    }
    public void applyUpdate(Game g, GameUpdateDto in){
        if (in.title()!=null) g.setTitle(in.title());
        if (in.description()!=null) g.setDescription(in.description());
        if (in.minPlayers()!=null) g.setMinPlayers(in.minPlayers());
        if (in.maxPlayers()!=null) g.setMaxPlayers(in.maxPlayers());
        if (in.minAge()!=null) g.setMinAge(in.minAge());
        if (in.durationMinutes()!=null) g.setDurationMinutes(in.durationMinutes());
        if (in.price()!=null) g.setPrice(in.price());
        if (in.publisherId()!=null) g.setPublisher(publishers.findById(in.publisherId()).orElseThrow());
    }
}
