package com.worksyncx.hrms.repository;

import com.worksyncx.hrms.entity.LeaveRequest;
import com.worksyncx.hrms.enums.LeaveStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface LeaveRequestRepository extends JpaRepository<LeaveRequest, Long> {
    List<LeaveRequest> findByTenantId(Long tenantId);
    List<LeaveRequest> findByTenantIdAndEmployeeId(Long tenantId, Long employeeId);
    List<LeaveRequest> findByTenantIdAndStatus(Long tenantId, LeaveStatus status);
    Optional<LeaveRequest> findByTenantIdAndId(Long tenantId, Long id);

    // Paginated methods
    Page<LeaveRequest> findByTenantId(Long tenantId, Pageable pageable);
    Page<LeaveRequest> findByTenantIdAndEmployeeId(Long tenantId, Long employeeId, Pageable pageable);
    Page<LeaveRequest> findByTenantIdAndStatus(Long tenantId, LeaveStatus status, Pageable pageable);
}
