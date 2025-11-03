package com.worksyncx.hrms.entity;

import com.worksyncx.hrms.entity.base.BaseEntity;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Entity
@Table(name = "leave_types", uniqueConstraints = {
    @UniqueConstraint(columnNames = {"tenant_id", "code"})
})
@Data
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = true)
public class LeaveType extends BaseEntity {

    @Column(nullable = false, length = 100)
    private String name;

    @Column(nullable = false, length = 50)
    private String code;

    @Column(name = "days_per_year", precision = 5, scale = 2)
    private BigDecimal daysPerYear;

    @Column(name = "is_paid", nullable = false)
    private Boolean isPaid = true;

    @Column(name = "requires_approval", nullable = false)
    private Boolean requiresApproval = true;

    @Column(name = "color_code", length = 10)
    private String colorCode;

    @Column(name = "is_active", nullable = false)
    private Boolean isActive = true;
}
