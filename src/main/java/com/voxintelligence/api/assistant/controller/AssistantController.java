package com.voxintelligence.api.assistant.controller;

import com.voxintelligence.api.assistant.dto.AssistantMessageRequest;
import com.voxintelligence.api.assistant.dto.AssistantMessageResponse;
import com.voxintelligence.api.assistant.service.AssistantService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/assistant")
public class AssistantController {

    private final AssistantService assistantService;

    public AssistantController(AssistantService assistantService) {
        this.assistantService = assistantService;
    }

    @PostMapping("/message")
    public ResponseEntity<AssistantMessageResponse> sendMessage(
            @Valid @RequestBody AssistantMessageRequest request
    ) {
        return ResponseEntity.ok(
                new AssistantMessageResponse(assistantService.chat(request.message()))
        );
    }
}
