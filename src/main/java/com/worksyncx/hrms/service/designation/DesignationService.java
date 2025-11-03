package com.worksyncx.hrms.service.designation;

import com.worksyncx.hrms.dto.designation.DesignationRequest;
import com.worksyncx.hrms.dto.designation.DesignationResponse;
import com.worksyncx.hrms.entity.Designation;
import com.worksyncx.hrms.repository.DepartmentRepository;
import com.worksyncx.hrms.repository.DesignationRepository;
import com.worksyncx.hrms.security.TenantContext;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class DesignationService {

    private final DesignationRepository designationRepository;
    private final DepartmentRepository departmentRepository;

    @Transactional
    public DesignationResponse createDesignation(DesignationRequest request) {
        Long tenantId = TenantContext.getTenantId();

        // Verify department exists
        departmentRepository.findByTenantIdAndId(tenantId, request.getDepartmentId())
            .orElseThrow(() -> new RuntimeException("Department not found with id: " + request.getDepartmentId()));

        Designation designation = new Designation();
        designation.setTenantId(tenantId);
        designation.setName(request.getName());
        designation.setCode(request.getCode());
        designation.setDescription(request.getDescription());
        designation.setDepartmentId(request.getDepartmentId());
        designation.setSalaryRangeMin(request.getSalaryRangeMin());
        designation.setSalaryRangeMax(request.getSalaryRangeMax());
        designation.setLevel(request.getLevel());
        designation.setIsActive(request.getIsActive() != null ? request.getIsActive() : true);
        designation.setCreatedBy(TenantContext.getUserId());

        designation = designationRepository.save(designation);
        return mapToResponse(designation);
    }

    @Transactional(readOnly = true)
    public List<DesignationResponse> getAllDesignations() {
        Long tenantId = TenantContext.getTenantId();
        return designationRepository.findByTenantId(tenantId)
            .stream()
            .map(this::mapToResponse)
            .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<DesignationResponse> getActiveDesignations() {
        Long tenantId = TenantContext.getTenantId();
        return designationRepository.findByTenantIdAndIsActiveTrue(tenantId)
            .stream()
            .map(this::mapToResponse)
            .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<DesignationResponse> getDesignationsByDepartment(Long departmentId) {
        Long tenantId = TenantContext.getTenantId();
        return designationRepository.findByTenantIdAndDepartmentId(tenantId, departmentId)
            .stream()
            .map(this::mapToResponse)
            .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public DesignationResponse getDesignationById(Long id) {
        Long tenantId = TenantContext.getTenantId();
        Designation designation = designationRepository.findByTenantIdAndId(tenantId, id)
            .orElseThrow(() -> new RuntimeException("Designation not found with id: " + id));
        return mapToResponse(designation);
    }

    @Transactional
    public DesignationResponse updateDesignation(Long id, DesignationRequest request) {
        Long tenantId = TenantContext.getTenantId();

        Designation designation = designationRepository.findByTenantIdAndId(tenantId, id)
            .orElseThrow(() -> new RuntimeException("Designation not found with id: " + id));

        // Verify department exists if it's being changed
        if (!designation.getDepartmentId().equals(request.getDepartmentId())) {
            departmentRepository.findByTenantIdAndId(tenantId, request.getDepartmentId())
                .orElseThrow(() -> new RuntimeException("Department not found with id: " + request.getDepartmentId()));
        }

        designation.setName(request.getName());
        designation.setCode(request.getCode());
        designation.setDescription(request.getDescription());
        designation.setDepartmentId(request.getDepartmentId());
        designation.setSalaryRangeMin(request.getSalaryRangeMin());
        designation.setSalaryRangeMax(request.getSalaryRangeMax());
        designation.setLevel(request.getLevel());
        designation.setIsActive(request.getIsActive());
        designation.setUpdatedBy(TenantContext.getUserId());

        designation = designationRepository.save(designation);
        return mapToResponse(designation);
    }

    @Transactional
    public void deleteDesignation(Long id) {
        Long tenantId = TenantContext.getTenantId();

        Designation designation = designationRepository.findByTenantIdAndId(tenantId, id)
            .orElseThrow(() -> new RuntimeException("Designation not found with id: " + id));

        designationRepository.delete(designation);
    }

    private DesignationResponse mapToResponse(Designation designation) {
        return DesignationResponse.builder()
            .id(designation.getId())
            .tenantId(designation.getTenantId())
            .name(designation.getName())
            .code(designation.getCode())
            .description(designation.getDescription())
            .departmentId(designation.getDepartmentId())
            .salaryRangeMin(designation.getSalaryRangeMin())
            .salaryRangeMax(designation.getSalaryRangeMax())
            .level(designation.getLevel())
            .isActive(designation.getIsActive())
            .createdAt(designation.getCreatedAt())
            .updatedAt(designation.getUpdatedAt())
            .createdBy(designation.getCreatedBy())
            .updatedBy(designation.getUpdatedBy())
            .build();
    }
}
