package com.worksyncx.hrms.dto.shift;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class EmployeeShiftResponse {
    private Long id;
    private Long employeeId;
    private String employeeName;
    private String employeeCode;
    private Long shiftId;
    private ShiftResponse shift;
    private LocalDate effectiveFrom;
    private LocalDate effectiveTo;
    private List<String> daysOfWeek;
    private Boolean isActive;
    private Long assignedBy;
    private String assignedByName;
    private String notes;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    // Additional computed fields
    private Boolean isCurrent;
    private Boolean isOngoing;
    private Boolean isExpired;
}
