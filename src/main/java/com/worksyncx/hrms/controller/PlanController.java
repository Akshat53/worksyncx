package com.worksyncx.hrms.controller;

import com.worksyncx.hrms.dto.plan.CreatePlanRequest;
import com.worksyncx.hrms.dto.plan.PlanDto;
import com.worksyncx.hrms.dto.plan.UpdatePlanRequest;
import com.worksyncx.hrms.service.plan.PlanService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/plans")
@RequiredArgsConstructor
@Slf4j
@CrossOrigin(origins = {"http://localhost:3000", "http://localhost:3001", "http://localhost:3002"})
public class PlanController {

    private final PlanService planService;

    /**
     * Get all plans (Super Admin only)
     */
    @GetMapping
    @PreAuthorize("hasRole('SUPER_ADMIN')")
    public ResponseEntity<List<PlanDto>> getAllPlans() {
        log.info("GET /api/plans - Fetching all plans");
        List<PlanDto> plans = planService.getAllPlans();
        return ResponseEntity.ok(plans);
    }

    /**
     * Get active plans (Public - for pricing page)
     */
    @GetMapping("/active")
    public ResponseEntity<List<PlanDto>> getActivePlans() {
        log.info("GET /api/plans/active - Fetching active plans");
        List<PlanDto> plans = planService.getActivePlans();
        return ResponseEntity.ok(plans);
    }

    /**
     * Get plan by ID (Super Admin only)
     */
    @GetMapping("/{id}")
    @PreAuthorize("hasRole('SUPER_ADMIN')")
    public ResponseEntity<PlanDto> getPlanById(@PathVariable Long id) {
        log.info("GET /api/plans/{} - Fetching plan", id);
        PlanDto plan = planService.getPlanById(id);
        return ResponseEntity.ok(plan);
    }

    /**
     * Create new plan (Super Admin only)
     */
    @PostMapping
    @PreAuthorize("hasRole('SUPER_ADMIN')")
    public ResponseEntity<PlanDto> createPlan(@Valid @RequestBody CreatePlanRequest request) {
        log.info("POST /api/plans - Creating new plan: {}", request.getName());
        PlanDto plan = planService.createPlan(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(plan);
    }

    /**
     * Update plan (Super Admin only)
     */
    @PutMapping("/{id}")
    @PreAuthorize("hasRole('SUPER_ADMIN')")
    public ResponseEntity<PlanDto> updatePlan(
            @PathVariable Long id,
            @Valid @RequestBody UpdatePlanRequest request) {
        log.info("PUT /api/plans/{} - Updating plan", id);
        PlanDto plan = planService.updatePlan(id, request);
        return ResponseEntity.ok(plan);
    }

    /**
     * Delete plan (Super Admin only)
     */
    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('SUPER_ADMIN')")
    public ResponseEntity<Void> deletePlan(@PathVariable Long id) {
        log.info("DELETE /api/plans/{} - Deleting plan", id);
        planService.deletePlan(id);
        return ResponseEntity.noContent().build();
    }
}
