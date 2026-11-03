package com.voxintelligence.api.transaction.service;

import org.springframework.ai.audio.transcription.TranscriptionModel;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;

import java.io.IOException;
import java.util.Locale;
import java.util.Set;

@Service
public class AudioTranscriptionService {

    private static final long MAX_FILE_SIZE = 25L * 1024 * 1024;
    private static final Set<String> ALLOWED_EXTENSIONS =
            Set.of("mp3", "mp4", "mpeg", "mpga", "m4a", "wav", "webm", "flac");

    private final TranscriptionModel transcriptionModel;

    public AudioTranscriptionService(TranscriptionModel transcriptionModel) {
        this.transcriptionModel = transcriptionModel;
    }

    public String transcribe(MultipartFile file) {
        validate(file);

        String filename = safeFilename(file.getOriginalFilename());

        try {
            ByteArrayResource audioResource = new ByteArrayResource(file.getBytes()) {
                @Override
                public String getFilename() {
                    return filename;
                }
            };

            String transcript = transcriptionModel.transcribe(audioResource);
            if (transcript == null || transcript.isBlank()) {
                throw new ResponseStatusException(HttpStatus.UNPROCESSABLE_ENTITY,
                        "Não foi possível identificar fala no arquivo enviado.");
            }

            return transcript.trim();
        } catch (IOException exception) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "Não foi possível ler o arquivo de áudio.", exception);
        }
    }

    private void validate(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "Envie um arquivo de áudio.");
        }

        if (file.getSize() > MAX_FILE_SIZE) {
            throw new ResponseStatusException(HttpStatus.PAYLOAD_TOO_LARGE,
                    "O arquivo deve ter no máximo 25 MB.");
        }

        String filename = file.getOriginalFilename();
        if (filename == null || !filename.contains(".")) {
            throw new ResponseStatusException(HttpStatus.UNSUPPORTED_MEDIA_TYPE,
                    "O arquivo precisa possuir uma extensão de áudio válida.");
        }

        String extension = filename.substring(filename.lastIndexOf('.') + 1)
                .toLowerCase(Locale.ROOT);

        if (!ALLOWED_EXTENSIONS.contains(extension)) {
            throw new ResponseStatusException(HttpStatus.UNSUPPORTED_MEDIA_TYPE,
                    "Formato não suportado. Utilize MP3, MP4, MPEG, MPGA, M4A, WAV, WEBM ou FLAC.");
        }
    }

    private String safeFilename(String originalFilename) {
        String filename = originalFilename == null ? "audio.wav" : originalFilename;
        String sanitized = filename.replaceAll("[^a-zA-Z0-9._-]", "_");
        return sanitized.isBlank() ? "audio.wav" : sanitized;
    }
}
