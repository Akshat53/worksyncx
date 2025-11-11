package com.worksyncx.hrms.dto.plan;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.Set;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CreatePlanRequest {

    @NotBlank(message = "Plan name is required")
    @Size(max = 100, message = "Plan name cannot exceed 100 characters")
    private String name;

    @Size(max = 500, message = "Description cannot exceed 500 characters")
    private String description;

    @Size(max = 200, message = "Short description cannot exceed 200 characters")
    private String shortDescription;

    @NotNull(message = "Monthly price is required")
    private BigDecimal monthlyPrice;

    @NotNull(message = "Yearly price is required")
    private BigDecimal yearlyPrice;

    private Integer maxEmployees;
    private Set<String> modules;
    private Set<String> features;
    private Boolean isActive;
    private Boolean isPopular;
    private Integer displayOrder;

    @Size(max = 50, message = "Badge text cannot exceed 50 characters")
    private String badgeText;

    @Size(max = 50, message = "Badge color cannot exceed 50 characters")
    private String badgeColor;
}
