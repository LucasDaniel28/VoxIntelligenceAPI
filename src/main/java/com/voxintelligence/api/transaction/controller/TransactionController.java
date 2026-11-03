package com.voxintelligence.api.transaction.controller;

import com.voxintelligence.api.transaction.dto.TransactionInterpretationRequest;
import com.voxintelligence.api.transaction.dto.TransactionInterpretationResponse;
import com.voxintelligence.api.transaction.service.AudioTranscriptionService;
import com.voxintelligence.api.transaction.service.TransactionInterpretationService;
import jakarta.validation.Valid;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/api/v1/transactions")
public class TransactionController {

    private final TransactionInterpretationService interpretationService;
    private final AudioTranscriptionService transcriptionService;

    public TransactionController(
            TransactionInterpretationService interpretationService,
            AudioTranscriptionService transcriptionService
    ) {
        this.interpretationService = interpretationService;
        this.transcriptionService = transcriptionService;
    }

    @PostMapping("/interpret")
    public ResponseEntity<TransactionInterpretationResponse> interpret(
            @Valid @RequestBody TransactionInterpretationRequest request
    ) {
        return ResponseEntity.ok(interpretationService.interpret(request.text()));
    }

    @PostMapping(value = "/interpret-audio", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<TransactionInterpretationResponse> interpretAudio(
            @RequestPart("file") MultipartFile file
    ) {
        String transcript = transcriptionService.transcribe(file);
        TransactionInterpretationResponse response = interpretationService.interpret(transcript);
        return ResponseEntity.ok(response);
    }
}
