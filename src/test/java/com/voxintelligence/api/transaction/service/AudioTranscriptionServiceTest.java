package com.voxintelligence.api.transaction.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpStatus;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.ai.audio.transcription.TranscriptionModel;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class AudioTranscriptionServiceTest {

    private TranscriptionModel transcriptionModel;
    private AudioTranscriptionService service;

    @BeforeEach
    void setUp() {
        transcriptionModel = mock(TranscriptionModel.class);
        service = new AudioTranscriptionService(transcriptionModel);
    }

    @Test
    void shouldTranscribeValidAudioFile() {
        MockMultipartFile file = new MockMultipartFile(
                "file",
                "gasto.wav",
                "audio/wav",
                new byte[]{1, 2, 3}
        );
        when(transcriptionModel.transcribe(any(Resource.class)))
                .thenReturn("  Gastei 50 reais no mercado.  ");

        String result = service.transcribe(file);

        assertEquals("Gastei 50 reais no mercado.", result);
        verify(transcriptionModel).transcribe(any(Resource.class));
    }

    @Test
    void shouldRejectEmptyFile() {
        MockMultipartFile file = new MockMultipartFile(
                "file", "gasto.wav", "audio/wav", new byte[0]
        );

        ResponseStatusException exception = assertThrows(
                ResponseStatusException.class,
                () -> service.transcribe(file)
        );

        assertEquals(HttpStatus.BAD_REQUEST, exception.getStatusCode());
    }

    @Test
    void shouldRejectUnsupportedFileExtension() {
        MockMultipartFile file = new MockMultipartFile(
                "file", "documento.pdf", "application/pdf", new byte[]{1, 2, 3}
        );

        ResponseStatusException exception = assertThrows(
                ResponseStatusException.class,
                () -> service.transcribe(file)
        );

        assertEquals(HttpStatus.UNSUPPORTED_MEDIA_TYPE, exception.getStatusCode());
    }

    @Test
    void shouldRejectFileWithoutExtension() {
        MockMultipartFile file = new MockMultipartFile(
                "file", "audio", "application/octet-stream", new byte[]{1, 2, 3}
        );

        ResponseStatusException exception = assertThrows(
                ResponseStatusException.class,
                () -> service.transcribe(file)
        );

        assertEquals(HttpStatus.UNSUPPORTED_MEDIA_TYPE, exception.getStatusCode());
    }
}
