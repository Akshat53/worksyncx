package com.worksyncx.hrms.service.leave;

import com.worksyncx.hrms.dto.common.PageResponse;
import com.worksyncx.hrms.dto.leave.*;
import com.worksyncx.hrms.entity.LeaveRequest;
import com.worksyncx.hrms.entity.LeaveType;
import com.worksyncx.hrms.enums.LeaveStatus;
import com.worksyncx.hrms.repository.EmployeeRepository;
import com.worksyncx.hrms.repository.LeaveRequestRepository;
import com.worksyncx.hrms.repository.LeaveTypeRepository;
import com.worksyncx.hrms.security.TenantContext;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class LeaveService {

    private final LeaveTypeRepository leaveTypeRepository;
    private final LeaveRequestRepository leaveRequestRepository;
    private final EmployeeRepository employeeRepository;

    // ==================== Leave Type Management ====================

    @Transactional
    public LeaveTypeResponse createLeaveType(LeaveTypeRequest request) {
        Long tenantId = TenantContext.getTenantId();

        LeaveType leaveType = new LeaveType();
        leaveType.setTenantId(tenantId);
        leaveType.setName(request.getName());
        leaveType.setCode(request.getCode());
        leaveType.setDaysPerYear(request.getDaysPerYear());
        leaveType.setIsPaid(request.getIsPaid() != null ? request.getIsPaid() : true);
        leaveType.setRequiresApproval(request.getRequiresApproval() != null ? request.getRequiresApproval() : true);
        leaveType.setColorCode(request.getColorCode());
        leaveType.setIsActive(request.getIsActive() != null ? request.getIsActive() : true);
        leaveType.setCreatedBy(TenantContext.getUserId());

        leaveType = leaveTypeRepository.save(leaveType);
        return mapLeaveTypeToResponse(leaveType);
    }

    @Transactional(readOnly = true)
    public List<LeaveTypeResponse> getAllLeaveTypes() {
        Long tenantId = TenantContext.getTenantId();
        return leaveTypeRepository.findByTenantId(tenantId)
            .stream()
            .map(this::mapLeaveTypeToResponse)
            .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<LeaveTypeResponse> getActiveLeaveTypes() {
        Long tenantId = TenantContext.getTenantId();
        return leaveTypeRepository.findByTenantIdAndIsActiveTrue(tenantId)
            .stream()
            .map(this::mapLeaveTypeToResponse)
            .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public LeaveTypeResponse getLeaveTypeById(Long id) {
        Long tenantId = TenantContext.getTenantId();
        LeaveType leaveType = leaveTypeRepository.findByTenantIdAndId(tenantId, id)
            .orElseThrow(() -> new RuntimeException("Leave type not found with id: " + id));
        return mapLeaveTypeToResponse(leaveType);
    }

    @Transactional
    public LeaveTypeResponse updateLeaveType(Long id, LeaveTypeRequest request) {
        Long tenantId = TenantContext.getTenantId();

        LeaveType leaveType = leaveTypeRepository.findByTenantIdAndId(tenantId, id)
            .orElseThrow(() -> new RuntimeException("Leave type not found with id: " + id));

        leaveType.setName(request.getName());
        leaveType.setCode(request.getCode());
        leaveType.setDaysPerYear(request.getDaysPerYear());
        leaveType.setIsPaid(request.getIsPaid());
        leaveType.setRequiresApproval(request.getRequiresApproval());
        leaveType.setColorCode(request.getColorCode());
        leaveType.setIsActive(request.getIsActive());
        leaveType.setUpdatedBy(TenantContext.getUserId());

        leaveType = leaveTypeRepository.save(leaveType);
        return mapLeaveTypeToResponse(leaveType);
    }

    @Transactional
    public void deleteLeaveType(Long id) {
        Long tenantId = TenantContext.getTenantId();

        LeaveType leaveType = leaveTypeRepository.findByTenantIdAndId(tenantId, id)
            .orElseThrow(() -> new RuntimeException("Leave type not found with id: " + id));

        leaveTypeRepository.delete(leaveType);
    }

    // ==================== Leave Request Management ====================

    @Transactional
    public LeaveRequestResponse createLeaveRequest(LeaveRequestDto request) {
        Long tenantId = TenantContext.getTenantId();

        // Verify employee exists
        employeeRepository.findByTenantIdAndId(tenantId, request.getEmployeeId())
            .orElseThrow(() -> new RuntimeException("Employee not found with id: " + request.getEmployeeId()));

        // Verify leave type exists
        leaveTypeRepository.findByTenantIdAndId(tenantId, request.getLeaveTypeId())
            .orElseThrow(() -> new RuntimeException("Leave type not found with id: " + request.getLeaveTypeId()));

        LeaveRequest leaveRequest = new LeaveRequest();
        leaveRequest.setTenantId(tenantId);
        leaveRequest.setEmployeeId(request.getEmployeeId());
        leaveRequest.setLeaveTypeId(request.getLeaveTypeId());
        leaveRequest.setStartDate(request.getStartDate());
        leaveRequest.setEndDate(request.getEndDate());

        // Calculate total days if not provided
        if (request.getTotalDays() != null) {
            leaveRequest.setTotalDays(request.getTotalDays());
        } else {
            long days = ChronoUnit.DAYS.between(request.getStartDate(), request.getEndDate()) + 1;
            leaveRequest.setTotalDays(java.math.BigDecimal.valueOf(days));
        }

        leaveRequest.setReason(request.getReason());
        leaveRequest.setAttachmentUrl(request.getAttachmentUrl());
        leaveRequest.setStatus(LeaveStatus.PENDING);
        leaveRequest.setCreatedBy(TenantContext.getUserId());

        leaveRequest = leaveRequestRepository.save(leaveRequest);
        return mapLeaveRequestToResponse(leaveRequest);
    }

    @Transactional(readOnly = true)
    public List<LeaveRequestResponse> getAllLeaveRequests() {
        Long tenantId = TenantContext.getTenantId();
        return leaveRequestRepository.findByTenantId(tenantId)
            .stream()
            .map(this::mapLeaveRequestToResponse)
            .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<LeaveRequestResponse> getLeaveRequestsByEmployee(Long employeeId) {
        Long tenantId = TenantContext.getTenantId();
        return leaveRequestRepository.findByTenantIdAndEmployeeId(tenantId, employeeId)
            .stream()
            .map(this::mapLeaveRequestToResponse)
            .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<LeaveRequestResponse> getLeaveRequestsByStatus(LeaveStatus status) {
        Long tenantId = TenantContext.getTenantId();
        return leaveRequestRepository.findByTenantIdAndStatus(tenantId, status)
            .stream()
            .map(this::mapLeaveRequestToResponse)
            .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public LeaveRequestResponse getLeaveRequestById(Long id) {
        Long tenantId = TenantContext.getTenantId();
        LeaveRequest leaveRequest = leaveRequestRepository.findByTenantIdAndId(tenantId, id)
            .orElseThrow(() -> new RuntimeException("Leave request not found with id: " + id));
        return mapLeaveRequestToResponse(leaveRequest);
    }

    @Transactional
    public LeaveRequestResponse approveLeaveRequest(Long id, LeaveApprovalRequest request) {
        Long tenantId = TenantContext.getTenantId();

        LeaveRequest leaveRequest = leaveRequestRepository.findByTenantIdAndId(tenantId, id)
            .orElseThrow(() -> new RuntimeException("Leave request not found with id: " + id));

        if (leaveRequest.getStatus() != LeaveStatus.PENDING) {
            throw new RuntimeException("Only pending leave requests can be approved");
        }

        leaveRequest.setStatus(LeaveStatus.APPROVED);
        leaveRequest.setApprovedBy(TenantContext.getUserId());
        leaveRequest.setApprovedAt(LocalDateTime.now());
        leaveRequest.setUpdatedBy(TenantContext.getUserId());

        leaveRequest = leaveRequestRepository.save(leaveRequest);
        return mapLeaveRequestToResponse(leaveRequest);
    }

    @Transactional
    public LeaveRequestResponse rejectLeaveRequest(Long id, LeaveRejectionRequest request) {
        Long tenantId = TenantContext.getTenantId();

        LeaveRequest leaveRequest = leaveRequestRepository.findByTenantIdAndId(tenantId, id)
            .orElseThrow(() -> new RuntimeException("Leave request not found with id: " + id));

        if (leaveRequest.getStatus() != LeaveStatus.PENDING) {
            throw new RuntimeException("Only pending leave requests can be rejected");
        }

        leaveRequest.setStatus(LeaveStatus.REJECTED);
        leaveRequest.setRejectionReason(request.getRejectionReason());
        leaveRequest.setApprovedBy(TenantContext.getUserId());
        leaveRequest.setApprovedAt(LocalDateTime.now());
        leaveRequest.setUpdatedBy(TenantContext.getUserId());

        leaveRequest = leaveRequestRepository.save(leaveRequest);
        return mapLeaveRequestToResponse(leaveRequest);
    }

    @Transactional
    public LeaveRequestResponse cancelLeaveRequest(Long id) {
        Long tenantId = TenantContext.getTenantId();

        LeaveRequest leaveRequest = leaveRequestRepository.findByTenantIdAndId(tenantId, id)
            .orElseThrow(() -> new RuntimeException("Leave request not found with id: " + id));

        if (leaveRequest.getStatus() == LeaveStatus.CANCELLED) {
            throw new RuntimeException("Leave request is already cancelled");
        }

        leaveRequest.setStatus(LeaveStatus.CANCELLED);
        leaveRequest.setUpdatedBy(TenantContext.getUserId());

        leaveRequest = leaveRequestRepository.save(leaveRequest);
        return mapLeaveRequestToResponse(leaveRequest);
    }

    // ==================== Paginated Methods ====================

    @Transactional(readOnly = true)
    public PageResponse<LeaveRequestResponse> getAllLeaveRequestsPaginated(Pageable pageable) {
        Long tenantId = TenantContext.getTenantId();
        Page<LeaveRequest> page = leaveRequestRepository.findByTenantId(tenantId, pageable);
        return mapToPageResponse(page);
    }

    @Transactional(readOnly = true)
    public PageResponse<LeaveRequestResponse> getLeaveRequestsByEmployeePaginated(Long employeeId, Pageable pageable) {
        Long tenantId = TenantContext.getTenantId();
        Page<LeaveRequest> page = leaveRequestRepository.findByTenantIdAndEmployeeId(tenantId, employeeId, pageable);
        return mapToPageResponse(page);
    }

    @Transactional(readOnly = true)
    public PageResponse<LeaveRequestResponse> getLeaveRequestsByStatusPaginated(LeaveStatus status, Pageable pageable) {
        Long tenantId = TenantContext.getTenantId();
        Page<LeaveRequest> page = leaveRequestRepository.findByTenantIdAndStatus(tenantId, status, pageable);
        return mapToPageResponse(page);
    }

    // ==================== Mappers ====================

    private PageResponse<LeaveRequestResponse> mapToPageResponse(Page<LeaveRequest> page) {
        List<LeaveRequestResponse> content = page.getContent()
            .stream()
            .map(this::mapLeaveRequestToResponse)
            .collect(Collectors.toList());

        return PageResponse.<LeaveRequestResponse>builder()
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

    private LeaveTypeResponse mapLeaveTypeToResponse(LeaveType leaveType) {
        return LeaveTypeResponse.builder()
            .id(leaveType.getId())
            .tenantId(leaveType.getTenantId())
            .name(leaveType.getName())
            .code(leaveType.getCode())
            .daysPerYear(leaveType.getDaysPerYear())
            .isPaid(leaveType.getIsPaid())
            .requiresApproval(leaveType.getRequiresApproval())
            .colorCode(leaveType.getColorCode())
            .isActive(leaveType.getIsActive())
            .createdAt(leaveType.getCreatedAt())
            .updatedAt(leaveType.getUpdatedAt())
            .createdBy(leaveType.getCreatedBy())
            .updatedBy(leaveType.getUpdatedBy())
            .build();
    }

    private LeaveRequestResponse mapLeaveRequestToResponse(LeaveRequest leaveRequest) {
        return LeaveRequestResponse.builder()
            .id(leaveRequest.getId())
            .tenantId(leaveRequest.getTenantId())
            .employeeId(leaveRequest.getEmployeeId())
            .leaveTypeId(leaveRequest.getLeaveTypeId())
            .startDate(leaveRequest.getStartDate())
            .endDate(leaveRequest.getEndDate())
            .totalDays(leaveRequest.getTotalDays())
            .reason(leaveRequest.getReason())
            .attachmentUrl(leaveRequest.getAttachmentUrl())
            .status(leaveRequest.getStatus())
            .approvedBy(leaveRequest.getApprovedBy())
            .approvedAt(leaveRequest.getApprovedAt())
            .rejectionReason(leaveRequest.getRejectionReason())
            .createdAt(leaveRequest.getCreatedAt())
            .updatedAt(leaveRequest.getUpdatedAt())
            .createdBy(leaveRequest.getCreatedBy())
            .updatedBy(leaveRequest.getUpdatedBy())
            .build();
    }
}
