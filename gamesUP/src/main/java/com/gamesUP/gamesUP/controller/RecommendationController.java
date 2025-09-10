package com.gamesUP.gamesUP.controller;

import com.gamesUP.gamesUP.integration.PythonRecoClient;
import com.gamesUP.gamesUP.model.Game;
import com.gamesUP.gamesUP.service.GameService;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/recommendations")
public class RecommendationController {

    private final PythonRecoClient py;
    private final GameService gameService;
    public RecommendationController(PythonRecoClient py, GameService gameService){
        this.py = py; this.gameService = gameService;
    }

    // Exemple simple: on considère que "rating=1" pour chaque achat
    @GetMapping("/{userId}")
    public List<Integer> recommend(@PathVariable long userId, @RequestParam(defaultValue="10") int k){
        // TODO: charge les achats/notes du user depuis tes tables (Purchase/Order/Avis)
        // Ici on simule un user qui a acheté les id 1 et 2:
        var purchases = List.of(
                new PythonRecoClient.Purchase(1, 1.0),
                new PythonRecoClient.Purchase(2, 1.0)
        );
        return py.getRecommendations(userId, purchases, k);
    }
}
