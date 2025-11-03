package com.worksyncx.hrms.service.auth;

import com.worksyncx.hrms.dto.auth.AuthResponse;
import com.worksyncx.hrms.dto.auth.LoginRequest;
import com.worksyncx.hrms.dto.auth.RegisterRequest;
import com.worksyncx.hrms.entity.Role;
import com.worksyncx.hrms.entity.Subscription;
import com.worksyncx.hrms.entity.Tenant;
import com.worksyncx.hrms.entity.User;
import com.worksyncx.hrms.enums.BillingCycle;
import com.worksyncx.hrms.enums.SubscriptionPlan;
import com.worksyncx.hrms.enums.SubscriptionStatus;
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

    public AuthResponse login(LoginRequest loginRequest) {
        Authentication authentication = authenticationManager.authenticate(
            new UsernamePasswordAuthenticationToken(loginRequest.getEmail(), loginRequest.getPassword())
        );

        SecurityContextHolder.getContext().setAuthentication(authentication);
        String jwt = jwtUtils.generateJwtToken(authentication);

        User user = (User) authentication.getPrincipal();
        List<String> roles = user.getAuthorities().stream()
            .map(GrantedAuthority::getAuthority)
            .filter(auth -> auth.startsWith("ROLE_"))
            .map(auth -> auth.substring(5))
            .collect(Collectors.toList());

        return new AuthResponse(
            jwt,
            user.getId(),
            user.getTenantId(),
            user.getEmail(),
            user.getFirstName(),
            user.getLastName(),
            roles
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

        // Create Subscription (Free trial)
        Subscription subscription = new Subscription();
        subscription.setTenant(tenant);
        subscription.setPlan(SubscriptionPlan.STARTER);
        subscription.setStatus(SubscriptionStatus.ACTIVE);
        subscription.setStartDate(LocalDate.now());
        subscription.setEndDate(LocalDate.now().plusDays(30)); // 30 days trial
        subscription.setMaxEmployees(10);
        subscription.setModules(Set.of("employee", "attendance", "leave"));
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

        // Generate token
        Authentication authentication = authenticationManager.authenticate(
            new UsernamePasswordAuthenticationToken(request.getEmail(), request.getPassword())
        );

        String jwt = jwtUtils.generateJwtToken(authentication);

        return new AuthResponse(
            jwt,
            user.getId(),
            user.getTenantId(),
            user.getEmail(),
            user.getFirstName(),
            user.getLastName(),
            List.of("TENANT_ADMIN")
        );
    }
}
