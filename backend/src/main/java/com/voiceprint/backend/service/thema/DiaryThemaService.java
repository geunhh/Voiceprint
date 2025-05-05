package com.voiceprint.backend.service.thema;

import com.voiceprint.backend.api.thema.dto.DiaryThemaListResponseDTO;
import com.voiceprint.backend.api.thema.dto.DiaryThemaResponse;
import com.voiceprint.backend.domain.thema.DiaryThema;
import com.voiceprint.backend.domain.thema.DiaryThemaRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class DiaryThemaService {
    private final DiaryThemaRepository diaryThemaRepository;
    public DiaryThemaListResponseDTO getThemasForUser(Long userId) {

        List<DiaryThema> themas = diaryThemaRepository.findByUserIdOrDefault(userId);
        System.out.println(themas);

        List<DiaryThemaResponse> defaultThemas = new ArrayList<>();
        List<DiaryThemaResponse> customThemas = new ArrayList<>();

        for (DiaryThema thema : themas) {
            DiaryThemaResponse dto = DiaryThemaResponse.from(thema);
            if (thema.getUser() == null) {
                defaultThemas.add(dto);
            } else {
                customThemas.add(dto);
            }
        }

        return new DiaryThemaListResponseDTO(defaultThemas,customThemas);
    }
}
