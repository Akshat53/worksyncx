package com.worksyncx.hrms.service.designation;

import com.worksyncx.hrms.dto.common.PageResponse;
import com.worksyncx.hrms.dto.designation.DesignationRequest;
import com.worksyncx.hrms.dto.designation.DesignationResponse;
import com.worksyncx.hrms.entity.Designation;
import com.worksyncx.hrms.exception.DepartmentNotFoundException;
import com.worksyncx.hrms.exception.DesignationNotFoundException;
import com.worksyncx.hrms.exception.DuplicateDesignationCodeException;
import com.worksyncx.hrms.exception.InvalidSalaryException;
import com.worksyncx.hrms.repository.DepartmentRepository;
import com.worksyncx.hrms.repository.DesignationRepository;
import com.worksyncx.hrms.security.TenantContext;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
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

        // Check if code already exists
        designationRepository.findByTenantIdAndCode(tenantId, request.getCode())
            .ifPresent(desig -> {
                throw new DuplicateDesignationCodeException("A designation with code '" + request.getCode() + "' already exists for your organization");
            });

        // Verify department exists
        departmentRepository.findByTenantIdAndId(tenantId, request.getDepartmentId())
            .orElseThrow(() -> new DepartmentNotFoundException("Department not found with id: " + request.getDepartmentId()));

        // Validate salary range
        if (request.getSalaryRangeMin() != null && request.getSalaryRangeMax() != null) {
            if (request.getSalaryRangeMin().compareTo(request.getSalaryRangeMax()) >= 0) {
                throw new InvalidSalaryException("Minimum salary must be less than maximum salary");
            }
        }

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
            .orElseThrow(() -> new DesignationNotFoundException("Designation not found with id: " + id));
        return mapToResponse(designation);
    }

    @Transactional
    public DesignationResponse updateDesignation(Long id, DesignationRequest request) {
        Long tenantId = TenantContext.getTenantId();

        Designation designation = designationRepository.findByTenantIdAndId(tenantId, id)
            .orElseThrow(() -> new DesignationNotFoundException("Designation not found with id: " + id));

        // Check if code is being changed and if new code already exists
        if (!designation.getCode().equals(request.getCode())) {
            designationRepository.findByTenantIdAndCode(tenantId, request.getCode())
                .ifPresent(desig -> {
                    throw new DuplicateDesignationCodeException("A designation with code '" + request.getCode() + "' already exists for your organization");
                });
        }

        // Verify department exists if it's being changed
        if (!designation.getDepartmentId().equals(request.getDepartmentId())) {
            departmentRepository.findByTenantIdAndId(tenantId, request.getDepartmentId())
                .orElseThrow(() -> new DepartmentNotFoundException("Department not found with id: " + request.getDepartmentId()));
        }

        // Validate salary range
        if (request.getSalaryRangeMin() != null && request.getSalaryRangeMax() != null) {
            if (request.getSalaryRangeMin().compareTo(request.getSalaryRangeMax()) >= 0) {
                throw new InvalidSalaryException("Minimum salary must be less than maximum salary");
            }
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
            .orElseThrow(() -> new DesignationNotFoundException("Designation not found with id: " + id));

        designationRepository.delete(designation);
    }

    // Paginated methods
    @Transactional(readOnly = true)
    public PageResponse<DesignationResponse> getAllDesignationsPaginated(Pageable pageable) {
        Long tenantId = TenantContext.getTenantId();
        Page<Designation> designationPage = designationRepository.findByTenantId(tenantId, pageable);
        return mapToPageResponse(designationPage);
    }

    @Transactional(readOnly = true)
    public PageResponse<DesignationResponse> getActiveDesignationsPaginated(Pageable pageable) {
        Long tenantId = TenantContext.getTenantId();
        Page<Designation> designationPage = designationRepository.findByTenantIdAndIsActiveTrue(tenantId, pageable);
        return mapToPageResponse(designationPage);
    }

    @Transactional(readOnly = true)
    public PageResponse<DesignationResponse> getDesignationsByDepartmentPaginated(Long departmentId, Pageable pageable) {
        Long tenantId = TenantContext.getTenantId();
        Page<Designation> designationPage = designationRepository.findByTenantIdAndDepartmentId(tenantId, departmentId, pageable);
        return mapToPageResponse(designationPage);
    }

    private PageResponse<DesignationResponse> mapToPageResponse(Page<Designation> page) {
        List<DesignationResponse> content = page.getContent()
            .stream()
            .map(this::mapToResponse)
            .collect(Collectors.toList());

        return PageResponse.<DesignationResponse>builder()
            .content(content)
            .pageNumber(page.getNumber())
            .pageSize(page.getSize())
            .totalElements(page.getTotalElements())
            .totalPages(page.getTotalPages())
            .first(page.isFirst())
            .last(page.isLast())
            .hasNext(page.hasNext())
            .hasPrevious(page.hasPrevious())
            .numberOfElements(page.getNumberOfElements())
            .empty(page.isEmpty())
            .build();
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
