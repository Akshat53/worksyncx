package com.worksyncx.hrms.repository;

import com.worksyncx.hrms.entity.Employee;
import com.worksyncx.hrms.enums.EmploymentStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface EmployeeRepository extends JpaRepository<Employee, Long> {
    // Performance optimization: Count employees without loading all entities
    @Query("SELECT COUNT(e) FROM Employee e WHERE e.tenantId = :tenantId")
    long countByTenantId(@Param("tenantId") Long tenantId);

    @Query("SELECT COUNT(e) FROM Employee e WHERE e.tenantId = :tenantId AND e.employmentStatus = :status")
    long countByTenantIdAndEmploymentStatus(@Param("tenantId") Long tenantId, @Param("status") EmploymentStatus status);


    // Non-paginated methods
    List<Employee> findByTenantId(Long tenantId);
    List<Employee> findByTenantIdAndEmploymentStatus(Long tenantId, EmploymentStatus status);
    List<Employee> findByTenantIdAndDepartmentId(Long tenantId, Long departmentId);
    Optional<Employee> findByTenantIdAndId(Long tenantId, Long id);
    Optional<Employee> findByTenantIdAndEmployeeCode(Long tenantId, String employeeCode);
    Optional<Employee> findByTenantIdAndUserId(Long tenantId, Long userId);
    Optional<Employee> findByTenantIdAndEmail(Long tenantId, String email);

    // Paginated methods
    Page<Employee> findByTenantId(Long tenantId, Pageable pageable);
    Page<Employee> findByTenantIdAndEmploymentStatus(Long tenantId, EmploymentStatus status, Pageable pageable);
    Page<Employee> findByTenantIdAndDepartmentId(Long tenantId, Long departmentId, Pageable pageable);

    // Manager relationship methods
    List<Employee> findByTenantIdAndManagerId(Long tenantId, Long managerId);

    @Query("SELECT COUNT(e) FROM Employee e WHERE e.tenantId = :tenantId AND e.managerId = :managerId")
    long countByTenantIdAndManagerId(@Param("tenantId") Long tenantId, @Param("managerId") Long managerId);
}
