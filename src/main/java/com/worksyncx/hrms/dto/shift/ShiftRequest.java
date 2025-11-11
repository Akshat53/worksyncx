package com.worksyncx.hrms.dto.shift;

import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ShiftRequest {

    @NotBlank(message = "Shift name is required")
    @Size(max = 100, message = "Name must not exceed 100 characters")
    private String name;

    @NotBlank(message = "Shift code is required")
    @Pattern(regexp = "^[A-Z_]+$", message = "Code must contain only uppercase letters and underscores")
    @Size(max = 50, message = "Code must not exceed 50 characters")
    private String code;

    @NotNull(message = "Start time is required")
    private LocalTime startTime;

    @NotNull(message = "End time is required")
    private LocalTime endTime;

    @Min(value = 0, message = "Grace period must be at least 0 minutes")
    @Max(value = 120, message = "Grace period cannot exceed 120 minutes")
    private Integer gracePeriodMinutes = 15;

    @DecimalMin(value = "0.0", message = "Half day hours must be positive")
    @DecimalMax(value = "24.0", message = "Half day hours cannot exceed 24")
    private BigDecimal halfDayHours = BigDecimal.valueOf(4.0);

    @DecimalMin(value = "0.0", message = "Full day hours must be positive")
    @DecimalMax(value = "24.0", message = "Full day hours cannot exceed 24")
    private BigDecimal fullDayHours = BigDecimal.valueOf(8.0);

    @Pattern(regexp = "^#([A-Fa-f0-9]{6}|[A-Fa-f0-9]{3})$", message = "Color must be a valid hex color code")
    private String color = "#3B82F6";

    @Size(max = 1000, message = "Description must not exceed 1000 characters")
    private String description;

    private Boolean isActive = true;
}
