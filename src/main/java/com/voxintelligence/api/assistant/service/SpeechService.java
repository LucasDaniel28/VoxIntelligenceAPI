package com.voxintelligence.api.assistant.service;

import org.springframework.ai.audio.tts.TextToSpeechModel;
import org.springframework.stereotype.Service;

@Service
public class SpeechService {
    private final TextToSpeechModel model;

    public SpeechService(TextToSpeechModel model) {
        this.model=model;
    }

    public byte[] synthesize(String text) {
        if (text == null || text.isBlank()) {
            throw new IllegalArgumentException("O texto não pode estar vazio.");
        }
        if (text.length() > 4000) {
            throw new IllegalArgumentException("O texto deve ter no máximo 4000 caracteres.");
        }
        byte[] audio=model.call(text.trim());
        if (audio == null || audio.length == 0) {
            throw new IllegalStateException("O serviço de voz não retornou áudio.");
        }
        return audio;
    }
}
