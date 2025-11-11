package com.worksyncx.hrms.repository;

import com.worksyncx.hrms.entity.EmployeeShift;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository
public interface EmployeeShiftRepository extends JpaRepository<EmployeeShift, Long> {

    // Find by employee
    List<EmployeeShift> findByEmployeeId(Long employeeId);

    List<EmployeeShift> findByEmployeeIdAndIsActive(Long employeeId, Boolean isActive);

    // Find by shift
    List<EmployeeShift> findByShiftId(Long shiftId);

    List<EmployeeShift> findByShiftIdAndIsActive(Long shiftId, Boolean isActive);

    // Find current active shift for employee
    @Query("SELECT es FROM EmployeeShift es WHERE es.employeeId = :employeeId " +
           "AND es.isActive = true " +
           "AND :date >= es.effectiveFrom " +
           "AND (es.effectiveTo IS NULL OR :date <= es.effectiveTo) " +
           "ORDER BY es.effectiveFrom DESC")
    List<EmployeeShift> findActiveShiftForEmployeeOnDate(
        @Param("employeeId") Long employeeId,
        @Param("date") LocalDate date
    );

    // Find current active shift for employee with day filter
    @Query(value = "SELECT * FROM employee_shifts es " +
           "WHERE es.employee_id = :employeeId " +
           "AND es.is_active = true " +
           "AND CAST(:date AS DATE) >= es.effective_from " +
           "AND (es.effective_to IS NULL OR CAST(:date AS DATE) <= es.effective_to) " +
           "AND :dayOfWeek = ANY(es.days_of_week) " +
           "ORDER BY es.effective_from DESC " +
           "LIMIT 1",
           nativeQuery = true)
    Optional<EmployeeShift> findActiveShiftForEmployeeOnDateAndDay(
        @Param("employeeId") Long employeeId,
        @Param("date") LocalDate date,
        @Param("dayOfWeek") String dayOfWeek
    );

    // Find all ongoing shifts (effectiveTo is null)
    @Query("SELECT es FROM EmployeeShift es WHERE es.employeeId = :employeeId " +
           "AND es.isActive = true AND es.effectiveTo IS NULL")
    List<EmployeeShift> findOngoingShiftsByEmployeeId(@Param("employeeId") Long employeeId);

    // Find overlapping shift assignments for an employee
    @Query("SELECT es FROM EmployeeShift es WHERE es.employeeId = :employeeId " +
           "AND es.isActive = true " +
           "AND es.id != :excludeId " +
           "AND ((es.effectiveFrom <= :effectiveTo AND (es.effectiveTo IS NULL OR es.effectiveTo >= :effectiveFrom)))")
    List<EmployeeShift> findOverlappingShifts(
        @Param("employeeId") Long employeeId,
        @Param("effectiveFrom") LocalDate effectiveFrom,
        @Param("effectiveTo") LocalDate effectiveTo,
        @Param("excludeId") Long excludeId
    );

    // Count employees assigned to a shift
    long countByShiftIdAndIsActive(Long shiftId, Boolean isActive);

    // Find all employees on a specific shift
    @Query("SELECT es FROM EmployeeShift es WHERE es.shiftId = :shiftId " +
           "AND es.isActive = true " +
           "AND :date >= es.effectiveFrom " +
           "AND (es.effectiveTo IS NULL OR :date <= es.effectiveTo)")
    List<EmployeeShift> findEmployeesOnShiftOnDate(
        @Param("shiftId") Long shiftId,
        @Param("date") LocalDate date
    );

    // Find future shift assignments for employee
    @Query("SELECT es FROM EmployeeShift es WHERE es.employeeId = :employeeId " +
           "AND es.effectiveFrom > :currentDate " +
           "ORDER BY es.effectiveFrom ASC")
    List<EmployeeShift> findFutureShiftAssignments(
        @Param("employeeId") Long employeeId,
        @Param("currentDate") LocalDate currentDate
    );

    // Find expired shift assignments
    @Query("SELECT es FROM EmployeeShift es WHERE es.employeeId = :employeeId " +
           "AND es.effectiveTo IS NOT NULL " +
           "AND es.effectiveTo < :currentDate " +
           "ORDER BY es.effectiveTo DESC")
    List<EmployeeShift> findExpiredShiftAssignments(
        @Param("employeeId") Long employeeId,
        @Param("currentDate") LocalDate currentDate
    );

    // Find shift history for employee
    @Query("SELECT es FROM EmployeeShift es WHERE es.employeeId = :employeeId " +
           "ORDER BY es.effectiveFrom DESC")
    List<EmployeeShift> findShiftHistory(@Param("employeeId") Long employeeId);

    // Check if employee has any active shift
    boolean existsByEmployeeIdAndIsActive(Long employeeId, Boolean isActive);

    // Deactivate all active shifts for an employee
    @Query("UPDATE EmployeeShift es SET es.isActive = false WHERE es.employeeId = :employeeId AND es.isActive = true")
    void deactivateAllShiftsForEmployee(@Param("employeeId") Long employeeId);
}
