package com.worksyncx.hrms.repository;

import com.worksyncx.hrms.entity.PayrollCycle;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface PayrollCycleRepository extends JpaRepository<PayrollCycle, Long> {
    List<PayrollCycle> findByTenantId(Long tenantId);
    Optional<PayrollCycle> findByTenantIdAndId(Long tenantId, Long id);
    Optional<PayrollCycle> findByTenantIdAndMonthAndYear(Long tenantId, Integer month, Integer year);

    // Paginated methods
    Page<PayrollCycle> findByTenantId(Long tenantId, Pageable pageable);
}
