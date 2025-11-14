package com.worksyncx.hrms.repository;

import com.worksyncx.hrms.entity.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {
    // Fetch user with roles and permissions for authentication
    @EntityGraph(attributePaths = {"roles", "roles.permissions"})
    Optional<User> findWithRolesAndPermissionsByEmail(String email);

    Optional<User> findByEmail(String email);
    Optional<User> findByTenantIdAndEmail(Long tenantId, String email);
    List<User> findByTenantId(Long tenantId);
    Boolean existsByEmail(String email);
    Boolean existsByTenantIdAndEmail(Long tenantId, String email);

    // Paginated method
    Page<User> findByTenantId(Long tenantId, Pageable pageable);

    @Query("SELECT COUNT(u) FROM User u JOIN u.roles r WHERE r.id = :roleId")
    long countUsersWithRole(@Param("roleId") Long roleId);
}
