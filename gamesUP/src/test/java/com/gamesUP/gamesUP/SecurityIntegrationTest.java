package com.gamesUP.gamesUP;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.gamesUP.gamesUP.domain.Role;
import com.gamesUP.gamesUP.domain.User;
import com.gamesUP.gamesUP.model.Publisher;
import com.gamesUP.gamesUP.repository.PublisherRepository;
import com.gamesUP.gamesUP.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import static org.hamcrest.Matchers.containsString;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class SecurityIntegrationTest {

    @Autowired MockMvc mvc;
    @Autowired ObjectMapper om;
    @Autowired UserRepository users;
    @Autowired PasswordEncoder encoder;
    @Autowired PublisherRepository pubs;

    @BeforeEach
    void setup() {
        users.deleteAll();
        pubs.deleteAll();
        // Admin
        User admin = new User();
        admin.setEmail("admin@gamesup.local");
        admin.setPasswordHash(encoder.encode("admin"));
        admin.setRole(Role.ADMIN);
        users.save(admin);
        // Client
        User client = new User();
        client.setEmail("client@gamesup.local");
        client.setPasswordHash(encoder.encode("client"));
        client.setRole(Role.CLIENT);
        users.save(client);
        // Publisher pour créer des jeux
        Publisher p = new Publisher();
        p.setName("Asmodee");
        pubs.save(p);
    }

    String login(String email, String password) throws Exception {
        var body = om.writeValueAsString(new Login(email, password));
        String json = mvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();
        return om.readTree(json).get("accessToken").asText();
    }
    record Login(String email, String password) {}

    @Test
    void GET_games_is_public() throws Exception {
        mvc.perform(get("/api/games"))
                .andExpect(status().isOk());
    }

    @Test
    void POST_games_requires_admin() throws Exception {
        // 401 if no token
        mvc.perform(post("/api/games")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
          {"title":"Test","publisherId":1,"price":10.0}
          """))
                .andExpect(status().isForbidden()); // 403 because security chain requires auth => with our config it's 403

        // 403 if client token
        String clientToken = login("client@gamesup.local", "client");
        mvc.perform(post("/api/games")
                        .header("Authorization", "Bearer " + clientToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
          {"title":"Test","publisherId":1,"price":10.0}
          """))
                .andExpect(status().isForbidden());

        // 200/201 if admin token
        String adminToken = login("admin@gamesup.local", "admin");
        mvc.perform(post("/api/games")
                        .header("Authorization", "Bearer " + adminToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
          {"title":"Admin Game","publisherId":1,"price":12.5}
          """))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("Admin Game")));
    }
}
