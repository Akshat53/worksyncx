package com.worksyncx.hrms.service.employee;

import com.worksyncx.hrms.dto.employee.EmployeeRequest;
import com.worksyncx.hrms.dto.employee.EmployeeResponse;
import com.worksyncx.hrms.entity.Employee;
import com.worksyncx.hrms.entity.Tenant;
import com.worksyncx.hrms.entity.User;
import com.worksyncx.hrms.entity.Role;
import com.worksyncx.hrms.enums.EmploymentStatus;
import com.worksyncx.hrms.exception.*;
import com.worksyncx.hrms.repository.DepartmentRepository;
import com.worksyncx.hrms.repository.DesignationRepository;
import com.worksyncx.hrms.repository.EmployeeRepository;
import com.worksyncx.hrms.repository.TenantRepository;
import com.worksyncx.hrms.repository.UserRepository;
import com.worksyncx.hrms.repository.RoleRepository;
import com.worksyncx.hrms.security.TenantContext;
import com.worksyncx.hrms.service.subscription.SubscriptionService;
import com.worksyncx.hrms.dto.common.PageResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.security.SecureRandom;
import java.time.LocalDate;
import java.util.HashSet;
import java.util.List;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class EmployeeService {

    private final EmployeeRepository employeeRepository;
    private final DepartmentRepository departmentRepository;
    private final DesignationRepository designationRepository;
    private final SubscriptionService subscriptionService;
    private final TenantRepository tenantRepository;
    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final PasswordEncoder passwordEncoder;

    private static final Pattern PHONE_PATTERN = Pattern.compile("^[+]?[0-9]{10,15}$");
    private static final String PASSWORD_CHARS = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789!@#$%^&*";
    private static final SecureRandom RANDOM = new SecureRandom();

    /**
     * Validates employee request data including dates, salary, and phone
     */
    private void validateEmployeeRequest(EmployeeRequest request, Long excludeEmployeeId) {
        // Validate phone number format
        if (request.getPhone() != null && !request.getPhone().isEmpty()) {
            if (!PHONE_PATTERN.matcher(request.getPhone()).matches()) {
                throw new EmployeeValidationException(
                    "Invalid phone number format. Phone must be 10-15 digits and may start with +"
                );
            }
        }

        // Validate salary is positive
        if (request.getBasicSalary() != null && request.getBasicSalary().doubleValue() <= 0) {
            throw new InvalidSalaryException("Salary must be greater than zero");
        }

        // Validate date of birth is not in future
        if (request.getDateOfBirth() != null && request.getDateOfBirth().isAfter(LocalDate.now())) {
            throw new InvalidDateException("Date of birth cannot be in the future");
        }

        // Validate date of joining is not in future
        if (request.getDateOfJoining() != null && request.getDateOfJoining().isAfter(LocalDate.now())) {
            throw new InvalidDateException("Date of joining cannot be in the future");
        }

        // Validate leaving date is after joining date
        if (request.getDateOfLeaving() != null && request.getDateOfJoining() != null) {
            if (request.getDateOfLeaving().isBefore(request.getDateOfJoining())) {
                throw new InvalidDateException("Date of leaving cannot be before date of joining");
            }
        }
    }

    /**
     * Generates a secure random temporary password
     */
    private String generateTemporaryPassword() {
        StringBuilder password = new StringBuilder(12);
        for (int i = 0; i < 12; i++) {
            password.append(PASSWORD_CHARS.charAt(RANDOM.nextInt(PASSWORD_CHARS.length())));
        }
        return password.toString();
    }

    /**
     * Generates a unique employee code automatically
     * Format: EMP0001, EMP0002, etc.
     */
    private String generateEmployeeCode(Long tenantId) {
        // Get all existing employee codes for this tenant
        List<Employee> employees = employeeRepository.findByTenantId(tenantId);

        int maxNumber = 0;
        Pattern codePattern = Pattern.compile("^EMP(\\d{4})$");

        for (Employee emp : employees) {
            if (emp.getEmployeeCode() != null) {
                java.util.regex.Matcher matcher = codePattern.matcher(emp.getEmployeeCode());
                if (matcher.matches()) {
                    int number = Integer.parseInt(matcher.group(1));
                    maxNumber = Math.max(maxNumber, number);
                }
            }
        }

        // Generate next code
        int nextNumber = maxNumber + 1;
        return String.format("EMP%04d", nextNumber);
    }

    @Transactional
    public EmployeeResponse createEmployee(EmployeeRequest request) {
        Long tenantId = TenantContext.getTenantId();

        // Validate request data
        validateEmployeeRequest(request, null);

        // Auto-generate employee code if not provided
        if (request.getEmployeeCode() == null || request.getEmployeeCode().trim().isEmpty()) {
            request.setEmployeeCode(generateEmployeeCode(tenantId));
        }

        // Check subscription employee limit
        if (!subscriptionService.checkEmployeeLimit(tenantId)) {
            throw new SubscriptionLimitException(
                "Employee limit reached for your subscription plan. Please upgrade to add more employees."
            );
        }

        // Check if employee code already exists
        employeeRepository.findByTenantIdAndEmployeeCode(tenantId, request.getEmployeeCode())
            .ifPresent(emp -> {
                throw new DuplicateEmployeeCodeException("Employee with code " + request.getEmployeeCode() + " already exists");
            });

        // Verify department exists
        departmentRepository.findByTenantIdAndId(tenantId, request.getDepartmentId())
            .orElseThrow(() -> new EmployeeNotFoundException("Department not found with id: " + request.getDepartmentId()));

        // Verify designation exists
        designationRepository.findByTenantIdAndId(tenantId, request.getDesignationId())
            .orElseThrow(() -> new EmployeeNotFoundException("Designation not found with id: " + request.getDesignationId()));

        // Verify manager exists if provided
        if (request.getManagerId() != null) {
            employeeRepository.findByTenantIdAndId(tenantId, request.getManagerId())
                .orElseThrow(() -> new EmployeeNotFoundException("Manager not found with id: " + request.getManagerId()));
        }

        // Check if user with this email already exists within this tenant
        userRepository.findByTenantIdAndEmail(tenantId, request.getEmail())
            .ifPresent(u -> {
                throw new DuplicateEmailException("User with email " + request.getEmail() + " already exists");
            });

        // Create Employee
        Employee employee = new Employee();
        employee.setTenantId(tenantId);
        mapRequestToEntity(request, employee);
        employee.setCreatedBy(TenantContext.getUserId());
        employee = employeeRepository.save(employee);

        // Generate secure random temporary password
        String temporaryPassword = generateTemporaryPassword();

        // Create User account for employee
        User user = new User();
        user.setTenantId(tenantId);
        user.setEmail(request.getEmail());
        user.setPassword(passwordEncoder.encode(temporaryPassword));
        user.setFirstName(request.getFirstName());
        user.setLastName(request.getLastName());
        user.setIsActive(true);
        user.setMustChangePassword(true); // Force password change on first login
        user.setCreatedBy(TenantContext.getUserId());
        user = userRepository.save(user);

        // Assign EMPLOYEE role
        Role employeeRole = roleRepository.findByTenantIdAndName(tenantId, "EMPLOYEE")
            .orElseGet(() -> {
                Role newRole = new Role();
                newRole.setTenantId(tenantId);
                newRole.setName("EMPLOYEE");
                newRole.setDescription("Employee with limited access");
                newRole.setIsSystemRole(false);
                newRole.setCreatedBy(TenantContext.getUserId());
                return roleRepository.save(newRole);
            });

        user.setRoles(new HashSet<>());
        user.getRoles().add(employeeRole);
        userRepository.save(user);

        // Link user to employee
        employee.setUserId(user.getId());
        employee = employeeRepository.save(employee);

        // Map to response and include temporary password
        EmployeeResponse response = mapToResponse(employee);
        response.setTemporaryPassword(temporaryPassword);
        return response;
    }

    @Transactional(readOnly = true)
    public List<EmployeeResponse> getAllEmployees() {
        Long tenantId = TenantContext.getTenantId();
        return employeeRepository.findByTenantId(tenantId)
            .stream()
            .map(this::mapToResponse)
            .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<EmployeeResponse> getEmployeesByStatus(EmploymentStatus status) {
        Long tenantId = TenantContext.getTenantId();
        return employeeRepository.findByTenantIdAndEmploymentStatus(tenantId, status)
            .stream()
            .map(this::mapToResponse)
            .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<EmployeeResponse> getEmployeesByDepartment(Long departmentId) {
        Long tenantId = TenantContext.getTenantId();
        return employeeRepository.findByTenantIdAndDepartmentId(tenantId, departmentId)
            .stream()
            .map(this::mapToResponse)
            .collect(Collectors.toList());
    }

    // =====================================================
    // PAGINATED METHODS
    // =====================================================

    @Transactional(readOnly = true)
    public PageResponse<EmployeeResponse> getAllEmployeesPaginated(Pageable pageable) {
        Long tenantId = TenantContext.getTenantId();
        Page<Employee> page = employeeRepository.findByTenantId(tenantId, pageable);
        List<EmployeeResponse> content = page.getContent()
            .stream()
            .map(this::mapToResponse)
            .collect(Collectors.toList());
        return PageResponse.from(page, content);
    }

    @Transactional(readOnly = true)
    public PageResponse<EmployeeResponse> getEmployeesByStatusPaginated(EmploymentStatus status, Pageable pageable) {
        Long tenantId = TenantContext.getTenantId();
        Page<Employee> page = employeeRepository.findByTenantIdAndEmploymentStatus(tenantId, status, pageable);
        List<EmployeeResponse> content = page.getContent()
            .stream()
            .map(this::mapToResponse)
            .collect(Collectors.toList());
        return PageResponse.from(page, content);
    }

    @Transactional(readOnly = true)
    public PageResponse<EmployeeResponse> getEmployeesByDepartmentPaginated(Long departmentId, Pageable pageable) {
        Long tenantId = TenantContext.getTenantId();
        Page<Employee> page = employeeRepository.findByTenantIdAndDepartmentId(tenantId, departmentId, pageable);
        List<EmployeeResponse> content = page.getContent()
            .stream()
            .map(this::mapToResponse)
            .collect(Collectors.toList());
        return PageResponse.from(page, content);
    }

    @Transactional(readOnly = true)
    public EmployeeResponse getEmployeeById(Long id) {
        Long tenantId = TenantContext.getTenantId();
        Employee employee = employeeRepository.findByTenantIdAndId(tenantId, id)
            .orElseThrow(() -> new EmployeeNotFoundException("Employee not found with id: " + id));
        return mapToResponse(employee);
    }

    @Transactional(readOnly = true)
    public EmployeeResponse getEmployeeByCode(String employeeCode) {
        Long tenantId = TenantContext.getTenantId();
        Employee employee = employeeRepository.findByTenantIdAndEmployeeCode(tenantId, employeeCode)
            .orElseThrow(() -> new EmployeeNotFoundException("Employee not found with code: " + employeeCode));
        return mapToResponse(employee);
    }

    @Transactional(readOnly = true)
    public EmployeeResponse getEmployeeByUserId(Long userId) {
        Long tenantId = TenantContext.getTenantId();
        Employee employee = employeeRepository.findByTenantIdAndUserId(tenantId, userId)
            .orElseThrow(() -> new EmployeeNotFoundException("Employee profile not found for user: " + userId));
        return mapToResponse(employee);
    }

    @Transactional
    public EmployeeResponse updateEmployeeProfile(Long userId, java.util.Map<String, Object> updates) {
        Long tenantId = TenantContext.getTenantId();

        Employee employee = employeeRepository.findByTenantIdAndUserId(tenantId, userId)
            .orElseThrow(() -> new EmployeeNotFoundException("Employee profile not found"));

        // Only allow updating specific fields for employee self-service
        if (updates.containsKey("phone")) {
            employee.setPhone((String) updates.get("phone"));
        }
        if (updates.containsKey("address")) {
            employee.setAddress((String) updates.get("address"));
        }
        if (updates.containsKey("city")) {
            employee.setCity((String) updates.get("city"));
        }
        if (updates.containsKey("state")) {
            employee.setState((String) updates.get("state"));
        }
        if (updates.containsKey("country")) {
            employee.setCountry((String) updates.get("country"));
        }
        if (updates.containsKey("postalCode")) {
            employee.setPostalCode((String) updates.get("postalCode"));
        }
        if (updates.containsKey("emergencyContactName")) {
            employee.setEmergencyContactName((String) updates.get("emergencyContactName"));
        }
        if (updates.containsKey("emergencyContactPhone")) {
            employee.setEmergencyContactPhone((String) updates.get("emergencyContactPhone"));
        }
        if (updates.containsKey("emergencyContactRelation")) {
            employee.setEmergencyContactRelation((String) updates.get("emergencyContactRelation"));
        }

        employee.setUpdatedBy(userId);
        employee = employeeRepository.save(employee);
        return mapToResponse(employee);
    }

    @Transactional
    public EmployeeResponse updateEmployee(Long id, EmployeeRequest request) {
        Long tenantId = TenantContext.getTenantId();

        // Validate request data
        validateEmployeeRequest(request, id);

        Employee employee = employeeRepository.findByTenantIdAndId(tenantId, id)
            .orElseThrow(() -> new EmployeeNotFoundException("Employee not found with id: " + id));

        // Store current values for lambda expressions (must be effectively final)
        final Long currentUserId = employee.getUserId();
        final String currentEmail = employee.getEmail();
        final String currentEmployeeCode = employee.getEmployeeCode();

        // Check if employee code is being changed and if new code already exists
        if (!currentEmployeeCode.equals(request.getEmployeeCode())) {
            employeeRepository.findByTenantIdAndEmployeeCode(tenantId, request.getEmployeeCode())
                .ifPresent(emp -> {
                    throw new DuplicateEmployeeCodeException("Employee with code " + request.getEmployeeCode() + " already exists");
                });
        }

        // Check if email is being changed and if new email already exists (CRITICAL FIX)
        if (!currentEmail.equals(request.getEmail())) {
            // Check if another employee is using this email
            employeeRepository.findByTenantIdAndEmail(tenantId, request.getEmail())
                .ifPresent(emp -> {
                    if (!emp.getId().equals(id)) {
                        throw new DuplicateEmailException("Employee with email " + request.getEmail() + " already exists");
                    }
                });

            // Check if another user within this tenant is using this email
            userRepository.findByTenantIdAndEmail(tenantId, request.getEmail())
                .ifPresent(u -> {
                    if (!u.getId().equals(currentUserId)) {
                        throw new DuplicateEmailException("User with email " + request.getEmail() + " already exists");
                    }
                });
        }

        // Verify department exists
        departmentRepository.findByTenantIdAndId(tenantId, request.getDepartmentId())
            .orElseThrow(() -> new EmployeeNotFoundException("Department not found with id: " + request.getDepartmentId()));

        // Verify designation exists
        designationRepository.findByTenantIdAndId(tenantId, request.getDesignationId())
            .orElseThrow(() -> new EmployeeNotFoundException("Designation not found with id: " + request.getDesignationId()));

        // Verify manager exists if provided
        if (request.getManagerId() != null) {
            employeeRepository.findByTenantIdAndId(tenantId, request.getManagerId())
                .orElseThrow(() -> new EmployeeNotFoundException("Manager not found with id: " + request.getManagerId()));
        }

        mapRequestToEntity(request, employee);
        employee.setUpdatedBy(TenantContext.getUserId());

        employee = employeeRepository.save(employee);
        return mapToResponse(employee);
    }

    @Transactional
    public void deleteEmployee(Long id) {
        Long tenantId = TenantContext.getTenantId();

        Employee employee = employeeRepository.findByTenantIdAndId(tenantId, id)
            .orElseThrow(() -> new EmployeeNotFoundException("Employee not found with id: " + id));

        // Get the user associated with this employee
        Long userId = employee.getUserId();
        if (userId != null) {
            // Check if this user is the primary owner of the tenant
            Tenant tenant = tenantRepository.findById(tenantId)
                .orElseThrow(() -> new RuntimeException("Tenant not found"));

            if (userId.equals(tenant.getPrimaryOwnerUserId())) {
                throw new TenantOwnerDeletionException(
                    "Cannot delete the primary owner of the tenant. " +
                    "The primary owner is the account that created and owns the subscription. " +
                    "To close your account, please contact support."
                );
            }

            // Check if this is the last tenant admin
            User user = userRepository.findById(userId).orElse(null);
            if (user != null && user.getRoles() != null) {
                boolean isTenantAdmin = user.getRoles().stream()
                    .anyMatch(role -> "TENANT_ADMIN".equals(role.getName()));

                if (isTenantAdmin) {
                    // Count total tenant admins
                    long tenantAdminCount = userRepository.findByTenantId(tenantId).stream()
                        .filter(u -> u.getRoles() != null && u.getRoles().stream()
                            .anyMatch(r -> "TENANT_ADMIN".equals(r.getName())))
                        .count();

                    if (tenantAdminCount <= 1) {
                        throw new LastTenantAdminException(
                            "Cannot delete the last tenant administrator. " +
                            "At least one tenant admin must exist to manage the organization. " +
                            "Please assign another user as admin before deleting this account."
                        );
                    }
                }
            }
        }

        employeeRepository.delete(employee);
    }

    private void mapRequestToEntity(EmployeeRequest request, Employee employee) {
        employee.setUserId(request.getUserId());
        employee.setEmployeeCode(request.getEmployeeCode());
        employee.setFirstName(request.getFirstName());
        employee.setLastName(request.getLastName());
        employee.setEmail(request.getEmail());
        employee.setPhone(request.getPhone());
        employee.setDateOfBirth(request.getDateOfBirth());
        employee.setGender(request.getGender());
        employee.setNationality(request.getNationality());
        employee.setDepartmentId(request.getDepartmentId());
        employee.setDesignationId(request.getDesignationId());
        employee.setManagerId(request.getManagerId());
        employee.setDateOfJoining(request.getDateOfJoining());
        employee.setDateOfLeaving(request.getDateOfLeaving());
        employee.setEmploymentType(request.getEmploymentType());
        employee.setEmploymentStatus(request.getEmploymentStatus() != null ?
            request.getEmploymentStatus() : EmploymentStatus.ACTIVE);
        employee.setBasicSalary(request.getBasicSalary());
        employee.setCurrency(request.getCurrency() != null ? request.getCurrency() : "USD");
        employee.setAddress(request.getAddress());
        employee.setCity(request.getCity());
        employee.setState(request.getState());
        employee.setCountry(request.getCountry());
        employee.setPostalCode(request.getPostalCode());
        employee.setEmergencyContactName(request.getEmergencyContactName());
        employee.setEmergencyContactPhone(request.getEmergencyContactPhone());
        employee.setEmergencyContactRelation(request.getEmergencyContactRelation());
        employee.setBankName(request.getBankName());
        employee.setBankAccount(request.getBankAccount());
        employee.setIfscCode(request.getIfscCode());
        employee.setPan(request.getPan());
    }

    private EmployeeResponse mapToResponse(Employee employee) {
        return EmployeeResponse.builder()
            .id(employee.getId())
            .tenantId(employee.getTenantId())
            .userId(employee.getUserId())
            .employeeCode(employee.getEmployeeCode())
            .firstName(employee.getFirstName())
            .lastName(employee.getLastName())
            .email(employee.getEmail())
            .phone(employee.getPhone())
            .dateOfBirth(employee.getDateOfBirth())
            .gender(employee.getGender())
            .nationality(employee.getNationality())
            .departmentId(employee.getDepartmentId())
            .designationId(employee.getDesignationId())
            .managerId(employee.getManagerId())
            .dateOfJoining(employee.getDateOfJoining())
            .dateOfLeaving(employee.getDateOfLeaving())
            .employmentType(employee.getEmploymentType())
            .employmentStatus(employee.getEmploymentStatus())
            .basicSalary(employee.getBasicSalary())
            .currency(employee.getCurrency())
            .address(employee.getAddress())
            .city(employee.getCity())
            .state(employee.getState())
            .country(employee.getCountry())
            .postalCode(employee.getPostalCode())
            .emergencyContactName(employee.getEmergencyContactName())
            .emergencyContactPhone(employee.getEmergencyContactPhone())
            .emergencyContactRelation(employee.getEmergencyContactRelation())
            .bankName(employee.getBankName())
            .bankAccount(employee.getBankAccount())
            .ifscCode(employee.getIfscCode())
            .pan(employee.getPan())
            .createdAt(employee.getCreatedAt())
            .updatedAt(employee.getUpdatedAt())
            .createdBy(employee.getCreatedBy())
            .updatedBy(employee.getUpdatedBy())
            .build();
    }
}
