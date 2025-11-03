package com.worksyncx.hrms.entity;

import com.worksyncx.hrms.entity.base.BaseEntity;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Entity
@Table(name = "designations", uniqueConstraints = {
    @UniqueConstraint(columnNames = {"tenant_id", "code"})
})
@Data
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = true)
public class Designation extends BaseEntity {

    @Column(nullable = false, length = 100)
    private String name;

    @Column(nullable = false, length = 50)
    private String code;

    @Column(columnDefinition = "TEXT")
    private String description;

    @Column(name = "department_id", nullable = false)
    private Long departmentId;

    @Column(name = "salary_range_min", precision = 12, scale = 2)
    private BigDecimal salaryRangeMin;

    @Column(name = "salary_range_max", precision = 12, scale = 2)
    private BigDecimal salaryRangeMax;

    @Column(length = 50)
    private String level;

    @Column(name = "is_active", nullable = false)
    private Boolean isActive = true;
}
