package com.worksyncx.hrms.repository;

import com.worksyncx.hrms.entity.LeaveType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface LeaveTypeRepository extends JpaRepository<LeaveType, Long> {
    List<LeaveType> findByTenantId(Long tenantId);
    List<LeaveType> findByTenantIdAndIsActiveTrue(Long tenantId);
    Optional<LeaveType> findByTenantIdAndId(Long tenantId, Long id);
}
