package com.worksyncx.hrms.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.LocalTime;

@Entity
@Table(name = "shifts", uniqueConstraints = {
    @UniqueConstraint(columnNames = {"tenant_id", "code"})
})
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Shift {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "tenant_id", nullable = false)
    private Long tenantId;

    @Column(nullable = false, length = 100)
    private String name;

    @Column(nullable = false, length = 50)
    private String code;

    @Column(name = "start_time", nullable = false, columnDefinition = "TIME")
    private LocalTime startTime;

    @Column(name = "end_time", nullable = false, columnDefinition = "TIME")
    private LocalTime endTime;

    @Column(name = "grace_period_minutes")
    private Integer gracePeriodMinutes = 15;

    @Column(name = "half_day_hours", precision = 4, scale = 2)
    private BigDecimal halfDayHours = BigDecimal.valueOf(4.0);

    @Column(name = "full_day_hours", precision = 4, scale = 2)
    private BigDecimal fullDayHours = BigDecimal.valueOf(8.0);

    @Column(length = 7)
    private String color = "#3B82F6";

    @Column(columnDefinition = "TEXT")
    private String description;

    @Column(name = "is_active")
    private Boolean isActive = true;

    @Column(name = "is_deleted")
    private Boolean isDeleted = false;

    @Column(name = "deleted_at")
    private LocalDateTime deletedAt;

    @Column(name = "deleted_by")
    private Long deletedBy;

    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }

    // Helper methods
    public LocalTime getGraceTimeLimit() {
        return startTime.plusMinutes(gracePeriodMinutes);
    }

    public boolean isWithinGracePeriod(LocalTime checkInTime) {
        return !checkInTime.isAfter(getGraceTimeLimit());
    }

    public int getMinutesLate(LocalTime checkInTime) {
        if (checkInTime.isBefore(getGraceTimeLimit())) {
            return 0;
        }
        return (int) java.time.Duration.between(startTime, checkInTime).toMinutes();
    }

    public int getMinutesEarly(LocalTime checkOutTime) {
        if (checkOutTime.isAfter(endTime) || checkOutTime.equals(endTime)) {
            return 0;
        }
        return (int) java.time.Duration.between(checkOutTime, endTime).toMinutes();
    }
}
