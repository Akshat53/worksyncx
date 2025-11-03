package com.worksyncx.hrms.dto.employee;

import com.worksyncx.hrms.enums.EmploymentStatus;
import com.worksyncx.hrms.enums.EmploymentType;
import com.worksyncx.hrms.enums.Gender;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;

@Data
public class EmployeeRequest {

    private Long userId;

    @NotBlank(message = "Employee code is required")
    @Size(max = 50, message = "Employee code cannot exceed 50 characters")
    private String employeeCode;

    @NotBlank(message = "First name is required")
    @Size(max = 100, message = "First name cannot exceed 100 characters")
    private String firstName;

    @NotBlank(message = "Last name is required")
    @Size(max = 100, message = "Last name cannot exceed 100 characters")
    private String lastName;

    @NotBlank(message = "Email is required")
    @Email(message = "Email should be valid")
    @Size(max = 255, message = "Email cannot exceed 255 characters")
    private String email;

    @Size(max = 20, message = "Phone cannot exceed 20 characters")
    private String phone;

    private LocalDate dateOfBirth;

    private Gender gender;

    @Size(max = 50, message = "Nationality cannot exceed 50 characters")
    private String nationality;

    @NotNull(message = "Department ID is required")
    private Long departmentId;

    @NotNull(message = "Designation ID is required")
    private Long designationId;

    private Long managerId;

    @NotNull(message = "Date of joining is required")
    private LocalDate dateOfJoining;

    private LocalDate dateOfLeaving;

    private EmploymentType employmentType;

    private EmploymentStatus employmentStatus;

    private BigDecimal basicSalary;

    @Size(max = 10, message = "Currency cannot exceed 10 characters")
    private String currency;

    @Size(max = 255, message = "Address cannot exceed 255 characters")
    private String address;

    @Size(max = 100, message = "City cannot exceed 100 characters")
    private String city;

    @Size(max = 100, message = "State cannot exceed 100 characters")
    private String state;

    @Size(max = 100, message = "Country cannot exceed 100 characters")
    private String country;

    @Size(max = 20, message = "Postal code cannot exceed 20 characters")
    private String postalCode;

    @Size(max = 100, message = "Emergency contact name cannot exceed 100 characters")
    private String emergencyContactName;

    @Size(max = 20, message = "Emergency contact phone cannot exceed 20 characters")
    private String emergencyContactPhone;

    @Size(max = 50, message = "Emergency contact relation cannot exceed 50 characters")
    private String emergencyContactRelation;

    @Size(max = 100, message = "Bank name cannot exceed 100 characters")
    private String bankName;

    @Size(max = 50, message = "Bank account cannot exceed 50 characters")
    private String bankAccount;

    @Size(max = 20, message = "IFSC code cannot exceed 20 characters")
    private String ifscCode;

    @Size(max = 20, message = "PAN cannot exceed 20 characters")
    private String pan;
}
