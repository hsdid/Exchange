package org.exchange.service.integration;


import com.fasterxml.jackson.databind.ObjectMapper;
import org.exchange.modules.user.infrastructure.rest.dto.BuyRequest;
import org.exchange.modules.user.infrastructure.rest.dto.auth.AuthenticationResponse;
import org.exchange.modules.user.infrastructure.rest.dto.auth.RegisterRequest;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.math.BigDecimal;

@SpringBootTest
@AutoConfigureMockMvc
public class WalletControllerTest {
    @Autowired
    private MockMvc mockMvc;
    @Autowired
    ObjectMapper objectMapper;

    @Test
    void buy_crypto() throws Exception {
        MvcResult result = mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(
                                new RegisterRequest("John Doe", "user@email.com", "password"))))
                .andExpect(status().isOk())
                .andReturn();

        String responseContent = result.getResponse().getContentAsString();
        AuthenticationResponse response = objectMapper.readValue(responseContent, AuthenticationResponse.class);

        BuyRequest request = new BuyRequest("BTC", new BigDecimal("1.0"));

        String json = objectMapper.writeValueAsString(request);

        mockMvc.perform(post("/api/crypto-buy").header("Authorization", "Bearer "+response.token())
                .contentType(MediaType.APPLICATION_JSON)
                .content(json))
                .andExpect(status().isOk());
    }

    @Test
    void buy_crypto_with_invalid_amount() throws Exception {
        MvcResult result = mockMvc.perform(post("/api/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(
                      new RegisterRequest("John Doe", "user1@email.com", "password"))))
                .andExpect(status().isOk())
                .andReturn();

        String responseContent = result.getResponse().getContentAsString();
        AuthenticationResponse response = objectMapper.readValue(responseContent, AuthenticationResponse.class);

        BuyRequest request = new BuyRequest("BTC", new BigDecimal("-1.0"));

        String json = objectMapper.writeValueAsString(request);

        mockMvc.perform(post("/api/crypto-buy")
                .header("Authorization", "Bearer "+response.token())
                .contentType(MediaType.APPLICATION_JSON)
                .content(json))
                .andExpect(status().isBadRequest());
    }

    @Test
    void buy_crypto_with_blank_symbol() throws Exception {
        MvcResult result = mockMvc.perform(post("/api/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(
                        new RegisterRequest("John Doe", "user2@email.com", "password"))))
                .andExpect(status().isOk())
                .andReturn();

        String responseContent = result.getResponse().getContentAsString();
        AuthenticationResponse response = objectMapper.readValue(responseContent, AuthenticationResponse.class);

        BuyRequest request = new BuyRequest("", new BigDecimal("1.0"));

        String json = objectMapper.writeValueAsString(request);

        mockMvc.perform(post("/api/crypto-buy")
                .header("Authorization", "Bearer "+response.token())
                .contentType(MediaType.APPLICATION_JSON)
                .content(json))
                .andExpect(status().isBadRequest());
    }

    @Test
    void buy_crypto_with_invalid_currency() throws Exception {
        MvcResult result = mockMvc.perform(post("/api/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(
                                new RegisterRequest("John Doe", "user3@email.com", "password"))))
                .andExpect(status().isOk())
                .andReturn();

        String responseContent = result.getResponse().getContentAsString();
        AuthenticationResponse response = objectMapper.readValue(responseContent, AuthenticationResponse.class);

        BuyRequest request = new BuyRequest("INVALID", new BigDecimal("1.0"));

        String json = objectMapper.writeValueAsString(request);

        mockMvc.perform(post("/api/crypto-buy")
                .header("Authorization", "Bearer "+response.token())
                .contentType(MediaType.APPLICATION_JSON)
                .content(json))
                .andExpect(status().isBadRequest());
    }
}
