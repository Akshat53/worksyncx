package com.worksyncx.hrms.service.auth;

import com.worksyncx.hrms.dto.auth.AuthPermissionDto;
import com.worksyncx.hrms.dto.auth.AuthResponse;
import com.worksyncx.hrms.dto.auth.AuthRoleDto;
import com.worksyncx.hrms.dto.auth.ChangePasswordRequest;
import com.worksyncx.hrms.dto.auth.LoginRequest;
import com.worksyncx.hrms.dto.auth.RegisterRequest;
import com.worksyncx.hrms.entity.Department;
import com.worksyncx.hrms.entity.Designation;
import com.worksyncx.hrms.entity.Employee;
import com.worksyncx.hrms.entity.Role;
import com.worksyncx.hrms.entity.Subscription;
import com.worksyncx.hrms.entity.Tenant;
import com.worksyncx.hrms.entity.User;
import com.worksyncx.hrms.enums.BillingCycle;
import com.worksyncx.hrms.enums.EmploymentStatus;
import com.worksyncx.hrms.enums.EmploymentType;
import com.worksyncx.hrms.enums.SubscriptionPlan;
import com.worksyncx.hrms.enums.SubscriptionStatus;
import com.worksyncx.hrms.repository.DepartmentRepository;
import com.worksyncx.hrms.repository.DesignationRepository;
import com.worksyncx.hrms.repository.EmployeeRepository;
import com.worksyncx.hrms.repository.RoleRepository;
import com.worksyncx.hrms.repository.TenantRepository;
import com.worksyncx.hrms.repository.UserRepository;
import com.worksyncx.hrms.security.jwt.JwtUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Service
public class AuthService {

    @Autowired
    private AuthenticationManager authenticationManager;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private TenantRepository tenantRepository;

    @Autowired
    private RoleRepository roleRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private JwtUtils jwtUtils;

    @Autowired
    private DepartmentRepository departmentRepository;

    @Autowired
    private DesignationRepository designationRepository;

    @Autowired
    private EmployeeRepository employeeRepository;

    public AuthResponse login(LoginRequest loginRequest) {
        Authentication authentication = authenticationManager.authenticate(
            new UsernamePasswordAuthenticationToken(loginRequest.getEmail(), loginRequest.getPassword())
        );

        SecurityContextHolder.getContext().setAuthentication(authentication);
        String jwt = jwtUtils.generateJwtToken(authentication);

        User user = (User) authentication.getPrincipal();
        List<AuthRoleDto> roleDtos = convertRolesToDtos(user.getRoles());

        return new AuthResponse(
            jwt,
            user.getId(),
            user.getTenantId(),
            user.getEmail(),
            user.getFirstName(),
            user.getLastName(),
            roleDtos,
            user.getMustChangePassword()
        );
    }

    @Transactional
    public AuthResponse register(RegisterRequest request) {
        // Check if email already exists
        if (tenantRepository.existsByEmail(request.getEmail())) {
            throw new RuntimeException("Email is already registered!");
        }

        // Create Tenant
        Tenant tenant = new Tenant();
        tenant.setName(request.getCompanyName());
        tenant.setEmail(request.getEmail());
        tenant.setPhone(request.getPhone());
        tenant.setWebsite(request.getWebsite());
        tenant.setIndustry(request.getIndustry());
        tenant.setEmployeeCount(1);
        tenant.setIsActive(true);
        tenant = tenantRepository.save(tenant);

        // Create Subscription (Free plan)
        Subscription subscription = new Subscription();
        subscription.setTenant(tenant);
        subscription.setPlan(SubscriptionPlan.FREE);
        subscription.setStatus(SubscriptionStatus.ACTIVE);
        subscription.setStartDate(LocalDate.now());
        subscription.setEndDate(null); // Free plan never expires
        subscription.setMaxEmployees(5);
        subscription.setModules(Set.of("DEPARTMENTS", "DESIGNATIONS", "EMPLOYEES"));
        subscription.setFeatures(new HashSet<>());
        subscription.setBillingCycle(BillingCycle.MONTHLY);
        subscription.setAmount(BigDecimal.ZERO);
        subscription.setAutoRenewal(false);
        tenant.setSubscription(subscription);
        tenant = tenantRepository.save(tenant);

        final Long tenantId = tenant.getId();

        // Create Admin User
        User user = new User();
        user.setTenantId(tenantId);
        user.setEmail(request.getEmail());
        user.setPassword(passwordEncoder.encode(request.getPassword()));
        user.setFirstName(request.getFirstName());
        user.setLastName(request.getLastName());
        user.setIsActive(true);

        // Assign TENANT_ADMIN role
        Role adminRole = roleRepository.findByTenantIdAndName(tenantId, "TENANT_ADMIN")
            .orElseGet(() -> {
                Role newRole = new Role();
                newRole.setTenantId(tenantId);
                newRole.setName("TENANT_ADMIN");
                newRole.setDescription("Tenant Administrator");
                newRole.setIsSystemRole(true);
                return roleRepository.save(newRole);
            });

        user.setRoles(Set.of(adminRole));
        user = userRepository.save(user);
        final Long userId = user.getId();

        // Create default department if none exists
        Department department = departmentRepository.findByTenantIdAndCode(tenantId, "ADMIN")
            .orElseGet(() -> {
                Department newDept = new Department();
                newDept.setTenantId(tenantId);
                newDept.setName("Administration");
                newDept.setCode("ADMIN");
                newDept.setDescription("Default administration department");
                newDept.setIsActive(true);
                newDept.setCreatedBy(userId);
                return departmentRepository.save(newDept);
            });

        // Create default designation if none exists
        Designation designation = designationRepository.findByTenantIdAndDepartmentId(tenantId, department.getId())
            .stream()
            .findFirst()
            .orElseGet(() -> {
                Designation newDesig = new Designation();
                newDesig.setTenantId(tenantId);
                newDesig.setName("Company Admin");
                newDesig.setCode("CADMIN");
                newDesig.setDescription("Company Administrator");
                newDesig.setDepartmentId(department.getId());
                newDesig.setIsActive(true);
                newDesig.setCreatedBy(userId);
                return designationRepository.save(newDesig);
            });

        // Create employee record for the admin user
        Employee employee = new Employee();
        employee.setTenantId(tenantId);
        employee.setUserId(user.getId());
        employee.setEmployeeCode("EMP001");
        employee.setFirstName(request.getFirstName());
        employee.setLastName(request.getLastName());
        employee.setEmail(request.getEmail());
        employee.setPhone(request.getPhone());
        employee.setDepartmentId(department.getId());
        employee.setDesignationId(designation.getId());
        employee.setDateOfJoining(LocalDate.now());
        employee.setEmploymentType(EmploymentType.PERMANENT);
        employee.setEmploymentStatus(EmploymentStatus.ACTIVE);
        employee.setBasicSalary(BigDecimal.ZERO);
        employee.setCurrency("USD");
        employee.setCreatedBy(user.getId());
        employeeRepository.save(employee);

        // Generate token
        Authentication authentication = authenticationManager.authenticate(
            new UsernamePasswordAuthenticationToken(request.getEmail(), request.getPassword())
        );

        String jwt = jwtUtils.generateJwtToken(authentication);

        // Fetch updated user to get roles with permissions
        User updatedUser = (User) authentication.getPrincipal();
        List<AuthRoleDto> roleDtos = convertRolesToDtos(updatedUser.getRoles());

        return new AuthResponse(
            jwt,
            user.getId(),
            user.getTenantId(),
            user.getEmail(),
            user.getFirstName(),
            user.getLastName(),
            roleDtos,
            false // Admin doesn't need to change password
        );
    }

    @Transactional
    public void changePassword(Long userId, ChangePasswordRequest request) {
        // Validate that new password and confirm password match
        if (!request.getNewPassword().equals(request.getConfirmPassword())) {
            throw new RuntimeException("New password and confirm password do not match");
        }

        // Get the user
        User user = userRepository.findById(userId)
            .orElseThrow(() -> new RuntimeException("User not found"));

        // Verify current password
        if (!passwordEncoder.matches(request.getCurrentPassword(), user.getPassword())) {
            throw new RuntimeException("Current password is incorrect");
        }

        // Update password
        user.setPassword(passwordEncoder.encode(request.getNewPassword()));
        user.setMustChangePassword(false); // Reset the flag after password change
        userRepository.save(user);
    }

    /**
     * Helper method to convert user roles to AuthRoleDto with permissions
     */
    private List<AuthRoleDto> convertRolesToDtos(Set<Role> roles) {
        return roles.stream()
            .map(role -> AuthRoleDto.builder()
                .id(role.getId())
                .name(role.getName())
                .description(role.getDescription())
                .permissions(
                    role.getPermissions().stream()
                        .map(permission -> AuthPermissionDto.builder()
                            .id(permission.getId())
                            .code(permission.getCode())
                            .name(permission.getName())
                            .module(permission.getModule())
                            .action(permission.getAction())
                            .build())
                        .collect(Collectors.toList())
                )
                .build())
            .collect(Collectors.toList());
    }
}
