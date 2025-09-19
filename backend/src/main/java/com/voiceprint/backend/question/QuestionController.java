package com.voiceprint.backend.question;


import com.voiceprint.backend.question.dto.QuestionGetResponseDTO;
import com.voiceprint.backend.global.dto.CommonResponse;
import com.voiceprint.backend.question.service.question.QuestionService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1")
public class QuestionController {
    private final QuestionService questionService;

    public QuestionController(QuestionService questionService) {
        this.questionService = questionService;
    }
    @GetMapping("/today-question")
    public ResponseEntity<CommonResponse<QuestionGetResponseDTO>> getTodayQustion() {
//        QuestionGetResponseDTO questionGetResponseDTO = questionService.getTodayQuestion();
        //Todo : 임시 질문 사용.
        QuestionGetResponseDTO questionGetResponseDTO = new QuestionGetResponseDTO();
        questionGetResponseDTO.setQuestion("임시 질문");
        questionGetResponseDTO.setId((byte) 1);
        return ResponseEntity.ok(new CommonResponse<>(200, "오늘의 질문 조회 성공", questionGetResponseDTO));
    }
}
