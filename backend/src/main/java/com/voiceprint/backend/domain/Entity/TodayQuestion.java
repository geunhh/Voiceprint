package com.voiceprint.backend.domain.Entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDate;


@Entity
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Getter
@Setter
@Table(name = "today_question")
public class TodayQuestion {
    @Id
    @Column(name = "date", columnDefinition = "DATE")
    private LocalDate date; // pk 오늘 날짜

    @Column(name = "question_id", nullable = false)
    private Byte questionId;

    public TodayQuestion(LocalDate date) {
        this.date = date;
    }


}
