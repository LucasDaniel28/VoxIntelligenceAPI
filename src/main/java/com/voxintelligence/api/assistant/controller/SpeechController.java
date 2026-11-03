package com.voxintelligence.api.assistant.controller;

import com.voxintelligence.api.assistant.dto.SpeechRequest;
import com.voxintelligence.api.assistant.service.SpeechService;
import jakarta.validation.Valid;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/assistant")
public class SpeechController {
    private final SpeechService speechService;

    public SpeechController(SpeechService speechService) {
        this.speechService=speechService;
    }

    @PostMapping(value="/speech", produces="audio/mpeg")
    public ResponseEntity<byte[]> speech(@Valid @RequestBody SpeechRequest request) {
        byte[] audio=speechService.synthesize(request.text());
        return ResponseEntity.ok()
                .contentType(MediaType.parseMediaType("audio/mpeg"))
                .header(HttpHeaders.CONTENT_DISPOSITION, "inline; filename=vox-response.mp3")
                .body(audio);
    }
}
