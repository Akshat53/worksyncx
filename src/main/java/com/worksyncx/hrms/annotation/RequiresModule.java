package com.worksyncx.hrms.annotation;

import com.worksyncx.hrms.enums.Module;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Annotation to enforce subscription module access on controller methods.
 * When applied, the method will only execute if the tenant's subscription includes the specified module.
 *
 * Example usage:
 * <pre>
 * {@code
 * @PostMapping
 * @RequiresModule(Module.PAYROLL)
 * public ResponseEntity<PayrollResponse> createPayroll(@RequestBody PayrollRequest request) {
 *     // This method will only execute if tenant has PAYROLL module in their subscription
 *     return ResponseEntity.ok(payrollService.createPayroll(request));
 * }
 * }
 * </pre>
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface RequiresModule {
    /**
     * The module required to execute this method
     */
    Module value();
}
