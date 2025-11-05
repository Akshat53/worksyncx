package com.worksyncx.hrms.repository;

import com.worksyncx.hrms.entity.Role;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface RoleRepository extends JpaRepository<Role, Long> {
    Optional<Role> findByTenantIdAndName(Long tenantId, String name);

    Optional<Role> findByIdAndTenantId(Long id, Long tenantId);

    List<Role> findByTenantId(Long tenantId);

    List<Role> findByIsSystemRoleTrue();

    boolean existsByTenantIdAndName(Long tenantId, String name);
}
