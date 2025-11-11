package com.worksyncx.hrms.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Set;

@Entity
@Table(name = "plans")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Plan {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true, length = 100)
    private String name;

    @Column(length = 500)
    private String description;

    @Column(name = "short_description", length = 200)
    private String shortDescription;

    @Column(name = "monthly_price", precision = 12, scale = 2, nullable = false)
    private BigDecimal monthlyPrice;

    @Column(name = "yearly_price", precision = 12, scale = 2, nullable = false)
    private BigDecimal yearlyPrice;

    @Column(name = "max_employees")
    private Integer maxEmployees;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(columnDefinition = "jsonb")
    private Set<String> modules;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(columnDefinition = "jsonb")
    private Set<String> features;

    @Column(name = "is_active", nullable = false)
    private Boolean isActive = true;

    @Column(name = "is_popular", nullable = false)
    private Boolean isPopular = false;

    @Column(name = "display_order")
    private Integer displayOrder;

    @Column(name = "badge_text", length = 50)
    private String badgeText;

    @Column(name = "badge_color", length = 50)
    private String badgeColor;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;
}
