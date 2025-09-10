package com.gamesUP.gamesUP.repository;

import com.gamesUP.gamesUP.model.Game;
import com.gamesUP.gamesUP.model.Publisher;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
class GameRepositoryTest {

    @Autowired GameRepository games;
    @Autowired PublisherRepository pubs;

    @Test
    void findByTitleIgnoreCase_works() {
        Publisher p = new Publisher();
        p.setName("Asmodee");
        pubs.save(p);

        Game g = new Game();
        g.setTitle("Catan");
        g.setPrice(new BigDecimal("34.90"));
        g.setPublisher(p);
        games.save(g);

        assertThat(games.findByTitleIgnoreCase("catan")).isPresent();
        assertThat(games.findByTitleIgnoreCase("CATAN")).isPresent();
        assertThat(games.findByTitleIgnoreCase("katan")).isNotPresent();
    }
}
