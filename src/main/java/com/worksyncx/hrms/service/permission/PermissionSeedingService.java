package com.worksyncx.hrms.service.permission;

import com.worksyncx.hrms.entity.Permission;
import com.worksyncx.hrms.repository.PermissionRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class PermissionSeedingService implements CommandLineRunner {

    private final PermissionRepository permissionRepository;

    @Override
    @Transactional
    public void run(String... args) {
        log.info("Starting permission seeding...");

        List<Permission> permissions = defineSystemPermissions();

        for (Permission permission : permissions) {
            if (!permissionRepository.existsByCode(permission.getCode())) {
                permissionRepository.save(permission);
                log.info("Created permission: {}", permission.getCode());
            }
        }

        log.info("Permission seeding completed. Total permissions: {}", permissionRepository.count());
    }

    private List<Permission> defineSystemPermissions() {
        List<Permission> permissions = new ArrayList<>();

        // DEPARTMENT Permissions
        permissions.add(createPermission("DEPARTMENT:CREATE", "Create Department", "DEPARTMENT", "CREATE",
            "Allows creating new departments"));
        permissions.add(createPermission("DEPARTMENT:READ", "View Departments", "DEPARTMENT", "READ",
            "Allows viewing department information"));
        permissions.add(createPermission("DEPARTMENT:UPDATE", "Update Department", "DEPARTMENT", "UPDATE",
            "Allows updating department information"));
        permissions.add(createPermission("DEPARTMENT:DELETE", "Delete Department", "DEPARTMENT", "DELETE",
            "Allows deleting departments"));

        // DESIGNATION Permissions
        permissions.add(createPermission("DESIGNATION:CREATE", "Create Designation", "DESIGNATION", "CREATE",
            "Allows creating new designations"));
        permissions.add(createPermission("DESIGNATION:READ", "View Designations", "DESIGNATION", "READ",
            "Allows viewing designation information"));
        permissions.add(createPermission("DESIGNATION:UPDATE", "Update Designation", "DESIGNATION", "UPDATE",
            "Allows updating designation information"));
        permissions.add(createPermission("DESIGNATION:DELETE", "Delete Designation", "DESIGNATION", "DELETE",
            "Allows deleting designations"));

        // EMPLOYEE Permissions
        permissions.add(createPermission("EMPLOYEE:CREATE", "Create Employee", "EMPLOYEE", "CREATE",
            "Allows creating new employees"));
        permissions.add(createPermission("EMPLOYEE:READ", "View Employees", "EMPLOYEE", "READ",
            "Allows viewing employee information"));
        permissions.add(createPermission("EMPLOYEE:UPDATE", "Update Employee", "EMPLOYEE", "UPDATE",
            "Allows updating employee information"));
        permissions.add(createPermission("EMPLOYEE:DELETE", "Delete Employee", "EMPLOYEE", "DELETE",
            "Allows deleting employees"));
        permissions.add(createPermission("EMPLOYEE:READ_SELF", "View Own Profile", "EMPLOYEE", "READ_SELF",
            "Allows employees to view their own profile"));
        permissions.add(createPermission("EMPLOYEE:UPDATE_SELF", "Update Own Profile", "EMPLOYEE", "UPDATE_SELF",
            "Allows employees to update their own profile"));

        // ATTENDANCE Permissions
        permissions.add(createPermission("ATTENDANCE:MARK", "Mark Attendance", "ATTENDANCE", "MARK",
            "Allows marking attendance for employees"));
        permissions.add(createPermission("ATTENDANCE:READ", "View Attendance", "ATTENDANCE", "READ",
            "Allows viewing attendance records"));
        permissions.add(createPermission("ATTENDANCE:UPDATE", "Update Attendance", "ATTENDANCE", "UPDATE",
            "Allows updating attendance records"));
        permissions.add(createPermission("ATTENDANCE:CHECK_IN", "Check In", "ATTENDANCE", "CHECK_IN",
            "Allows employees to check in"));
        permissions.add(createPermission("ATTENDANCE:CHECK_OUT", "Check Out", "ATTENDANCE", "CHECK_OUT",
            "Allows employees to check out"));
        permissions.add(createPermission("ATTENDANCE:READ_SELF", "View Own Attendance", "ATTENDANCE", "READ_SELF",
            "Allows employees to view their own attendance"));

        // LEAVE Permissions
        permissions.add(createPermission("LEAVE:CREATE", "Create Leave Type", "LEAVE", "CREATE",
            "Allows creating new leave types"));
        permissions.add(createPermission("LEAVE:READ", "View Leaves", "LEAVE", "READ",
            "Allows viewing leave information"));
        permissions.add(createPermission("LEAVE:UPDATE", "Update Leave Type", "LEAVE", "UPDATE",
            "Allows updating leave types"));
        permissions.add(createPermission("LEAVE:DELETE", "Delete Leave Type", "LEAVE", "DELETE",
            "Allows deleting leave types"));
        permissions.add(createPermission("LEAVE:REQUEST", "Request Leave", "LEAVE", "REQUEST",
            "Allows employees to request leave"));
        permissions.add(createPermission("LEAVE:APPROVE", "Approve Leave", "LEAVE", "APPROVE",
            "Allows approving leave requests"));
        permissions.add(createPermission("LEAVE:REJECT", "Reject Leave", "LEAVE", "REJECT",
            "Allows rejecting leave requests"));
        permissions.add(createPermission("LEAVE:READ_SELF", "View Own Leaves", "LEAVE", "READ_SELF",
            "Allows employees to view their own leave requests"));

        // SHIFT Permissions
        permissions.add(createPermission("SHIFT:CREATE", "Create Shift", "SHIFT", "CREATE",
            "Allows creating new shifts"));
        permissions.add(createPermission("SHIFT:READ", "View Shifts", "SHIFT", "READ",
            "Allows viewing shift information"));
        permissions.add(createPermission("SHIFT:UPDATE", "Update Shift", "SHIFT", "UPDATE",
            "Allows updating shift details"));
        permissions.add(createPermission("SHIFT:DELETE", "Delete Shift", "SHIFT", "DELETE",
            "Allows deleting shifts"));
        permissions.add(createPermission("SHIFT:ASSIGN", "Assign Shifts", "SHIFT", "ASSIGN",
            "Allows assigning shifts to employees"));

        // PAYROLL Permissions
        permissions.add(createPermission("PAYROLL:CREATE", "Create Payroll", "PAYROLL", "CREATE",
            "Allows creating payroll records"));
        permissions.add(createPermission("PAYROLL:READ", "View Payroll", "PAYROLL", "READ",
            "Allows viewing payroll information"));
        permissions.add(createPermission("PAYROLL:UPDATE", "Update Payroll", "PAYROLL", "UPDATE",
            "Allows updating payroll records"));
        permissions.add(createPermission("PAYROLL:DELETE", "Delete Payroll", "PAYROLL", "DELETE",
            "Allows deleting payroll records"));
        permissions.add(createPermission("PAYROLL:PROCESS", "Process Payroll", "PAYROLL", "PROCESS",
            "Allows processing payroll for employees"));
        permissions.add(createPermission("PAYROLL:APPROVE", "Approve Payroll", "PAYROLL", "APPROVE",
            "Allows approving payroll records"));
        permissions.add(createPermission("PAYROLL:READ_SELF", "View Own Payslips", "PAYROLL", "READ_SELF",
            "Allows employees to view their own payslips"));

        // ROLE Permissions
        permissions.add(createPermission("ROLE:CREATE", "Create Role", "ROLE", "CREATE",
            "Allows creating new roles"));
        permissions.add(createPermission("ROLE:READ", "View Roles", "ROLE", "READ",
            "Allows viewing role information"));
        permissions.add(createPermission("ROLE:UPDATE", "Update Role", "ROLE", "UPDATE",
            "Allows updating roles"));
        permissions.add(createPermission("ROLE:DELETE", "Delete Role", "ROLE", "DELETE",
            "Allows deleting roles"));
        permissions.add(createPermission("ROLE:ASSIGN_PERMISSION", "Assign Permissions to Role", "ROLE", "ASSIGN_PERMISSION",
            "Allows assigning permissions to roles"));

        // PERMISSION Permissions
        permissions.add(createPermission("PERMISSION:CREATE", "Create Permission", "PERMISSION", "CREATE",
            "Allows creating custom permissions"));
        permissions.add(createPermission("PERMISSION:READ", "View Permissions", "PERMISSION", "READ",
            "Allows viewing permission information"));
        permissions.add(createPermission("PERMISSION:UPDATE", "Update Permission", "PERMISSION", "UPDATE",
            "Allows updating permissions"));
        permissions.add(createPermission("PERMISSION:DELETE", "Delete Permission", "PERMISSION", "DELETE",
            "Allows deleting custom permissions"));

        // SUBSCRIPTION Permissions
        permissions.add(createPermission("SUBSCRIPTION:CREATE", "Create Subscription", "SUBSCRIPTION", "CREATE",
            "Allows creating subscriptions"));
        permissions.add(createPermission("SUBSCRIPTION:READ", "View Subscription", "SUBSCRIPTION", "READ",
            "Allows viewing subscription information"));
        permissions.add(createPermission("SUBSCRIPTION:UPDATE", "Update Subscription", "SUBSCRIPTION", "UPDATE",
            "Allows updating subscription details"));
        permissions.add(createPermission("SUBSCRIPTION:DELETE", "Delete Subscription", "SUBSCRIPTION", "DELETE",
            "Allows deleting subscriptions"));

        return permissions;
    }

    private Permission createPermission(String code, String name, String module, String action, String description) {
        Permission permission = new Permission();
        permission.setCode(code);
        permission.setName(name);
        permission.setModule(module);
        permission.setAction(action);
        permission.setDescription(description);
        return permission;
    }
}
