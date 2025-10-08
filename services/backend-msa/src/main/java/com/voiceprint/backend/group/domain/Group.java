package com.voiceprint.backend.group.domain;

import lombok.Builder;
import lombok.Getter;

import java.time.Clock;
import java.time.DayOfWeek;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;

@Getter
public class Group {

    private final Integer id;                 // 신규 생성 시 null
    private final String name;
    private final String description;
    private final String groupImage;
    private final List<DayOfWeek> alarmDays;  // 불변 보장 위해 방어적 복사
    private final LocalTime alarmTime;
    private final Boolean enableAlarm;        // 상태 전이 메서드로만 변경
    private final Boolean isDeleted;
    private final LocalDateTime createdAt;
    private final LocalDateTime updatedAt;

    /* =========================
       신규 생성 전용
       ========================= */
    @Builder(toBuilder = true)
    private Group(String name,
                  String description,
                  String groupImage,
                  List<DayOfWeek> alarmDays,
                  LocalTime alarmTime,
                  Boolean enableAlarm
    ) {

        this.id = null; // 신규 생성에서는 없음
        this.name = name;
        this.description = description;
        this.groupImage = groupImage;
        this.alarmDays = alarmDays == null ? List.of() : List.copyOf(alarmDays);
        this.alarmTime = alarmTime;
        this.enableAlarm = enableAlarm != null ? enableAlarm : Boolean.FALSE;

        this.isDeleted = false; // 규칙
        this.createdAt = LocalDateTime.now();
        this.updatedAt = this.createdAt;
    }

    /* ==================================
       재구성 전용 (Repository/Mapper 전용)
       ================================== */
    public Group(Integer id,
                 String name,
                 String description,
                 String groupImage,
                 List<DayOfWeek> alarmDays,
                 LocalTime alarmTime,
                 Boolean enableAlarm,
                 Boolean isDeleted,
                 LocalDateTime createdAt,
                 LocalDateTime updatedAt) {
        this.id = id;
        this.name = name;
        this.description = description;
        this.groupImage = groupImage;
        this.alarmDays = alarmDays == null ? List.of() : List.copyOf(alarmDays);
        this.alarmTime = alarmTime;
        this.enableAlarm = enableAlarm;
        this.isDeleted = isDeleted;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }

    /* =========================
       정적 팩토리
       ========================= */
    public static Group create(String name,
                               String description,
                               String groupImage,
                               List<DayOfWeek> alarmDays,
                               LocalTime alarmTime,
                               Boolean enableAlarm) {
        return Group.builder()
                .name(name)
                .description(description)
                .groupImage(groupImage)
                .alarmDays(alarmDays)
                .alarmTime(alarmTime)
                .enableAlarm(enableAlarm)
                .build();
    }

    public static Group reconstitute(Integer id,
                                     String name,
                                     String description,
                                     String groupImage,
                                     List<DayOfWeek> alarmDays,
                                     LocalTime alarmTime,
                                     Boolean enableAlarm,
                                     Boolean isDeleted,
                                     LocalDateTime createdAt,
                                     LocalDateTime updatedAt) {
        return new Group(id, name, description, groupImage, alarmDays, alarmTime,
                enableAlarm, isDeleted, createdAt, updatedAt);
    }

    /* =========================
       불변 업데이트(상태 전이) 메서드
       ========================= */
    public Group withEnableAlarm(boolean enable, Clock clock) {
        final Clock c = (clock == null ? Clock.systemDefaultZone() : clock);
        return new Group(
                this.id, this.name, this.description, this.groupImage, this.alarmDays,
                this.alarmTime, enable, this.isDeleted, this.createdAt, LocalDateTime.now(c)
        );
    }

    public Group withAlarmTime(LocalTime time, Clock clock) {
        final Clock c = (clock == null ? Clock.systemDefaultZone() : clock);
        return new Group(
                this.id, this.name, this.description, this.groupImage, this.alarmDays,
                time, this.enableAlarm, this.isDeleted, this.createdAt, LocalDateTime.now(c)
        );
    }
}