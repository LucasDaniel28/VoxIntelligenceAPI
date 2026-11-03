package com.voxintelligence.api.assistant.service;

import org.junit.jupiter.api.Test;
import org.springframework.ai.audio.tts.TextToSpeechModel;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class SpeechServiceTest {
    private final TextToSpeechModel model=mock(TextToSpeechModel.class);
    private final SpeechService service=new SpeechService(model);

    @Test
    void shouldReturnGeneratedAudio() {
        byte[] expected={1,2,3};
        when(model.call("Olá")).thenReturn(expected);
        assertArrayEquals(expected, service.synthesize(" Olá "));
    }

    @Test
    void shouldRejectBlankText() {
        assertThrows(IllegalArgumentException.class, () -> service.synthesize(" "));
        verifyNoInteractions(model);
    }

    @Test
    void shouldRejectTextOverLimit() {
        assertThrows(IllegalArgumentException.class, () -> service.synthesize("a".repeat(4001)));
        verifyNoInteractions(model);
    }
}
