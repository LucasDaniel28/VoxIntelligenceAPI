package com.voxintelligence.api.assistant.controller;

import com.voxintelligence.api.assistant.service.AssistantService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.Mockito.when;
import static org.springframework.http.MediaType.APPLICATION_JSON;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(value = AssistantController.class, properties = "app.security.api-key=test-api-key")
class AssistantControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private AssistantService assistantService;

    @Test
    void shouldReturnAssistantResponse() throws Exception {
        when(assistantService.chat("Qual é meu saldo?"))
                .thenReturn("Seu saldo atual é R$ 0,00.");

        mockMvc.perform(post("/api/v1/assistant/message").header("X-API-KEY", "test-api-key")
                        .contentType(APPLICATION_JSON)
                        .content("""
                                {"message":"Qual é meu saldo?"}
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("Seu saldo atual é R$ 0,00."));
    }

    @Test
    void shouldRejectBlankMessage() throws Exception {
        mockMvc.perform(post("/api/v1/assistant/message")
                        .contentType(APPLICATION_JSON)
                        .content("""
                                {"message":" "}
                                """))
                .andExpect(status().isBadRequest());
    }
}
