package com.worksyncx.hrms.dto.shift;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.LocalTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ShiftResponse {
    private Long id;
    private Long tenantId;
    private String name;
    private String code;
    private LocalTime startTime;
    private LocalTime endTime;
    private Integer gracePeriodMinutes;
    private BigDecimal halfDayHours;
    private BigDecimal fullDayHours;
    private String color;
    private String description;
    private Boolean isActive;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    // Additional computed fields
    private LocalTime graceTimeLimit;
    private Integer totalHours;
    private Integer totalMinutes;
}
