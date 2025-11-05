package com.worksyncx.hrms.repository;

import com.worksyncx.hrms.entity.AttendanceRecord;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository
public interface AttendanceRecordRepository extends JpaRepository<AttendanceRecord, Long> {
    List<AttendanceRecord> findByTenantIdAndEmployeeId(Long tenantId, Long employeeId);
    List<AttendanceRecord> findByTenantIdAndEmployeeIdAndAttendanceDateBetween(
        Long tenantId, Long employeeId, LocalDate startDate, LocalDate endDate
    );
    Optional<AttendanceRecord> findByTenantIdAndEmployeeIdAndAttendanceDate(
        Long tenantId, Long employeeId, LocalDate date
    );
    List<AttendanceRecord> findByTenantIdAndAttendanceDate(Long tenantId, LocalDate date);
}
