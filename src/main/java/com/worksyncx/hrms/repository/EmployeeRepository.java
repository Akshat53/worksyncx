package com.worksyncx.hrms.repository;

import com.worksyncx.hrms.entity.Employee;
import com.worksyncx.hrms.enums.EmploymentStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface EmployeeRepository extends JpaRepository<Employee, Long> {
    List<Employee> findByTenantId(Long tenantId);
    List<Employee> findByTenantIdAndEmploymentStatus(Long tenantId, EmploymentStatus status);
    List<Employee> findByTenantIdAndDepartmentId(Long tenantId, Long departmentId);
    Optional<Employee> findByTenantIdAndId(Long tenantId, Long id);
    Optional<Employee> findByTenantIdAndEmployeeCode(Long tenantId, String employeeCode);
    Optional<Employee> findByTenantIdAndUserId(Long tenantId, Long userId);
    Optional<Employee> findByTenantIdAndEmail(Long tenantId, String email);
}
