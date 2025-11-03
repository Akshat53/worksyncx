package com.worksyncx.hrms.service.department;

import com.worksyncx.hrms.dto.department.DepartmentRequest;
import com.worksyncx.hrms.dto.department.DepartmentResponse;
import com.worksyncx.hrms.entity.Department;
import com.worksyncx.hrms.repository.DepartmentRepository;
import com.worksyncx.hrms.security.TenantContext;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class DepartmentService {

    private final DepartmentRepository departmentRepository;

    @Transactional
    public DepartmentResponse createDepartment(DepartmentRequest request) {
        Long tenantId = TenantContext.getTenantId();

        // Check if code already exists
        departmentRepository.findByTenantIdAndCode(tenantId, request.getCode())
            .ifPresent(dept -> {
                throw new RuntimeException("Department with code " + request.getCode() + " already exists");
            });

        Department department = new Department();
        department.setTenantId(tenantId);
        department.setName(request.getName());
        department.setCode(request.getCode());
        department.setDescription(request.getDescription());
        department.setHeadId(request.getHeadId());
        department.setParentDepartmentId(request.getParentDepartmentId());
        department.setIsActive(request.getIsActive() != null ? request.getIsActive() : true);
        department.setCreatedBy(TenantContext.getUserId());

        department = departmentRepository.save(department);
        return mapToResponse(department);
    }

    @Transactional(readOnly = true)
    public List<DepartmentResponse> getAllDepartments() {
        Long tenantId = TenantContext.getTenantId();
        return departmentRepository.findByTenantId(tenantId)
            .stream()
            .map(this::mapToResponse)
            .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<DepartmentResponse> getActiveDepartments() {
        Long tenantId = TenantContext.getTenantId();
        return departmentRepository.findByTenantIdAndIsActiveTrue(tenantId)
            .stream()
            .map(this::mapToResponse)
            .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public DepartmentResponse getDepartmentById(Long id) {
        Long tenantId = TenantContext.getTenantId();
        Department department = departmentRepository.findByTenantIdAndId(tenantId, id)
            .orElseThrow(() -> new RuntimeException("Department not found with id: " + id));
        return mapToResponse(department);
    }

    @Transactional
    public DepartmentResponse updateDepartment(Long id, DepartmentRequest request) {
        Long tenantId = TenantContext.getTenantId();

        Department department = departmentRepository.findByTenantIdAndId(tenantId, id)
            .orElseThrow(() -> new RuntimeException("Department not found with id: " + id));

        // Check if code is being changed and if new code already exists
        if (!department.getCode().equals(request.getCode())) {
            departmentRepository.findByTenantIdAndCode(tenantId, request.getCode())
                .ifPresent(dept -> {
                    throw new RuntimeException("Department with code " + request.getCode() + " already exists");
                });
        }

        department.setName(request.getName());
        department.setCode(request.getCode());
        department.setDescription(request.getDescription());
        department.setHeadId(request.getHeadId());
        department.setParentDepartmentId(request.getParentDepartmentId());
        department.setIsActive(request.getIsActive());
        department.setUpdatedBy(TenantContext.getUserId());

        department = departmentRepository.save(department);
        return mapToResponse(department);
    }

    @Transactional
    public void deleteDepartment(Long id) {
        Long tenantId = TenantContext.getTenantId();

        Department department = departmentRepository.findByTenantIdAndId(tenantId, id)
            .orElseThrow(() -> new RuntimeException("Department not found with id: " + id));

        departmentRepository.delete(department);
    }

    private DepartmentResponse mapToResponse(Department department) {
        return DepartmentResponse.builder()
            .id(department.getId())
            .tenantId(department.getTenantId())
            .name(department.getName())
            .code(department.getCode())
            .description(department.getDescription())
            .headId(department.getHeadId())
            .parentDepartmentId(department.getParentDepartmentId())
            .isActive(department.getIsActive())
            .createdAt(department.getCreatedAt())
            .updatedAt(department.getUpdatedAt())
            .createdBy(department.getCreatedBy())
            .updatedBy(department.getUpdatedBy())
            .build();
    }
}
