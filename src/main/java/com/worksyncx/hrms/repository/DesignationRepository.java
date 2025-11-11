package com.worksyncx.hrms.repository;

import com.worksyncx.hrms.entity.Designation;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface DesignationRepository extends JpaRepository<Designation, Long> {
    List<Designation> findByTenantId(Long tenantId);
    List<Designation> findByTenantIdAndIsActiveTrue(Long tenantId);
    List<Designation> findByTenantIdAndDepartmentId(Long tenantId, Long departmentId);
    Optional<Designation> findByTenantIdAndId(Long tenantId, Long id);
    Optional<Designation> findByTenantIdAndCode(Long tenantId, String code);

    // Paginated methods
    Page<Designation> findByTenantId(Long tenantId, Pageable pageable);
    Page<Designation> findByTenantIdAndIsActiveTrue(Long tenantId, Pageable pageable);
    Page<Designation> findByTenantIdAndDepartmentId(Long tenantId, Long departmentId, Pageable pageable);
}
