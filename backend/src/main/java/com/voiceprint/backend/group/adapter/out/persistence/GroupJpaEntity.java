package com.voiceprint.backend.group.adapter.out.persistence;


import jakarta.persistence.Table;
import jakarta.persistence.*;
import lombok.*;

import java.time.DayOfWeek;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;

@Entity(name = "Group")
@Table(name = "`groups`")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class GroupJpaEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(nullable = false, length = 30)
    private String name;

    @Column(length = 255)
    private String description;

    @Column(nullable = false)
    private String groupImage;

    @Column(name = "alarm_day")
    @Convert(converter = StringListConverter.class)
    private List<DayOfWeek> alarmDays;

    @Column(columnDefinition = "TIME")
    private LocalTime alarmTime;

    @Column(nullable = false, columnDefinition = "BOOLEAN DEFAULT FALSE")
    @Builder.Default
    private Boolean enableAlarm = false;

    @Column(nullable = false, columnDefinition = "BOOLEAN DEFAULT FALSE")
    @Builder.Default
    private Boolean isDeleted = false;

    @Column(nullable = false, updatable = false)
    @Builder.Default
    private LocalDateTime createdAt = LocalDateTime.now();

    @Column(nullable = false)
    @Builder.Default
    private LocalDateTime updatedAt = LocalDateTime.now();

    @PreUpdate
    public void preUpdate() {
        this.updatedAt = LocalDateTime.now();
    }
}
