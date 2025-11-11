package com.worksyncx.hrms.repository;

import com.worksyncx.hrms.entity.Payroll;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface PayrollRepository extends JpaRepository<Payroll, Long> {
    List<Payroll> findByTenantId(Long tenantId);
    List<Payroll> findByTenantIdAndPayrollCycleId(Long tenantId, Long cycleId);
    List<Payroll> findByTenantIdAndEmployeeId(Long tenantId, Long employeeId);
    Optional<Payroll> findByTenantIdAndId(Long tenantId, Long id);

    // Paginated methods
    Page<Payroll> findByTenantId(Long tenantId, Pageable pageable);
    Page<Payroll> findByTenantIdAndPayrollCycleId(Long tenantId, Long cycleId, Pageable pageable);
    Page<Payroll> findByTenantIdAndEmployeeId(Long tenantId, Long employeeId, Pageable pageable);
}
