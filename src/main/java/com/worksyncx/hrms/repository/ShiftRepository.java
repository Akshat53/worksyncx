package com.worksyncx.hrms.repository;

import com.worksyncx.hrms.entity.Shift;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ShiftRepository extends JpaRepository<Shift, Long> {

    // Find by tenant (excluding soft-deleted)
    List<Shift> findByTenantIdAndIsDeleted(Long tenantId, Boolean isDeleted);

    default List<Shift> findByTenantId(Long tenantId) {
        return findByTenantIdAndIsDeleted(tenantId, false);
    }

    List<Shift> findByTenantIdAndIsActiveAndIsDeleted(Long tenantId, Boolean isActive, Boolean isDeleted);

    default List<Shift> findByTenantIdAndIsActive(Long tenantId, Boolean isActive) {
        return findByTenantIdAndIsActiveAndIsDeleted(tenantId, isActive, false);
    }

    // Find by code
    Optional<Shift> findByTenantIdAndCode(Long tenantId, String code);

    boolean existsByTenantIdAndCodeAndIsDeleted(Long tenantId, String code, Boolean isDeleted);

    default boolean existsByTenantIdAndCodeExcludingDeleted(Long tenantId, String code) {
        return existsByTenantIdAndCodeAndIsDeleted(tenantId, code, false);
    }

    // Find by name
    Optional<Shift> findByTenantIdAndName(Long tenantId, String name);

    // Find default shift (excluding soft-deleted)
    @Query("SELECT s FROM Shift s WHERE s.tenantId = :tenantId AND s.code = 'REGULAR' AND s.isActive = true AND s.isDeleted = false")
    Optional<Shift> findDefaultShift(@Param("tenantId") Long tenantId);

    // Count active shifts for tenant
    long countByTenantIdAndIsActive(Long tenantId, Boolean isActive);

    // Find all active shifts ordered by name (excluding soft-deleted)
    @Query("SELECT s FROM Shift s WHERE s.tenantId = :tenantId AND s.isActive = true AND s.isDeleted = false ORDER BY s.name ASC")
    List<Shift> findActiveShiftsByTenantIdOrderedByName(@Param("tenantId") Long tenantId);

    // Search shifts by name or code (excluding soft-deleted)
    @Query("SELECT s FROM Shift s WHERE s.tenantId = :tenantId AND s.isDeleted = false AND " +
           "(LOWER(s.name) LIKE LOWER(CONCAT('%', :search, '%')) OR " +
           "LOWER(s.code) LIKE LOWER(CONCAT('%', :search, '%')))")
    List<Shift> searchShifts(@Param("tenantId") Long tenantId, @Param("search") String search);

    // Paginated methods (excluding soft-deleted)
    Page<Shift> findByTenantIdAndIsDeleted(Long tenantId, Boolean isDeleted, Pageable pageable);

    default Page<Shift> findByTenantId(Long tenantId, Pageable pageable) {
        return findByTenantIdAndIsDeleted(tenantId, false, pageable);
    }

    Page<Shift> findByTenantIdAndIsActiveAndIsDeleted(Long tenantId, Boolean isActive, Boolean isDeleted, Pageable pageable);

    default Page<Shift> findByTenantIdAndIsActive(Long tenantId, Boolean isActive, Pageable pageable) {
        return findByTenantIdAndIsActiveAndIsDeleted(tenantId, isActive, false, pageable);
    }
}
