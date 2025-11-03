package com.worksyncx.hrms.service.employee;

import com.worksyncx.hrms.dto.employee.EmployeeRequest;
import com.worksyncx.hrms.dto.employee.EmployeeResponse;
import com.worksyncx.hrms.entity.Employee;
import com.worksyncx.hrms.enums.EmploymentStatus;
import com.worksyncx.hrms.repository.DepartmentRepository;
import com.worksyncx.hrms.repository.DesignationRepository;
import com.worksyncx.hrms.repository.EmployeeRepository;
import com.worksyncx.hrms.security.TenantContext;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class EmployeeService {

    private final EmployeeRepository employeeRepository;
    private final DepartmentRepository departmentRepository;
    private final DesignationRepository designationRepository;

    @Transactional
    public EmployeeResponse createEmployee(EmployeeRequest request) {
        Long tenantId = TenantContext.getTenantId();

        // Check if employee code already exists
        employeeRepository.findByTenantIdAndEmployeeCode(tenantId, request.getEmployeeCode())
            .ifPresent(emp -> {
                throw new RuntimeException("Employee with code " + request.getEmployeeCode() + " already exists");
            });

        // Verify department exists
        departmentRepository.findByTenantIdAndId(tenantId, request.getDepartmentId())
            .orElseThrow(() -> new RuntimeException("Department not found with id: " + request.getDepartmentId()));

        // Verify designation exists
        designationRepository.findByTenantIdAndId(tenantId, request.getDesignationId())
            .orElseThrow(() -> new RuntimeException("Designation not found with id: " + request.getDesignationId()));

        // Verify manager exists if provided
        if (request.getManagerId() != null) {
            employeeRepository.findByTenantIdAndId(tenantId, request.getManagerId())
                .orElseThrow(() -> new RuntimeException("Manager not found with id: " + request.getManagerId()));
        }

        Employee employee = new Employee();
        employee.setTenantId(tenantId);
        mapRequestToEntity(request, employee);
        employee.setCreatedBy(TenantContext.getUserId());

        employee = employeeRepository.save(employee);
        return mapToResponse(employee);
    }

    @Transactional(readOnly = true)
    public List<EmployeeResponse> getAllEmployees() {
        Long tenantId = TenantContext.getTenantId();
        return employeeRepository.findByTenantId(tenantId)
            .stream()
            .map(this::mapToResponse)
            .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<EmployeeResponse> getEmployeesByStatus(EmploymentStatus status) {
        Long tenantId = TenantContext.getTenantId();
        return employeeRepository.findByTenantIdAndEmploymentStatus(tenantId, status)
            .stream()
            .map(this::mapToResponse)
            .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<EmployeeResponse> getEmployeesByDepartment(Long departmentId) {
        Long tenantId = TenantContext.getTenantId();
        return employeeRepository.findByTenantIdAndDepartmentId(tenantId, departmentId)
            .stream()
            .map(this::mapToResponse)
            .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public EmployeeResponse getEmployeeById(Long id) {
        Long tenantId = TenantContext.getTenantId();
        Employee employee = employeeRepository.findByTenantIdAndId(tenantId, id)
            .orElseThrow(() -> new RuntimeException("Employee not found with id: " + id));
        return mapToResponse(employee);
    }

    @Transactional(readOnly = true)
    public EmployeeResponse getEmployeeByCode(String employeeCode) {
        Long tenantId = TenantContext.getTenantId();
        Employee employee = employeeRepository.findByTenantIdAndEmployeeCode(tenantId, employeeCode)
            .orElseThrow(() -> new RuntimeException("Employee not found with code: " + employeeCode));
        return mapToResponse(employee);
    }

    @Transactional
    public EmployeeResponse updateEmployee(Long id, EmployeeRequest request) {
        Long tenantId = TenantContext.getTenantId();

        Employee employee = employeeRepository.findByTenantIdAndId(tenantId, id)
            .orElseThrow(() -> new RuntimeException("Employee not found with id: " + id));

        // Check if employee code is being changed and if new code already exists
        if (!employee.getEmployeeCode().equals(request.getEmployeeCode())) {
            employeeRepository.findByTenantIdAndEmployeeCode(tenantId, request.getEmployeeCode())
                .ifPresent(emp -> {
                    throw new RuntimeException("Employee with code " + request.getEmployeeCode() + " already exists");
                });
        }

        // Verify department exists
        departmentRepository.findByTenantIdAndId(tenantId, request.getDepartmentId())
            .orElseThrow(() -> new RuntimeException("Department not found with id: " + request.getDepartmentId()));

        // Verify designation exists
        designationRepository.findByTenantIdAndId(tenantId, request.getDesignationId())
            .orElseThrow(() -> new RuntimeException("Designation not found with id: " + request.getDesignationId()));

        // Verify manager exists if provided
        if (request.getManagerId() != null) {
            employeeRepository.findByTenantIdAndId(tenantId, request.getManagerId())
                .orElseThrow(() -> new RuntimeException("Manager not found with id: " + request.getManagerId()));
        }

        mapRequestToEntity(request, employee);
        employee.setUpdatedBy(TenantContext.getUserId());

        employee = employeeRepository.save(employee);
        return mapToResponse(employee);
    }

    @Transactional
    public void deleteEmployee(Long id) {
        Long tenantId = TenantContext.getTenantId();

        Employee employee = employeeRepository.findByTenantIdAndId(tenantId, id)
            .orElseThrow(() -> new RuntimeException("Employee not found with id: " + id));

        employeeRepository.delete(employee);
    }

    private void mapRequestToEntity(EmployeeRequest request, Employee employee) {
        employee.setUserId(request.getUserId());
        employee.setEmployeeCode(request.getEmployeeCode());
        employee.setFirstName(request.getFirstName());
        employee.setLastName(request.getLastName());
        employee.setEmail(request.getEmail());
        employee.setPhone(request.getPhone());
        employee.setDateOfBirth(request.getDateOfBirth());
        employee.setGender(request.getGender());
        employee.setNationality(request.getNationality());
        employee.setDepartmentId(request.getDepartmentId());
        employee.setDesignationId(request.getDesignationId());
        employee.setManagerId(request.getManagerId());
        employee.setDateOfJoining(request.getDateOfJoining());
        employee.setDateOfLeaving(request.getDateOfLeaving());
        employee.setEmploymentType(request.getEmploymentType());
        employee.setEmploymentStatus(request.getEmploymentStatus() != null ?
            request.getEmploymentStatus() : EmploymentStatus.ACTIVE);
        employee.setBasicSalary(request.getBasicSalary());
        employee.setCurrency(request.getCurrency() != null ? request.getCurrency() : "USD");
        employee.setAddress(request.getAddress());
        employee.setCity(request.getCity());
        employee.setState(request.getState());
        employee.setCountry(request.getCountry());
        employee.setPostalCode(request.getPostalCode());
        employee.setEmergencyContactName(request.getEmergencyContactName());
        employee.setEmergencyContactPhone(request.getEmergencyContactPhone());
        employee.setEmergencyContactRelation(request.getEmergencyContactRelation());
        employee.setBankName(request.getBankName());
        employee.setBankAccount(request.getBankAccount());
        employee.setIfscCode(request.getIfscCode());
        employee.setPan(request.getPan());
    }

    private EmployeeResponse mapToResponse(Employee employee) {
        return EmployeeResponse.builder()
            .id(employee.getId())
            .tenantId(employee.getTenantId())
            .userId(employee.getUserId())
            .employeeCode(employee.getEmployeeCode())
            .firstName(employee.getFirstName())
            .lastName(employee.getLastName())
            .email(employee.getEmail())
            .phone(employee.getPhone())
            .dateOfBirth(employee.getDateOfBirth())
            .gender(employee.getGender())
            .nationality(employee.getNationality())
            .departmentId(employee.getDepartmentId())
            .designationId(employee.getDesignationId())
            .managerId(employee.getManagerId())
            .dateOfJoining(employee.getDateOfJoining())
            .dateOfLeaving(employee.getDateOfLeaving())
            .employmentType(employee.getEmploymentType())
            .employmentStatus(employee.getEmploymentStatus())
            .basicSalary(employee.getBasicSalary())
            .currency(employee.getCurrency())
            .address(employee.getAddress())
            .city(employee.getCity())
            .state(employee.getState())
            .country(employee.getCountry())
            .postalCode(employee.getPostalCode())
            .emergencyContactName(employee.getEmergencyContactName())
            .emergencyContactPhone(employee.getEmergencyContactPhone())
            .emergencyContactRelation(employee.getEmergencyContactRelation())
            .bankName(employee.getBankName())
            .bankAccount(employee.getBankAccount())
            .ifscCode(employee.getIfscCode())
            .pan(employee.getPan())
            .createdAt(employee.getCreatedAt())
            .updatedAt(employee.getUpdatedAt())
            .createdBy(employee.getCreatedBy())
            .updatedBy(employee.getUpdatedBy())
            .build();
    }
}
