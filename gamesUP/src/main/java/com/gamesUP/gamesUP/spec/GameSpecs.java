package com.gamesUP.gamesUP.spec;

import com.gamesUP.gamesUP.model.Game;
import org.springframework.data.jpa.domain.Specification;

import java.math.BigDecimal;

public final class GameSpecs {
    private GameSpecs(){}

    public static Specification<Game> qLike(String q){
        if (q == null || q.isBlank()) return null;
        String like = "%" + q.toLowerCase() + "%";
        return (root, cq, cb) -> cb.or(
                cb.like(cb.lower(root.get("title")), like),
                cb.like(cb.lower(root.get("description")), like)
        );
    }

    public static Specification<Game> publisherEq(Long id){
        return (id==null) ? null : (root, cq, cb) -> cb.equal(root.get("publisher").get("id"), id);
    }

    public static Specification<Game> playersCompatible(Integer p){
        return (p==null) ? null : (root, cq, cb) -> cb.and(
                cb.lessThanOrEqualTo(root.get("minPlayers"), p),
                cb.greaterThanOrEqualTo(root.get("maxPlayers"), p)
        );
    }

    public static Specification<Game> durationLe(Integer d){
        return (d==null) ? null : (root, cq, cb) -> cb.lessThanOrEqualTo(root.get("durationMinutes"), d);
    }

    public static Specification<Game> priceGte(BigDecimal min){
        return (min==null) ? null : (root, cq, cb) -> cb.greaterThanOrEqualTo(root.get("price"), min);
    }

    public static Specification<Game> priceLte(BigDecimal max){
        return (max==null) ? null : (root, cq, cb) -> cb.lessThanOrEqualTo(root.get("price"), max);
    }
}
