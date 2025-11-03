package com.worksyncx.hrms.repository;

import com.worksyncx.hrms.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {
    Optional<User> findByEmail(String email);
    Optional<User> findByTenantIdAndEmail(Long tenantId, String email);
    List<User> findByTenantId(Long tenantId);
    Boolean existsByEmail(String email);
    Boolean existsByTenantIdAndEmail(Long tenantId, String email);
}
