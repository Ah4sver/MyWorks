package com.daniilkhanukov.spring.myworks.controller;

import com.daniilkhanukov.spring.myworks.dto.ShortenRequest;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.testcontainers.postgresql.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.time.OffsetDateTime;

import static org.hamcrest.Matchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Testcontainers
class ShortenControllerIntegrationTest {

    @Container
    @ServiceConnection
    static PostgreSQLContainer postgres = new PostgreSQLContainer("postgres:16-alpine");

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    void shorten_createsLinkAndRedirectWorks() throws Exception {
        ShortenRequest request = new ShortenRequest("https://www.example.com/some/long/path", null, null);

        String responseBody = mockMvc.perform(post("/api/v1/links")
                        .contentType("application/json")
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.shortCode", not(emptyString())))
                .andExpect(jsonPath("$.originalUrl", is("https://www.example.com/some/long/path")))
                .andReturn().getResponse().getContentAsString();

        String shortCode = objectMapper.readTree(responseBody).get("shortCode").asText();

        mockMvc.perform(get("/{code}", shortCode))
                .andExpect(status().isFound())
                .andExpect(header().string("Location", "https://www.example.com/some/long/path"));
    }

    @Test
    void shorten_withCustomAlias_isUsedAsShortCode() throws Exception {
        String alias = "my-custom-alias";
        ShortenRequest request = new ShortenRequest("https://example.com", alias, null);

        mockMvc.perform(post("/api/v1/links")
                        .contentType("application/json")
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.shortCode", is(alias)));

        mockMvc.perform(get("/{code}", alias))
                .andExpect(status().isFound())
                .andExpect(header().string("Location", "https://example.com"));
    }

    @Test
    void shorten_withDuplicateAlias_returnsConflict() throws Exception {
        ShortenRequest request = new ShortenRequest("https://example.com/1", "duplicate-alias", null);

        mockMvc.perform(post("/api/v1/links")
                        .contentType("application/json")
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated());

        ShortenRequest secondRequest = new ShortenRequest("https://example.com/2", "duplicate-alias", null);

        mockMvc.perform(post("/api/v1/links")
                        .contentType("application/json")
                        .content(objectMapper.writeValueAsString(secondRequest)))
                .andExpect(status().isConflict());
    }

    @Test
    void shorten_withInvalidUrl_returnsBadRequest() throws Exception {
        ShortenRequest request = new ShortenRequest("not-a-valid-url", null, null);

        mockMvc.perform(post("/api/v1/links")
                        .contentType("application/json")
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void redirect_withExpiredLink_returnsGone() throws Exception {
        ShortenRequest request = new ShortenRequest(
                "https://example.com/expiring",
                "expiring-alias",
                OffsetDateTime.now().plusSeconds(2)
        );

        mockMvc.perform(post("/api/v1/links")
                        .contentType("application/json")
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated());

        Thread.sleep(3000);

        mockMvc.perform(get("/{code}", "expiring-alias"))
                .andExpect(status().isGone());
    }

    @Test
    void redirect_withUnknownCode_returnsNotFound() throws Exception {
        mockMvc.perform(get("/{code}", "does-not-exist"))
                .andExpect(status().isNotFound());
    }
}