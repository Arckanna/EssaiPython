
package com.gamesUP.gamesUP.controller;

import com.gamesUP.gamesUP.model.Publisher;
import com.gamesUP.gamesUP.repository.PublisherRepository;
import jakarta.validation.constraints.NotBlank;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import org.springframework.security.access.prepost.PreAuthorize;



@RestController
@RequestMapping("/api/publishers")
public class PublisherController {
    private final PublisherRepository repo;
    public PublisherController(PublisherRepository repo){ this.repo = repo; }

    public record PublisherCreateDto(@NotBlank String name) {}
    public record PublisherUpdateDto(String name) {}

    @GetMapping public List<Publisher> list(){ return repo.findAll(); }

    @GetMapping("/{id}")
    public Publisher get(@PathVariable Long id){
        return repo.findById(id).orElseThrow(() -> new IllegalArgumentException("Publisher "+id+" not found"));
    }

    @PostMapping
    public Publisher create(@RequestBody PublisherCreateDto in){
        Publisher p = new Publisher(); p.setName(in.name()); return repo.save(p);
    }

    @PutMapping("/{id}")
    public Publisher update(@PathVariable Long id, @RequestBody PublisherUpdateDto in){
        Publisher p = repo.findById(id).orElseThrow(() -> new IllegalArgumentException("Publisher "+id+" not found"));
        if (in.name()!=null) p.setName(in.name());
        return repo.save(p);
    }

    @DeleteMapping("/{id}")
    public void delete(@PathVariable Long id){ repo.deleteById(id); }
}
