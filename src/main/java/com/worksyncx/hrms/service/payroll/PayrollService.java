package com.worksyncx.hrms.service.payroll;

import com.worksyncx.hrms.dto.common.PageResponse;
import com.worksyncx.hrms.dto.payroll.PayrollCycleRequest;
import com.worksyncx.hrms.dto.payroll.PayrollCycleResponse;
import com.worksyncx.hrms.dto.payroll.PayrollRequest;
import com.worksyncx.hrms.dto.payroll.PayrollResponse;
import com.worksyncx.hrms.entity.Payroll;
import com.worksyncx.hrms.entity.PayrollCycle;
import com.worksyncx.hrms.enums.PayrollStatus;
import com.worksyncx.hrms.repository.EmployeeRepository;
import com.worksyncx.hrms.repository.PayrollCycleRepository;
import com.worksyncx.hrms.repository.PayrollRepository;
import com.worksyncx.hrms.security.TenantContext;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class PayrollService {

    private final PayrollCycleRepository payrollCycleRepository;
    private final PayrollRepository payrollRepository;
    private final EmployeeRepository employeeRepository;

    // ==================== Payroll Cycle Management ====================

    @Transactional
    public PayrollCycleResponse createPayrollCycle(PayrollCycleRequest request) {
        Long tenantId = TenantContext.getTenantId();

        // Check if cycle already exists for this month/year
        payrollCycleRepository.findByTenantIdAndMonthAndYear(tenantId, request.getMonth(), request.getYear())
            .ifPresent(cycle -> {
                throw new RuntimeException("Payroll cycle already exists for " + request.getMonth() + "/" + request.getYear());
            });

        PayrollCycle cycle = new PayrollCycle();
        cycle.setTenantId(tenantId);
        cycle.setName(request.getName());
        cycle.setMonth(request.getMonth());
        cycle.setYear(request.getYear());
        cycle.setStartDate(request.getStartDate());
        cycle.setEndDate(request.getEndDate());
        cycle.setSalaryDate(request.getSalaryDate());
        cycle.setStatus(request.getStatus() != null ? request.getStatus() : PayrollStatus.DRAFT);
        cycle.setCreatedBy(TenantContext.getUserId());

        cycle = payrollCycleRepository.save(cycle);
        return mapCycleToResponse(cycle);
    }

    @Transactional(readOnly = true)
    public List<PayrollCycleResponse> getAllPayrollCycles() {
        Long tenantId = TenantContext.getTenantId();
        return payrollCycleRepository.findByTenantId(tenantId)
            .stream()
            .map(this::mapCycleToResponse)
            .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public PayrollCycleResponse getPayrollCycleById(Long id) {
        Long tenantId = TenantContext.getTenantId();
        PayrollCycle cycle = payrollCycleRepository.findByTenantIdAndId(tenantId, id)
            .orElseThrow(() -> new RuntimeException("Payroll cycle not found with id: " + id));
        return mapCycleToResponse(cycle);
    }

    @Transactional
    public PayrollCycleResponse updatePayrollCycle(Long id, PayrollCycleRequest request) {
        Long tenantId = TenantContext.getTenantId();

        PayrollCycle cycle = payrollCycleRepository.findByTenantIdAndId(tenantId, id)
            .orElseThrow(() -> new RuntimeException("Payroll cycle not found with id: " + id));

        cycle.setName(request.getName());
        cycle.setMonth(request.getMonth());
        cycle.setYear(request.getYear());
        cycle.setStartDate(request.getStartDate());
        cycle.setEndDate(request.getEndDate());
        cycle.setSalaryDate(request.getSalaryDate());
        cycle.setStatus(request.getStatus());
        cycle.setUpdatedBy(TenantContext.getUserId());

        cycle = payrollCycleRepository.save(cycle);
        return mapCycleToResponse(cycle);
    }

    @Transactional
    public void deletePayrollCycle(Long id) {
        Long tenantId = TenantContext.getTenantId();

        PayrollCycle cycle = payrollCycleRepository.findByTenantIdAndId(tenantId, id)
            .orElseThrow(() -> new RuntimeException("Payroll cycle not found with id: " + id));

        payrollCycleRepository.delete(cycle);
    }

    // ==================== Payroll Management ====================

    @Transactional
    public PayrollResponse createPayroll(PayrollRequest request) {
        Long tenantId = TenantContext.getTenantId();

        // Verify employee exists
        employeeRepository.findByTenantIdAndId(tenantId, request.getEmployeeId())
            .orElseThrow(() -> new RuntimeException("Employee not found with id: " + request.getEmployeeId()));

        // Verify payroll cycle exists
        payrollCycleRepository.findByTenantIdAndId(tenantId, request.getPayrollCycleId())
            .orElseThrow(() -> new RuntimeException("Payroll cycle not found with id: " + request.getPayrollCycleId()));

        Payroll payroll = new Payroll();
        payroll.setTenantId(tenantId);
        payroll.setEmployeeId(request.getEmployeeId());
        payroll.setPayrollCycleId(request.getPayrollCycleId());

        mapRequestToPayroll(request, payroll);

        // Auto-calculate if not provided
        if (payroll.getGrossSalary() == null) {
            payroll.setGrossSalary(calculateGrossSalary(payroll));
        }
        if (payroll.getTotalDeductions() == null) {
            payroll.setTotalDeductions(calculateTotalDeductions(payroll));
        }
        if (payroll.getNetSalary() == null) {
            payroll.setNetSalary(calculateNetSalary(payroll));
        }

        payroll.setStatus(request.getStatus() != null ? request.getStatus() : PayrollStatus.DRAFT);
        payroll.setCreatedBy(TenantContext.getUserId());

        payroll = payrollRepository.save(payroll);
        return mapPayrollToResponse(payroll);
    }

    @Transactional(readOnly = true)
    public List<PayrollResponse> getAllPayrolls() {
        Long tenantId = TenantContext.getTenantId();
        return payrollRepository.findByTenantId(tenantId)
            .stream()
            .map(this::mapPayrollToResponse)
            .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<PayrollResponse> getPayrollsByCycle(Long cycleId) {
        Long tenantId = TenantContext.getTenantId();
        return payrollRepository.findByTenantIdAndPayrollCycleId(tenantId, cycleId)
            .stream()
            .map(this::mapPayrollToResponse)
            .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<PayrollResponse> getPayrollsByEmployee(Long employeeId) {
        Long tenantId = TenantContext.getTenantId();
        return payrollRepository.findByTenantIdAndEmployeeId(tenantId, employeeId)
            .stream()
            .map(this::mapPayrollToResponse)
            .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public PayrollResponse getPayrollById(Long id) {
        Long tenantId = TenantContext.getTenantId();
        Payroll payroll = payrollRepository.findByTenantIdAndId(tenantId, id)
            .orElseThrow(() -> new RuntimeException("Payroll not found with id: " + id));
        return mapPayrollToResponse(payroll);
    }

    @Transactional
    public PayrollResponse updatePayroll(Long id, PayrollRequest request) {
        Long tenantId = TenantContext.getTenantId();

        Payroll payroll = payrollRepository.findByTenantIdAndId(tenantId, id)
            .orElseThrow(() -> new RuntimeException("Payroll not found with id: " + id));

        mapRequestToPayroll(request, payroll);

        // Recalculate if not provided
        if (request.getGrossSalary() == null) {
            payroll.setGrossSalary(calculateGrossSalary(payroll));
        }
        if (request.getTotalDeductions() == null) {
            payroll.setTotalDeductions(calculateTotalDeductions(payroll));
        }
        if (request.getNetSalary() == null) {
            payroll.setNetSalary(calculateNetSalary(payroll));
        }

        payroll.setUpdatedBy(TenantContext.getUserId());

        payroll = payrollRepository.save(payroll);
        return mapPayrollToResponse(payroll);
    }

    @Transactional
    public PayrollResponse markAsPaid(Long id, String bankTransferRef) {
        Long tenantId = TenantContext.getTenantId();

        Payroll payroll = payrollRepository.findByTenantIdAndId(tenantId, id)
            .orElseThrow(() -> new RuntimeException("Payroll not found with id: " + id));

        payroll.setStatus(PayrollStatus.PAID);
        payroll.setPaidDate(LocalDateTime.now());
        if (bankTransferRef != null) {
            payroll.setBankTransferRef(bankTransferRef);
        }
        payroll.setUpdatedBy(TenantContext.getUserId());

        payroll = payrollRepository.save(payroll);
        return mapPayrollToResponse(payroll);
    }

    @Transactional
    public void deletePayroll(Long id) {
        Long tenantId = TenantContext.getTenantId();

        Payroll payroll = payrollRepository.findByTenantIdAndId(tenantId, id)
            .orElseThrow(() -> new RuntimeException("Payroll not found with id: " + id));

        payrollRepository.delete(payroll);
    }

    // ==================== Paginated Methods ====================

    @Transactional(readOnly = true)
    public PageResponse<PayrollCycleResponse> getAllPayrollCyclesPaginated(Pageable pageable) {
        Long tenantId = TenantContext.getTenantId();
        Page<PayrollCycle> page = payrollCycleRepository.findByTenantId(tenantId, pageable);
        return mapCyclePageToResponse(page);
    }

    @Transactional(readOnly = true)
    public PageResponse<PayrollResponse> getAllPayrollsPaginated(Pageable pageable) {
        Long tenantId = TenantContext.getTenantId();
        Page<Payroll> page = payrollRepository.findByTenantId(tenantId, pageable);
        return mapPayrollPageToResponse(page);
    }

    @Transactional(readOnly = true)
    public PageResponse<PayrollResponse> getPayrollsByCyclePaginated(Long cycleId, Pageable pageable) {
        Long tenantId = TenantContext.getTenantId();
        Page<Payroll> page = payrollRepository.findByTenantIdAndPayrollCycleId(tenantId, cycleId, pageable);
        return mapPayrollPageToResponse(page);
    }

    @Transactional(readOnly = true)
    public PageResponse<PayrollResponse> getPayrollsByEmployeePaginated(Long employeeId, Pageable pageable) {
        Long tenantId = TenantContext.getTenantId();
        Page<Payroll> page = payrollRepository.findByTenantIdAndEmployeeId(tenantId, employeeId, pageable);
        return mapPayrollPageToResponse(page);
    }

    // ==================== Helper Methods ====================

    private void mapRequestToPayroll(PayrollRequest request, Payroll payroll) {
        payroll.setBasicSalary(request.getBasicSalary());
        payroll.setHra(request.getHra());
        payroll.setDearnessAllowance(request.getDearnessAllowance());
        payroll.setOtherAllowances(request.getOtherAllowances());
        payroll.setGrossSalary(request.getGrossSalary());
        payroll.setIncomeTax(request.getIncomeTax());
        payroll.setProfessionalTax(request.getProfessionalTax());
        payroll.setEmployeePf(request.getEmployeePf());
        payroll.setEmployeeEsi(request.getEmployeeEsi());
        payroll.setOtherDeductions(request.getOtherDeductions());
        payroll.setTotalDeductions(request.getTotalDeductions());
        payroll.setNetSalary(request.getNetSalary());
        payroll.setBankTransferRef(request.getBankTransferRef());
        if (request.getStatus() != null) {
            payroll.setStatus(request.getStatus());
        }
    }

    private BigDecimal calculateGrossSalary(Payroll payroll) {
        BigDecimal gross = BigDecimal.ZERO;
        if (payroll.getBasicSalary() != null) gross = gross.add(payroll.getBasicSalary());
        if (payroll.getHra() != null) gross = gross.add(payroll.getHra());
        if (payroll.getDearnessAllowance() != null) gross = gross.add(payroll.getDearnessAllowance());
        if (payroll.getOtherAllowances() != null) gross = gross.add(payroll.getOtherAllowances());
        return gross;
    }

    private BigDecimal calculateTotalDeductions(Payroll payroll) {
        BigDecimal deductions = BigDecimal.ZERO;
        if (payroll.getIncomeTax() != null) deductions = deductions.add(payroll.getIncomeTax());
        if (payroll.getProfessionalTax() != null) deductions = deductions.add(payroll.getProfessionalTax());
        if (payroll.getEmployeePf() != null) deductions = deductions.add(payroll.getEmployeePf());
        if (payroll.getEmployeeEsi() != null) deductions = deductions.add(payroll.getEmployeeEsi());
        if (payroll.getOtherDeductions() != null) deductions = deductions.add(payroll.getOtherDeductions());
        return deductions;
    }

    private BigDecimal calculateNetSalary(Payroll payroll) {
        BigDecimal gross = payroll.getGrossSalary() != null ? payroll.getGrossSalary() : BigDecimal.ZERO;
        BigDecimal deductions = payroll.getTotalDeductions() != null ? payroll.getTotalDeductions() : BigDecimal.ZERO;
        return gross.subtract(deductions);
    }

    // ==================== Mappers ====================

    private PayrollCycleResponse mapCycleToResponse(PayrollCycle cycle) {
        return PayrollCycleResponse.builder()
            .id(cycle.getId())
            .tenantId(cycle.getTenantId())
            .name(cycle.getName())
            .month(cycle.getMonth())
            .year(cycle.getYear())
            .startDate(cycle.getStartDate())
            .endDate(cycle.getEndDate())
            .salaryDate(cycle.getSalaryDate())
            .status(cycle.getStatus())
            .createdAt(cycle.getCreatedAt())
            .updatedAt(cycle.getUpdatedAt())
            .createdBy(cycle.getCreatedBy())
            .updatedBy(cycle.getUpdatedBy())
            .build();
    }

    private PayrollResponse mapPayrollToResponse(Payroll payroll) {
        return PayrollResponse.builder()
            .id(payroll.getId())
            .tenantId(payroll.getTenantId())
            .employeeId(payroll.getEmployeeId())
            .payrollCycleId(payroll.getPayrollCycleId())
            .basicSalary(payroll.getBasicSalary())
            .hra(payroll.getHra())
            .dearnessAllowance(payroll.getDearnessAllowance())
            .otherAllowances(payroll.getOtherAllowances())
            .grossSalary(payroll.getGrossSalary())
            .incomeTax(payroll.getIncomeTax())
            .professionalTax(payroll.getProfessionalTax())
            .employeePf(payroll.getEmployeePf())
            .employeeEsi(payroll.getEmployeeEsi())
            .otherDeductions(payroll.getOtherDeductions())
            .totalDeductions(payroll.getTotalDeductions())
            .netSalary(payroll.getNetSalary())
            .bankTransferRef(payroll.getBankTransferRef())
            .status(payroll.getStatus())
            .paidDate(payroll.getPaidDate())
            .createdAt(payroll.getCreatedAt())
            .updatedAt(payroll.getUpdatedAt())
            .createdBy(payroll.getCreatedBy())
            .updatedBy(payroll.getUpdatedBy())
            .build();
    }

    private PageResponse<PayrollCycleResponse> mapCyclePageToResponse(Page<PayrollCycle> page) {
        List<PayrollCycleResponse> content = page.getContent()
            .stream()
            .map(this::mapCycleToResponse)
            .collect(Collectors.toList());

        return PageResponse.<PayrollCycleResponse>builder()
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

    private PageResponse<PayrollResponse> mapPayrollPageToResponse(Page<Payroll> page) {
        List<PayrollResponse> content = page.getContent()
            .stream()
            .map(this::mapPayrollToResponse)
            .collect(Collectors.toList());

        return PageResponse.<PayrollResponse>builder()
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
}
