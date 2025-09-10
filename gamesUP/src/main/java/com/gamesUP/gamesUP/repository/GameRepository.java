package com.gamesUP.gamesUP.repository;
import com.gamesUP.gamesUP.model.Game;
import org.springframework.data.jpa.repository.*;
import java.util.Optional;

public interface GameRepository extends JpaRepository<Game, Long>, JpaSpecificationExecutor<Game> {
  Optional<Game> findByTitleIgnoreCase(String title);
}
