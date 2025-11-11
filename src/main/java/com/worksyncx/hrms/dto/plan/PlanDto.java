package com.worksyncx.hrms.dto.plan;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Set;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PlanDto {
    private Long id;
    private String name;
    private String description;
    private String shortDescription;
    private BigDecimal monthlyPrice;
    private BigDecimal yearlyPrice;
    private Integer maxEmployees;
    private Set<String> modules;
    private Set<String> features;
    private Boolean isActive;
    private Boolean isPopular;
    private Integer displayOrder;
    private String badgeText;
    private String badgeColor;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
