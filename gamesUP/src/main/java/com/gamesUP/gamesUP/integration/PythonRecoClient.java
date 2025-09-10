package com.gamesUP.gamesUP.integration;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

import java.util.List;

@Component
public class PythonRecoClient {
    private final RestClient rest;

    public PythonRecoClient(@Value("${python.api.base-url}") String baseUrl) {
        this.rest = RestClient.builder().baseUrl(baseUrl).build();
    }

    public List<Integer> getRecommendations(long userId, List<Purchase> purchases, int k) {
        var payload = new UserData(userId, purchases);
        RecoResponse resp = rest.post()
                .uri(uri -> uri.path("/recommendations").queryParam("k", k).build())
                .contentType(MediaType.APPLICATION_JSON)
                .body(payload)
                .retrieve()
                .body(RecoResponse.class);
        return resp != null ? resp.recommendations() : List.of();
    }

    public record Purchase(long game_id, double rating) {}
    public record UserData(long user_id, List<Purchase> purchases) {}
    public record RecoResponse(long user_id, List<Integer> recommendations) {}
    // dans PythonRecoClient
    public record TrainUser(long user_id, List<Purchase> purchases) {}
    public record TrainRequest(List<TrainUser> interactions) {}

    public void train(List<TrainUser> interactions){
        rest.post()
                .uri("/train")
                .contentType(MediaType.APPLICATION_JSON)
                .body(new TrainRequest(interactions))
                .retrieve()
                .toBodilessEntity();
    }

}
