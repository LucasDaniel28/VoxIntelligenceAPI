package com.voxintelligence.api.transaction.controller;

import com.voxintelligence.api.transaction.dto.TransactionInterpretationRequest;
import com.voxintelligence.api.transaction.dto.TransactionInterpretationResponse;
import com.voxintelligence.api.transaction.service.TransactionTextInterpreter;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/transactions")
public class TransactionController {

    private final TransactionTextInterpreter interpreter;

    public TransactionController(TransactionTextInterpreter interpreter) {
        this.interpreter = interpreter;
    }

    @PostMapping("/interpret")
    public ResponseEntity<TransactionInterpretationResponse> interpret(
            @Valid @RequestBody TransactionInterpretationRequest request
    ) {
        return ResponseEntity.ok(interpreter.interpret(request.text()));
    }
}
