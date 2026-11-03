package com.voxintelligence.api.transaction.controller;

import com.voxintelligence.api.transaction.dto.TransactionInterpretationResponse;
import com.voxintelligence.api.transaction.model.TransactionCategory;
import com.voxintelligence.api.transaction.model.TransactionType;
import com.voxintelligence.api.transaction.service.AudioTranscriptionService;
import com.voxintelligence.api.transaction.service.TransactionInterpretationService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.mock.web.MockMultipartFile;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(TransactionController.class)
class TransactionControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private TransactionInterpretationService interpretationService;

    @MockitoBean
    private AudioTranscriptionService transcriptionService;

    @Test
    void shouldInterpretTransactionFromText() throws Exception {
        when(interpretationService.interpret("Gastei 50 reais no mercado"))
                .thenReturn(sampleResponse());

        mockMvc.perform(post("/api/v1/transactions/interpret")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"text":"Gastei 50 reais no mercado"}
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.type").value("EXPENSE"))
                .andExpect(jsonPath("$.amount").value(50.00))
                .andExpect(jsonPath("$.category").value("FOOD"))
                .andExpect(jsonPath("$.status").value("READY_FOR_CONFIRMATION"));
    }

    @Test
    void shouldRejectBlankText() throws Exception {
        mockMvc.perform(post("/api/v1/transactions/interpret")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"text":"   "}
                                """))
                .andExpect(status().isBadRequest());
    }

    @Test
    void shouldTranscribeAndInterpretUploadedAudio() throws Exception {
        MockMultipartFile audio = new MockMultipartFile(
                "file", "gasto.wav", "audio/wav", new byte[]{1, 2, 3}
        );

        when(transcriptionService.transcribe(any())).thenReturn("Gastei 50 reais no mercado");
        when(interpretationService.interpret("Gastei 50 reais no mercado"))
                .thenReturn(sampleResponse());

        mockMvc.perform(multipart("/api/v1/transactions/interpret-audio").file(audio))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.type").value("EXPENSE"))
                .andExpect(jsonPath("$.amount").value(50.00))
                .andExpect(jsonPath("$.originalText").value("Gastei 50 reais no mercado"));
    }

    private TransactionInterpretationResponse sampleResponse() {
        return new TransactionInterpretationResponse(
                "Gastei 50 reais no mercado",
                TransactionType.EXPENSE,
                new BigDecimal("50.00"),
                "BRL",
                "Compra no mercado",
                "Mercado",
                TransactionCategory.FOOD,
                "GROCERIES",
                LocalDate.of(2026, 11, 3),
                "READY_FOR_CONFIRMATION",
                List.of()
        );
    }
}
