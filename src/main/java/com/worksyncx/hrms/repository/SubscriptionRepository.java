package com.worksyncx.hrms.repository;

import com.worksyncx.hrms.entity.Subscription;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface SubscriptionRepository extends JpaRepository<Subscription, Long> {
    @Query("SELECT s FROM Subscription s WHERE s.tenant.id = :tenantId")
    Optional<Subscription> findByTenantId(Long tenantId);
}
