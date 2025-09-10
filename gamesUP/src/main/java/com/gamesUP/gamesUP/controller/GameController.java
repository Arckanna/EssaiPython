package com.gamesUP.gamesUP.controller;

import com.gamesUP.gamesUP.dto.*;
import com.gamesUP.gamesUP.mapper.GameMapper;
import com.gamesUP.gamesUP.model.Game;
import com.gamesUP.gamesUP.service.GameService;
import jakarta.validation.Valid;
import org.springframework.data.domain.*;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.web.bind.annotation.*;
import org.springframework.security.access.prepost.PreAuthorize;



@RestController
@RequestMapping("/api/games")
public class GameController {
    private final GameService service; private final GameMapper mapper;
    public GameController(GameService service, GameMapper mapper){ this.service = service; this.mapper = mapper; }

    // LIST (pagination)

    @GetMapping
    public Page<GameDto> list(
            com.gamesUP.gamesUP.dto.GameSearchRequest req,
            @RequestParam(defaultValue="0") int page,
            @RequestParam(defaultValue="20") int size,
            @RequestParam(defaultValue="title") String sortBy,
            @RequestParam(defaultValue="ASC") Sort.Direction dir
    ){
        Pageable pageable = PageRequest.of(page, size, Sort.by(dir, sortBy));
        Specification<Game> spec = Specification
                .where(com.gamesUP.gamesUP.spec.GameSpecs.qLike(req.q()))
                .and(com.gamesUP.gamesUP.spec.GameSpecs.publisherEq(req.publisherId()))
                .and(com.gamesUP.gamesUP.spec.GameSpecs.playersCompatible(req.players()))
                .and(com.gamesUP.gamesUP.spec.GameSpecs.durationLe(req.durationMax()))
                .and(com.gamesUP.gamesUP.spec.GameSpecs.priceGte(req.priceMin()))
                .and(com.gamesUP.gamesUP.spec.GameSpecs.priceLte(req.priceMax()));
        return service.search(spec, pageable).map(mapper::toDto);
    }

    // GET by id
    @GetMapping("/{id}")
    public GameDto get(@PathVariable Long id){ return mapper.toDto(service.get(id)); }
    // CREATE
    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public GameDto create(@Valid @RequestBody GameCreateDto in){
        Game saved = service.create(mapper.fromCreate(in));
        return mapper.toDto(saved);
    }
    // UPDATE
    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public GameDto update(@PathVariable Long id, @Valid @RequestBody GameUpdateDto in){
        return mapper.toDto(service.update(id, g -> mapper.applyUpdate(g, in)));
    }
    // DELETE
    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public void delete(@PathVariable Long id){ service.delete(id); }
}
