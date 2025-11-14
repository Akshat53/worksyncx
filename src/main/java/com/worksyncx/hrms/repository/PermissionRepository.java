package com.worksyncx.hrms.repository;

import com.worksyncx.hrms.entity.Permission;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface PermissionRepository extends JpaRepository<Permission, Long> {

    boolean existsByCode(String code);

    Optional<Permission> findByCode(String code);

    List<Permission> findByModule(String module);

    List<Permission> findByModuleAndAction(String module, String action);

    @Query("SELECT COUNT(r) FROM Role r JOIN r.permissions p WHERE p.id = :permissionId")
    long countRolesUsingPermission(@Param("permissionId") Long permissionId);
}
