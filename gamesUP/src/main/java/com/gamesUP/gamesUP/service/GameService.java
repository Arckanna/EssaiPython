package com.gamesUP.gamesUP.service;

import com.gamesUP.gamesUP.model.Game;
import com.gamesUP.gamesUP.repository.GameRepository;
import org.springframework.data.domain.*;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service @Transactional
public class GameService {
    private final GameRepository repo;
    public GameService(GameRepository repo){ this.repo = repo; }

    @Transactional(readOnly = true)
    public Page<Game> search(Specification<Game> spec, Pageable pageable){ return repo.findAll(spec, pageable); }

    @Transactional(readOnly = true)
    public Game get(Long id){ return repo.findById(id).orElseThrow(() -> new IllegalArgumentException("Game "+id+" not found")); }

    public Game create(Game g){ return repo.save(g); }

    public Game update(Long id, java.util.function.Consumer<Game> patch){
        Game g = get(id); patch.accept(g); return repo.save(g);
    }

    public void delete(Long id){ repo.deleteById(id); }
}
