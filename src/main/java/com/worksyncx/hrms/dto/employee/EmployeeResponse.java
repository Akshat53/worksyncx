package com.worksyncx.hrms.dto.employee;

import com.worksyncx.hrms.enums.EmploymentStatus;
import com.worksyncx.hrms.enums.EmploymentType;
import com.worksyncx.hrms.enums.Gender;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class EmployeeResponse {
    private Long id;
    private Long tenantId;
    private Long userId;
    private String employeeCode;
    private String firstName;
    private String lastName;
    private String email;
    private String phone;
    private LocalDate dateOfBirth;
    private Gender gender;
    private String nationality;
    private Long departmentId;
    private Long designationId;
    private Long managerId;
    private LocalDate dateOfJoining;
    private LocalDate dateOfLeaving;
    private EmploymentType employmentType;
    private EmploymentStatus employmentStatus;
    private BigDecimal basicSalary;
    private String currency;
    private String address;
    private String city;
    private String state;
    private String country;
    private String postalCode;
    private String emergencyContactName;
    private String emergencyContactPhone;
    private String emergencyContactRelation;
    private String bankName;
    private String bankAccount;
    private String ifscCode;
    private String pan;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private Long createdBy;
    private Long updatedBy;
    private String temporaryPassword; // Only populated on creation, for admin to share with employee
}
