package com.worksyncx.hrms.repository;

import com.worksyncx.hrms.entity.Department;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface DepartmentRepository extends JpaRepository<Department, Long> {
    List<Department> findByTenantId(Long tenantId);
    List<Department> findByTenantIdAndIsActiveTrue(Long tenantId);
    Optional<Department> findByTenantIdAndCode(Long tenantId, String code);
    Optional<Department> findByTenantIdAndId(Long tenantId, Long id);
}
