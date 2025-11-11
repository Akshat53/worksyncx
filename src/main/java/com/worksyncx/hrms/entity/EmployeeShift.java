package com.worksyncx.hrms.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.Type;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

@Entity
@Table(name = "employee_shifts")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class EmployeeShift {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "employee_id", nullable = false)
    private Long employeeId;

    @Column(name = "shift_id", nullable = false)
    private Long shiftId;

    @Column(name = "effective_from", nullable = false)
    private LocalDate effectiveFrom;

    @Column(name = "effective_to")
    private LocalDate effectiveTo;

    @Column(name = "days_of_week", columnDefinition = "varchar(20)[]")
    private String[] daysOfWeek = {"MONDAY", "TUESDAY", "WEDNESDAY", "THURSDAY", "FRIDAY"};

    @Column(name = "is_active")
    private Boolean isActive = true;

    @Column(name = "assigned_by")
    private Long assignedBy;

    @Column(columnDefinition = "TEXT")
    private String notes;

    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    // Transient fields for relationships
    @Transient
    private Shift shift;

    @Transient
    private Employee employee;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
        if (daysOfWeek == null || daysOfWeek.length == 0) {
            daysOfWeek = new String[]{"MONDAY", "TUESDAY", "WEDNESDAY", "THURSDAY", "FRIDAY"};
        }
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }

    // Helper methods
    public boolean isActiveOn(LocalDate date) {
        if (!isActive) {
            return false;
        }
        if (date.isBefore(effectiveFrom)) {
            return false;
        }
        if (effectiveTo != null && date.isAfter(effectiveTo)) {
            return false;
        }
        return isWorkingDay(date);
    }

    public boolean isWorkingDay(LocalDate date) {
        if (daysOfWeek == null || daysOfWeek.length == 0) {
            return true; // If not specified, assume all days
        }
        String dayName = date.getDayOfWeek().name();
        return Arrays.asList(daysOfWeek).contains(dayName);
    }

    public List<DayOfWeek> getWorkingDaysAsDayOfWeek() {
        if (daysOfWeek == null) {
            return new ArrayList<>();
        }
        return Arrays.stream(daysOfWeek)
                .map(DayOfWeek::valueOf)
                .collect(Collectors.toList());
    }

    public void setWorkingDaysFromDayOfWeek(List<DayOfWeek> days) {
        if (days == null || days.isEmpty()) {
            this.daysOfWeek = new String[]{"MONDAY", "TUESDAY", "WEDNESDAY", "THURSDAY", "FRIDAY"};
        } else {
            this.daysOfWeek = days.stream()
                    .map(DayOfWeek::name)
                    .toArray(String[]::new);
        }
    }

    public boolean isOngoing() {
        return effectiveTo == null;
    }

    public boolean isExpired() {
        return effectiveTo != null && LocalDate.now().isAfter(effectiveTo);
    }

    public boolean isCurrent() {
        LocalDate now = LocalDate.now();
        return !now.isBefore(effectiveFrom) && (effectiveTo == null || !now.isAfter(effectiveTo));
    }
}
