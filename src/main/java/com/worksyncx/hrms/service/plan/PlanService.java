package com.worksyncx.hrms.service.plan;

import com.worksyncx.hrms.dto.plan.CreatePlanRequest;
import com.worksyncx.hrms.dto.plan.PlanDto;
import com.worksyncx.hrms.dto.plan.UpdatePlanRequest;
import com.worksyncx.hrms.entity.Plan;
import com.worksyncx.hrms.repository.PlanRepository;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class PlanService {

    private final PlanRepository planRepository;

    @Transactional(readOnly = true)
    public List<PlanDto> getAllPlans() {
        log.info("Fetching all plans");
        return planRepository.findAllByOrderByDisplayOrderAsc()
                .stream()
                .map(this::convertToDto)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<PlanDto> getActivePlans() {
        log.info("Fetching active plans");
        return planRepository.findByIsActiveTrueOrderByDisplayOrderAsc()
                .stream()
                .map(this::convertToDto)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public PlanDto getPlanById(Long id) {
        log.info("Fetching plan with id: {}", id);
        Plan plan = planRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Plan not found with id: " + id));
        return convertToDto(plan);
    }

    @Transactional
    public PlanDto createPlan(CreatePlanRequest request) {
        log.info("Creating new plan: {}", request.getName());

        // Check if plan with the same name already exists
        if (planRepository.findByName(request.getName()).isPresent()) {
            throw new IllegalArgumentException("Plan with name '" + request.getName() + "' already exists");
        }

        Plan plan = new Plan();
        plan.setName(request.getName());
        plan.setDescription(request.getDescription());
        plan.setShortDescription(request.getShortDescription());
        plan.setMonthlyPrice(request.getMonthlyPrice());
        plan.setYearlyPrice(request.getYearlyPrice());
        plan.setMaxEmployees(request.getMaxEmployees());
        plan.setModules(request.getModules());
        plan.setFeatures(request.getFeatures());
        plan.setIsActive(request.getIsActive() != null ? request.getIsActive() : true);
        plan.setIsPopular(request.getIsPopular() != null ? request.getIsPopular() : false);
        plan.setDisplayOrder(request.getDisplayOrder());
        plan.setBadgeText(request.getBadgeText());
        plan.setBadgeColor(request.getBadgeColor());

        Plan savedPlan = planRepository.save(plan);
        log.info("Plan created successfully with id: {}", savedPlan.getId());

        return convertToDto(savedPlan);
    }

    @Transactional
    public PlanDto updatePlan(Long id, UpdatePlanRequest request) {
        log.info("Updating plan with id: {}", id);

        Plan plan = planRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Plan not found with id: " + id));

        // Check if name is being changed and if the new name already exists
        if (request.getName() != null && !request.getName().equals(plan.getName())) {
            if (planRepository.findByName(request.getName()).isPresent()) {
                throw new IllegalArgumentException("Plan with name '" + request.getName() + "' already exists");
            }
            plan.setName(request.getName());
        }

        if (request.getDescription() != null) {
            plan.setDescription(request.getDescription());
        }
        if (request.getShortDescription() != null) {
            plan.setShortDescription(request.getShortDescription());
        }
        if (request.getMonthlyPrice() != null) {
            plan.setMonthlyPrice(request.getMonthlyPrice());
        }
        if (request.getYearlyPrice() != null) {
            plan.setYearlyPrice(request.getYearlyPrice());
        }
        if (request.getMaxEmployees() != null) {
            plan.setMaxEmployees(request.getMaxEmployees());
        }
        if (request.getModules() != null) {
            plan.setModules(request.getModules());
        }
        if (request.getFeatures() != null) {
            plan.setFeatures(request.getFeatures());
        }
        if (request.getIsActive() != null) {
            plan.setIsActive(request.getIsActive());
        }
        if (request.getIsPopular() != null) {
            plan.setIsPopular(request.getIsPopular());
        }
        if (request.getDisplayOrder() != null) {
            plan.setDisplayOrder(request.getDisplayOrder());
        }
        if (request.getBadgeText() != null) {
            plan.setBadgeText(request.getBadgeText());
        }
        if (request.getBadgeColor() != null) {
            plan.setBadgeColor(request.getBadgeColor());
        }

        Plan updatedPlan = planRepository.save(plan);
        log.info("Plan updated successfully with id: {}", updatedPlan.getId());

        return convertToDto(updatedPlan);
    }

    @Transactional
    public void deletePlan(Long id) {
        log.info("Deleting plan with id: {}", id);

        if (!planRepository.existsById(id)) {
            throw new EntityNotFoundException("Plan not found with id: " + id);
        }

        planRepository.deleteById(id);
        log.info("Plan deleted successfully with id: {}", id);
    }

    private PlanDto convertToDto(Plan plan) {
        return PlanDto.builder()
                .id(plan.getId())
                .name(plan.getName())
                .description(plan.getDescription())
                .shortDescription(plan.getShortDescription())
                .monthlyPrice(plan.getMonthlyPrice())
                .yearlyPrice(plan.getYearlyPrice())
                .maxEmployees(plan.getMaxEmployees())
                .modules(plan.getModules())
                .features(plan.getFeatures())
                .isActive(plan.getIsActive())
                .isPopular(plan.getIsPopular())
                .displayOrder(plan.getDisplayOrder())
                .badgeText(plan.getBadgeText())
                .badgeColor(plan.getBadgeColor())
                .createdAt(plan.getCreatedAt())
                .updatedAt(plan.getUpdatedAt())
                .build();
    }
}
