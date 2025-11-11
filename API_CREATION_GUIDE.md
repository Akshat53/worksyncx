# API Creation Guide - WorkSyncX HRMS

**Complete Guide for Creating New APIs with Role-Based Access Control**

## Table of Contents
1. [Overview](#overview)
2. [Architecture Pattern](#architecture-pattern)
3. [Step-by-Step Guide](#step-by-step-guide)
4. [Role & Permission Setup](#role--permission-setup)
5. [Examples](#examples)
6. [Best Practices](#best-practices)
7. [Testing Guide](#testing-guide)

---

## Overview

This guide explains the **standard pattern** for creating new APIs in the WorkSyncX HRMS system. All APIs follow a **layered architecture** with built-in **role-based access control (RBAC)**.

### Key Principles
- **Security First**: Every API endpoint is secured by default
- **Role-Based Access**: Access controlled by roles and permissions
- **Tenant Isolation**: All data is scoped to the tenant
- **Consistent Structure**: Follow the same pattern across all APIs
- **Validation**: Input validation at every layer

---

## Architecture Pattern

```
┌─────────────────────────────────────────────────────────────┐
│                    CLIENT (React Frontend)                   │
└────────────────────┬────────────────────────────────────────┘
                     │ HTTP Request (JWT Token in Header)
                     ▼
┌─────────────────────────────────────────────────────────────┐
│  CONTROLLER LAYER                                            │
│  - Handles HTTP requests                                     │
│  - Validates input (DTOs)                                    │
│  - Checks permissions (@PreAuthorize)                        │
│  - Maps requests to service calls                            │
└────────────────────┬────────────────────────────────────────┘
                     │
                     ▼
┌─────────────────────────────────────────────────────────────┐
│  SERVICE LAYER                                               │
│  - Business logic                                            │
│  - Tenant isolation                                          │
│  - Validation rules                                          │
│  - Transaction management                                    │
└────────────────────┬────────────────────────────────────────┘
                     │
                     ▼
┌─────────────────────────────────────────────────────────────┐
│  REPOSITORY LAYER                                            │
│  - Database operations                                       │
│  - Query optimization                                        │
│  - Custom queries                                            │
└────────────────────┬────────────────────────────────────────┘
                     │
                     ▼
┌─────────────────────────────────────────────────────────────┐
│                    DATABASE (PostgreSQL)                     │
└─────────────────────────────────────────────────────────────┘
```

---

## Step-by-Step Guide

### Step 1: Define the Entity (Database Model)

**Location**: `src/main/java/com/worksyncx/hrms/entity/`

```java
package com.worksyncx.hrms.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;
import java.time.LocalDateTime;

@Entity
@Table(name = "products") // Change table name
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Product {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private Long tenantId; // ALWAYS include for multi-tenancy

    @Column(nullable = false, length = 100)
    private String name;

    @Column(length = 500)
    private String description;

    @Column(nullable = false)
    private Double price;

    @Column(nullable = false)
    private Integer stockQuantity;

    @Column(length = 50)
    private String category;

    @Column(nullable = false)
    private Boolean isActive = true;

    @CreationTimestamp
    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    private LocalDateTime updatedAt;
}
```

**Key Points:**
- ✅ Always include `tenantId` for multi-tenancy
- ✅ Use `@Column(nullable = false)` for required fields
- ✅ Add `createdAt` and `updatedAt` timestamps
- ✅ Use meaningful column names and constraints

---

### Step 2: Create DTOs (Data Transfer Objects)

**Location**: `src/main/java/com/worksyncx/hrms/dto/product/`

#### Request DTO

```java
package com.worksyncx.hrms.dto.product;

import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ProductRequest {

    @NotBlank(message = "Product name is required")
    @Size(min = 3, max = 100, message = "Name must be between 3 and 100 characters")
    private String name;

    @Size(max = 500, message = "Description cannot exceed 500 characters")
    private String description;

    @NotNull(message = "Price is required")
    @DecimalMin(value = "0.01", message = "Price must be greater than 0")
    private Double price;

    @NotNull(message = "Stock quantity is required")
    @Min(value = 0, message = "Stock quantity cannot be negative")
    private Integer stockQuantity;

    @Size(max = 50, message = "Category cannot exceed 50 characters")
    private String category;

    private Boolean isActive = true;
}
```

#### Response DTO

```java
package com.worksyncx.hrms.dto.product;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ProductResponse {
    private Long id;
    private Long tenantId;
    private String name;
    private String description;
    private Double price;
    private Integer stockQuantity;
    private String category;
    private Boolean isActive;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
```

**Key Points:**
- ✅ Use validation annotations (`@NotBlank`, `@NotNull`, `@Size`, etc.)
- ✅ Separate Request and Response DTOs
- ✅ Never expose sensitive data in Response DTOs
- ✅ Include all necessary fields for the frontend

---

### Step 3: Create Repository

**Location**: `src/main/java/com/worksyncx/hrms/repository/`

```java
package com.worksyncx.hrms.repository;

import com.worksyncx.hrms.entity.Product;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ProductRepository extends JpaRepository<Product, Long> {

    // Find all products by tenant
    List<Product> findByTenantId(Long tenantId);

    // Find active products by tenant
    List<Product> findByTenantIdAndIsActive(Long tenantId, Boolean isActive);

    // Find by ID and tenant (for security)
    Optional<Product> findByIdAndTenantId(Long id, Long tenantId);

    // Find by category and tenant
    List<Product> findByTenantIdAndCategory(Long tenantId, String category);

    // Check if product name exists for tenant
    boolean existsByTenantIdAndNameIgnoreCase(Long tenantId, String name);

    // Custom query with JOIN (if needed)
    @Query("SELECT p FROM Product p WHERE p.tenantId = :tenantId AND p.price BETWEEN :minPrice AND :maxPrice")
    List<Product> findProductsByPriceRange(
        @Param("tenantId") Long tenantId,
        @Param("minPrice") Double minPrice,
        @Param("maxPrice") Double maxPrice
    );
}
```

**Key Points:**
- ✅ ALWAYS filter by `tenantId` for security
- ✅ Use `Optional` for single-result queries
- ✅ Create custom methods using Spring Data naming conventions
- ✅ Use `@Query` for complex queries

---

### Step 4: Create Service Interface

**Location**: `src/main/java/com/worksyncx/hrms/service/product/`

```java
package com.worksyncx.hrms.service.product;

import com.worksyncx.hrms.dto.product.ProductRequest;
import com.worksyncx.hrms.dto.product.ProductResponse;
import java.util.List;

public interface ProductService {

    /**
     * Create a new product
     * @param request Product creation request
     * @param tenantId Tenant ID from authenticated user
     * @return Created product
     */
    ProductResponse createProduct(ProductRequest request, Long tenantId);

    /**
     * Get product by ID
     * @param id Product ID
     * @param tenantId Tenant ID for security check
     * @return Product details
     */
    ProductResponse getProductById(Long id, Long tenantId);

    /**
     * Get all products for a tenant
     * @param tenantId Tenant ID
     * @return List of products
     */
    List<ProductResponse> getAllProducts(Long tenantId);

    /**
     * Update product
     * @param id Product ID
     * @param request Update request
     * @param tenantId Tenant ID for security check
     * @return Updated product
     */
    ProductResponse updateProduct(Long id, ProductRequest request, Long tenantId);

    /**
     * Delete product (soft delete)
     * @param id Product ID
     * @param tenantId Tenant ID for security check
     */
    void deleteProduct(Long id, Long tenantId);

    /**
     * Get products by category
     * @param category Category name
     * @param tenantId Tenant ID
     * @return List of products in category
     */
    List<ProductResponse> getProductsByCategory(String category, Long tenantId);
}
```

---

### Step 5: Implement Service

**Location**: `src/main/java/com/worksyncx/hrms/service/product/`

```java
package com.worksyncx.hrms.service.product;

import com.worksyncx.hrms.dto.product.ProductRequest;
import com.worksyncx.hrms.dto.product.ProductResponse;
import com.worksyncx.hrms.entity.Product;
import com.worksyncx.hrms.exception.ResourceNotFoundException;
import com.worksyncx.hrms.exception.BadRequestException;
import com.worksyncx.hrms.repository.ProductRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class ProductServiceImpl implements ProductService {

    private final ProductRepository productRepository;

    @Override
    @Transactional
    public ProductResponse createProduct(ProductRequest request, Long tenantId) {
        log.info("Creating product for tenant: {}", tenantId);

        // Validate: Check if product name already exists for tenant
        if (productRepository.existsByTenantIdAndNameIgnoreCase(tenantId, request.getName())) {
            throw new BadRequestException("Product with name '" + request.getName() + "' already exists");
        }

        // Create entity
        Product product = Product.builder()
                .tenantId(tenantId)
                .name(request.getName())
                .description(request.getDescription())
                .price(request.getPrice())
                .stockQuantity(request.getStockQuantity())
                .category(request.getCategory())
                .isActive(request.getIsActive() != null ? request.getIsActive() : true)
                .build();

        // Save to database
        Product savedProduct = productRepository.save(product);

        log.info("Product created with ID: {} for tenant: {}", savedProduct.getId(), tenantId);

        // Convert to DTO and return
        return mapToResponse(savedProduct);
    }

    @Override
    @Transactional(readOnly = true)
    public ProductResponse getProductById(Long id, Long tenantId) {
        log.info("Fetching product ID: {} for tenant: {}", id, tenantId);

        Product product = productRepository.findByIdAndTenantId(id, tenantId)
                .orElseThrow(() -> new ResourceNotFoundException("Product not found with ID: " + id));

        return mapToResponse(product);
    }

    @Override
    @Transactional(readOnly = true)
    public List<ProductResponse> getAllProducts(Long tenantId) {
        log.info("Fetching all products for tenant: {}", tenantId);

        List<Product> products = productRepository.findByTenantId(tenantId);

        return products.stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public ProductResponse updateProduct(Long id, ProductRequest request, Long tenantId) {
        log.info("Updating product ID: {} for tenant: {}", id, tenantId);

        // Find existing product
        Product product = productRepository.findByIdAndTenantId(id, tenantId)
                .orElseThrow(() -> new ResourceNotFoundException("Product not found with ID: " + id));

        // Check if new name conflicts with another product
        if (!product.getName().equalsIgnoreCase(request.getName()) &&
            productRepository.existsByTenantIdAndNameIgnoreCase(tenantId, request.getName())) {
            throw new BadRequestException("Product with name '" + request.getName() + "' already exists");
        }

        // Update fields
        product.setName(request.getName());
        product.setDescription(request.getDescription());
        product.setPrice(request.getPrice());
        product.setStockQuantity(request.getStockQuantity());
        product.setCategory(request.getCategory());
        if (request.getIsActive() != null) {
            product.setIsActive(request.getIsActive());
        }

        // Save updated product
        Product updatedProduct = productRepository.save(product);

        log.info("Product updated: {}", updatedProduct.getId());

        return mapToResponse(updatedProduct);
    }

    @Override
    @Transactional
    public void deleteProduct(Long id, Long tenantId) {
        log.info("Deleting product ID: {} for tenant: {}", id, tenantId);

        // Find existing product
        Product product = productRepository.findByIdAndTenantId(id, tenantId)
                .orElseThrow(() -> new ResourceNotFoundException("Product not found with ID: " + id));

        // Soft delete (set isActive to false)
        product.setIsActive(false);
        productRepository.save(product);

        // For hard delete, use: productRepository.delete(product);

        log.info("Product soft-deleted: {}", id);
    }

    @Override
    @Transactional(readOnly = true)
    public List<ProductResponse> getProductsByCategory(String category, Long tenantId) {
        log.info("Fetching products by category: {} for tenant: {}", category, tenantId);

        List<Product> products = productRepository.findByTenantIdAndCategory(tenantId, category);

        return products.stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    // Helper method to map Entity to DTO
    private ProductResponse mapToResponse(Product product) {
        return ProductResponse.builder()
                .id(product.getId())
                .tenantId(product.getTenantId())
                .name(product.getName())
                .description(product.getDescription())
                .price(product.getPrice())
                .stockQuantity(product.getStockQuantity())
                .category(product.getCategory())
                .isActive(product.getIsActive())
                .createdAt(product.getCreatedAt())
                .updatedAt(product.getUpdatedAt())
                .build();
    }
}
```

**Key Points:**
- ✅ Always validate tenant ID
- ✅ Use `@Transactional` for write operations
- ✅ Use `@Transactional(readOnly = true)` for read operations
- ✅ Log important operations
- ✅ Throw appropriate exceptions
- ✅ Prefer soft delete over hard delete

---

### Step 6: Create Controller

**Location**: `src/main/java/com/worksyncx/hrms/controller/`

```java
package com.worksyncx.hrms.controller;

import com.worksyncx.hrms.dto.product.ProductRequest;
import com.worksyncx.hrms.dto.product.ProductResponse;
import com.worksyncx.hrms.entity.User;
import com.worksyncx.hrms.service.product.ProductService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/products")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Products", description = "Product management APIs")
@SecurityRequirement(name = "bearerAuth")
public class ProductController {

    private final ProductService productService;

    /**
     * CREATE - Add new product
     * Permission: PRODUCT:CREATE or TENANT_ADMIN role
     */
    @PostMapping
    @PreAuthorize("hasRole('TENANT_ADMIN') or hasAuthority('PRODUCT:CREATE')")
    @Operation(summary = "Create a new product", description = "Creates a new product for the tenant")
    public ResponseEntity<ProductResponse> createProduct(
            @Valid @RequestBody ProductRequest request,
            @AuthenticationPrincipal User user) {

        log.info("Create product request from user: {}", user.getEmail());

        ProductResponse response = productService.createProduct(request, user.getTenantId());

        return new ResponseEntity<>(response, HttpStatus.CREATED);
    }

    /**
     * READ - Get product by ID
     * Permission: PRODUCT:READ or TENANT_ADMIN role
     */
    @GetMapping("/{id}")
    @PreAuthorize("hasRole('TENANT_ADMIN') or hasAuthority('PRODUCT:READ')")
    @Operation(summary = "Get product by ID", description = "Retrieves a product by its ID")
    public ResponseEntity<ProductResponse> getProductById(
            @PathVariable Long id,
            @AuthenticationPrincipal User user) {

        log.info("Get product {} request from user: {}", id, user.getEmail());

        ProductResponse response = productService.getProductById(id, user.getTenantId());

        return ResponseEntity.ok(response);
    }

    /**
     * READ - Get all products
     * Permission: PRODUCT:READ or TENANT_ADMIN role
     */
    @GetMapping
    @PreAuthorize("hasRole('TENANT_ADMIN') or hasAuthority('PRODUCT:READ')")
    @Operation(summary = "Get all products", description = "Retrieves all products for the tenant")
    public ResponseEntity<List<ProductResponse>> getAllProducts(
            @AuthenticationPrincipal User user) {

        log.info("Get all products request from user: {}", user.getEmail());

        List<ProductResponse> responses = productService.getAllProducts(user.getTenantId());

        return ResponseEntity.ok(responses);
    }

    /**
     * READ - Get products by category
     * Permission: PRODUCT:READ or TENANT_ADMIN role
     */
    @GetMapping("/category/{category}")
    @PreAuthorize("hasRole('TENANT_ADMIN') or hasAuthority('PRODUCT:READ')")
    @Operation(summary = "Get products by category", description = "Retrieves products by category")
    public ResponseEntity<List<ProductResponse>> getProductsByCategory(
            @PathVariable String category,
            @AuthenticationPrincipal User user) {

        log.info("Get products by category {} request from user: {}", category, user.getEmail());

        List<ProductResponse> responses = productService.getProductsByCategory(category, user.getTenantId());

        return ResponseEntity.ok(responses);
    }

    /**
     * UPDATE - Update product
     * Permission: PRODUCT:UPDATE or TENANT_ADMIN role
     */
    @PutMapping("/{id}")
    @PreAuthorize("hasRole('TENANT_ADMIN') or hasAuthority('PRODUCT:UPDATE')")
    @Operation(summary = "Update product", description = "Updates an existing product")
    public ResponseEntity<ProductResponse> updateProduct(
            @PathVariable Long id,
            @Valid @RequestBody ProductRequest request,
            @AuthenticationPrincipal User user) {

        log.info("Update product {} request from user: {}", id, user.getEmail());

        ProductResponse response = productService.updateProduct(id, request, user.getTenantId());

        return ResponseEntity.ok(response);
    }

    /**
     * DELETE - Delete product
     * Permission: PRODUCT:DELETE or TENANT_ADMIN role
     */
    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('TENANT_ADMIN') or hasAuthority('PRODUCT:DELETE')")
    @Operation(summary = "Delete product", description = "Deletes a product (soft delete)")
    public ResponseEntity<Void> deleteProduct(
            @PathVariable Long id,
            @AuthenticationPrincipal User user) {

        log.info("Delete product {} request from user: {}", id, user.getEmail());

        productService.deleteProduct(id, user.getTenantId());

        return ResponseEntity.noContent().build();
    }
}
```

**Key Points:**
- ✅ Use `@PreAuthorize` for permission checks
- ✅ Extract `tenantId` from authenticated user
- ✅ Use `@Valid` for request validation
- ✅ Return appropriate HTTP status codes
- ✅ Add Swagger documentation

---

## Role & Permission Setup

### Step 1: Add Permission to Database

Run this SQL to add the new permission:

```sql
-- Add PRODUCT module permissions
INSERT INTO permissions (module, action, code, name, description, created_at)
VALUES
    ('PRODUCT', 'CREATE', 'PRODUCT:CREATE', 'Create Product', 'Can create new products', NOW()),
    ('PRODUCT', 'READ', 'PRODUCT:READ', 'Read Product', 'Can view products', NOW()),
    ('PRODUCT', 'UPDATE', 'PRODUCT:UPDATE', 'Update Product', 'Can update products', NOW()),
    ('PRODUCT', 'DELETE', 'PRODUCT:DELETE', 'Delete Product', 'Can delete products', NOW());
```

### Step 2: Assign Permissions to Roles

```sql
-- Assign all PRODUCT permissions to TENANT_ADMIN role
INSERT INTO role_permissions (role_id, permission_id)
SELECT r.id, p.id
FROM roles r
CROSS JOIN permissions p
WHERE r.name = 'TENANT_ADMIN'
AND p.module = 'PRODUCT';

-- Assign READ permission to EMPLOYEE role
INSERT INTO role_permissions (role_id, permission_id)
SELECT r.id, p.id
FROM roles r
CROSS JOIN permissions p
WHERE r.name = 'EMPLOYEE'
AND p.code = 'PRODUCT:READ';

-- Assign CREATE, READ, UPDATE to HR_MANAGER role
INSERT INTO role_permissions (role_id, permission_id)
SELECT r.id, p.id
FROM roles r
CROSS JOIN permissions p
WHERE r.name = 'HR_MANAGER'
AND p.module = 'PRODUCT'
AND p.action IN ('CREATE', 'READ', 'UPDATE');
```

### Permission Format

```
MODULE:ACTION
```

**Examples:**
- `PRODUCT:CREATE` - Can create products
- `PRODUCT:READ` - Can view products
- `PRODUCT:UPDATE` - Can update products
- `PRODUCT:DELETE` - Can delete products
- `EMPLOYEE:CREATE` - Can create employees
- `ATTENDANCE:READ` - Can view attendance

---

## Examples

### Example 1: Simple CRUD API (No Relations)

**Use Case**: Product management

Files to create:
1. `entity/Product.java`
2. `dto/product/ProductRequest.java`
3. `dto/product/ProductResponse.java`
4. `repository/ProductRepository.java`
5. `service/product/ProductService.java`
6. `service/product/ProductServiceImpl.java`
7. `controller/ProductController.java`

See full implementation above ↑

---

### Example 2: API with Relations (One-to-Many)

**Use Case**: Order with Order Items

```java
@Entity
@Table(name = "orders")
public class Order {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private Long tenantId;
    private Long customerId;
    private LocalDate orderDate;
    private Double totalAmount;
    private String status; // PENDING, CONFIRMED, SHIPPED, DELIVERED

    @OneToMany(mappedBy = "order", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<OrderItem> items = new ArrayList<>();

    // Getters, setters, timestamps...
}

@Entity
@Table(name = "order_items")
public class OrderItem {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "order_id", nullable = false)
    private Order order;

    private Long productId;
    private Integer quantity;
    private Double unitPrice;

    // Getters, setters...
}
```

**Request DTO with nested objects:**

```java
@Data
public class OrderRequest {
    @NotNull
    private Long customerId;

    @NotNull
    private LocalDate orderDate;

    @NotEmpty(message = "Order must have at least one item")
    private List<OrderItemRequest> items;
}

@Data
public class OrderItemRequest {
    @NotNull
    private Long productId;

    @Min(1)
    private Integer quantity;

    @DecimalMin("0.01")
    private Double unitPrice;
}
```

---

### Example 3: API with Query Parameters

**Use Case**: Search/Filter employees

```java
@GetMapping("/search")
@PreAuthorize("hasRole('TENANT_ADMIN') or hasAuthority('EMPLOYEE:READ')")
public ResponseEntity<List<EmployeeResponse>> searchEmployees(
        @RequestParam(required = false) String name,
        @RequestParam(required = false) Long departmentId,
        @RequestParam(required = false) String employmentStatus,
        @RequestParam(required = false) String designation,
        @AuthenticationPrincipal User user) {

    List<EmployeeResponse> employees = employeeService.searchEmployees(
        user.getTenantId(),
        name,
        departmentId,
        employmentStatus,
        designation
    );

    return ResponseEntity.ok(employees);
}
```

**Repository with dynamic query:**

```java
@Query("SELECT e FROM Employee e WHERE e.tenantId = :tenantId " +
       "AND (:name IS NULL OR LOWER(e.firstName) LIKE LOWER(CONCAT('%', :name, '%')) OR LOWER(e.lastName) LIKE LOWER(CONCAT('%', :name, '%'))) " +
       "AND (:departmentId IS NULL OR e.department.id = :departmentId) " +
       "AND (:status IS NULL OR e.employmentStatus = :status)")
List<Employee> searchEmployees(
    @Param("tenantId") Long tenantId,
    @Param("name") String name,
    @Param("departmentId") Long departmentId,
    @Param("status") String status
);
```

---

## Best Practices

### 1. Security Best Practices

#### ✅ DO:
```java
// ALWAYS check tenant ID
@PreAuthorize("hasRole('TENANT_ADMIN') or hasAuthority('PRODUCT:READ')")
public ProductResponse getProduct(Long id, @AuthenticationPrincipal User user) {
    return productService.getProductById(id, user.getTenantId()); // ✅ Pass tenantId
}

// ALWAYS validate ownership in service
Product product = repository.findByIdAndTenantId(id, tenantId)
    .orElseThrow(() -> new ResourceNotFoundException("Product not found"));
```

#### ❌ DON'T:
```java
// ❌ NEVER skip tenant check
Product product = repository.findById(id).orElseThrow(); // WRONG!

// ❌ NEVER trust client-provided tenant ID
public void createProduct(ProductRequest request) {
    product.setTenantId(request.getTenantId()); // DANGEROUS!
}
```

---

### 2. Validation Best Practices

#### ✅ DO:
```java
// Use validation annotations
@NotBlank(message = "Name is required")
@Size(min = 3, max = 100)
private String name;

// Validate business rules in service
if (repository.existsByTenantIdAndName(tenantId, name)) {
    throw new BadRequestException("Name already exists");
}
```

#### ❌ DON'T:
```java
// ❌ Don't skip validation
private String name; // Missing @NotBlank

// ❌ Don't validate only in controller
if (name == null) return; // Service should also validate
```

---

### 3. Transaction Best Practices

#### ✅ DO:
```java
// Use @Transactional for writes
@Transactional
public ProductResponse createProduct(ProductRequest request, Long tenantId) {
    // Multiple database operations in one transaction
    Product product = repository.save(entity);
    auditLogRepository.save(createAuditLog(product));
    return mapToResponse(product);
}

// Use readOnly for reads
@Transactional(readOnly = true)
public List<ProductResponse> getAllProducts(Long tenantId) {
    return repository.findByTenantId(tenantId);
}
```

---

### 4. Error Handling Best Practices

#### ✅ DO:
```java
// Throw specific exceptions
throw new ResourceNotFoundException("Product not found with ID: " + id);
throw new BadRequestException("Invalid price value");
throw new UnauthorizedException("You don't have permission");

// Log errors appropriately
log.error("Failed to create product for tenant {}: {}", tenantId, e.getMessage(), e);
```

#### ❌ DON'T:
```java
// ❌ Don't swallow exceptions
try {
    repository.save(product);
} catch (Exception e) {
    // Silent failure - WRONG!
}

// ❌ Don't expose internal errors
throw new RuntimeException(e.getMessage()); // Exposes stack trace
```

---

### 5. Logging Best Practices

#### ✅ DO:
```java
log.info("Creating product for tenant: {}", tenantId);
log.debug("Product details: {}", product);
log.error("Failed to save product: {}", e.getMessage(), e);
```

#### ❌ DON'T:
```java
System.out.println("Creating product"); // ❌ Use logger
log.info("Product: " + product); // ❌ Use {} placeholders
log.error(e); // ❌ Include message
```

---

## Testing Guide

### 1. Unit Tests (Service Layer)

```java
@ExtendWith(MockitoExtension.class)
class ProductServiceImplTest {

    @Mock
    private ProductRepository productRepository;

    @InjectMocks
    private ProductServiceImpl productService;

    @Test
    void createProduct_Success() {
        // Arrange
        Long tenantId = 1L;
        ProductRequest request = ProductRequest.builder()
                .name("Test Product")
                .price(99.99)
                .stockQuantity(100)
                .build();

        Product savedProduct = Product.builder()
                .id(1L)
                .tenantId(tenantId)
                .name(request.getName())
                .price(request.getPrice())
                .build();

        when(productRepository.existsByTenantIdAndNameIgnoreCase(tenantId, request.getName()))
                .thenReturn(false);
        when(productRepository.save(any(Product.class))).thenReturn(savedProduct);

        // Act
        ProductResponse response = productService.createProduct(request, tenantId);

        // Assert
        assertNotNull(response);
        assertEquals(request.getName(), response.getName());
        verify(productRepository).save(any(Product.class));
    }

    @Test
    void createProduct_DuplicateName_ThrowsException() {
        // Arrange
        Long tenantId = 1L;
        ProductRequest request = ProductRequest.builder()
                .name("Test Product")
                .build();

        when(productRepository.existsByTenantIdAndNameIgnoreCase(tenantId, request.getName()))
                .thenReturn(true);

        // Act & Assert
        assertThrows(BadRequestException.class, () -> {
            productService.createProduct(request, tenantId);
        });
    }
}
```

---

### 2. Integration Tests (Controller Layer)

```java
@SpringBootTest
@AutoConfigureMockMvc
class ProductControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private ProductService productService;

    @Test
    @WithMockUser(username = "admin@test.com", authorities = {"PRODUCT:CREATE"})
    void createProduct_Success() throws Exception {
        // Arrange
        ProductRequest request = ProductRequest.builder()
                .name("Test Product")
                .price(99.99)
                .stockQuantity(100)
                .build();

        ProductResponse response = ProductResponse.builder()
                .id(1L)
                .name(request.getName())
                .build();

        when(productService.createProduct(any(), any())).thenReturn(response);

        // Act & Assert
        mockMvc.perform(post("/api/products")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.name").value("Test Product"));
    }

    @Test
    @WithMockUser(username = "user@test.com", authorities = {"OTHER:PERMISSION"})
    void createProduct_NoPermission_Returns403() throws Exception {
        // Arrange
        ProductRequest request = ProductRequest.builder()
                .name("Test Product")
                .build();

        // Act & Assert
        mockMvc.perform(post("/api/products")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isForbidden());
    }
}
```

---

### 3. Manual Testing with cURL

```bash
# 1. Login to get JWT token
curl -X POST http://localhost:8080/api/auth/login \
  -H "Content-Type: application/json" \
  -d '{
    "email": "admin@company.com",
    "password": "password123"
  }'

# Response: { "token": "eyJhbGc..." }

# 2. Create Product
curl -X POST http://localhost:8080/api/products \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer YOUR_TOKEN_HERE" \
  -d '{
    "name": "Laptop",
    "description": "High-performance laptop",
    "price": 1299.99,
    "stockQuantity": 50,
    "category": "Electronics"
  }'

# 3. Get All Products
curl -X GET http://localhost:8080/api/products \
  -H "Authorization: Bearer YOUR_TOKEN_HERE"

# 4. Get Product by ID
curl -X GET http://localhost:8080/api/products/1 \
  -H "Authorization: Bearer YOUR_TOKEN_HERE"

# 5. Update Product
curl -X PUT http://localhost:8080/api/products/1 \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer YOUR_TOKEN_HERE" \
  -d '{
    "name": "Updated Laptop",
    "price": 1199.99,
    "stockQuantity": 45
  }'

# 6. Delete Product
curl -X DELETE http://localhost:8080/api/products/1 \
  -H "Authorization: Bearer YOUR_TOKEN_HERE"
```

---

## Quick Reference Checklist

When creating a new API, ensure you have:

### Backend Checklist
- [ ] Entity class with `@Entity` and `tenantId`
- [ ] Request DTO with validation annotations
- [ ] Response DTO (separate from Request)
- [ ] Repository interface with tenant-scoped queries
- [ ] Service interface with clear method signatures
- [ ] Service implementation with business logic
- [ ] Controller with `@PreAuthorize` annotations
- [ ] Proper exception handling
- [ ] Logging at key points
- [ ] Transaction management (`@Transactional`)

### Security Checklist
- [ ] Permission created in database
- [ ] Permission assigned to appropriate roles
- [ ] `@PreAuthorize` annotation on controller methods
- [ ] Tenant ID extracted from authenticated user
- [ ] Tenant ID validated in service layer
- [ ] All queries filtered by tenant ID

### Testing Checklist
- [ ] Unit tests for service layer
- [ ] Integration tests for controller
- [ ] Manual testing with Postman/cURL
- [ ] Permission testing (authorized/unauthorized)
- [ ] Edge case testing (invalid input, not found, etc.)

---

## Common Patterns Summary

### Pattern 1: Basic CRUD
```
Entity → DTO (Request/Response) → Repository → Service → Controller
```

### Pattern 2: With Relations
```
Parent Entity ← OneToMany → Child Entity
Parent DTO includes List<Child DTO>
Cascade operations in service layer
```

### Pattern 3: Search/Filter
```
Controller: @RequestParam for filters
Service: Pass filters to repository
Repository: Dynamic @Query with optional parameters
```

### Pattern 4: Pagination (Future)
```
Controller: Pageable parameter
Service: Return Page<DTO>
Repository: extends JpaRepository (supports Pageable)
```

---

## Permissions Reference

### Standard Permission Format
```
MODULE:ACTION

Modules: EMPLOYEE, DEPARTMENT, ATTENDANCE, LEAVE, PAYROLL, SHIFT, PRODUCT, etc.
Actions: CREATE, READ, UPDATE, DELETE, APPROVE, REJECT, etc.
```

### Common Permissions
```
EMPLOYEE:CREATE     - Create employees
EMPLOYEE:READ       - View employees
EMPLOYEE:UPDATE     - Update employees
EMPLOYEE:DELETE     - Delete employees

ATTENDANCE:CREATE   - Mark attendance
ATTENDANCE:READ     - View attendance
ATTENDANCE:UPDATE   - Update attendance
ATTENDANCE:APPROVE  - Approve attendance

LEAVE:CREATE        - Request leave
LEAVE:READ          - View leave requests
LEAVE:UPDATE        - Update leave request
LEAVE:APPROVE       - Approve leave requests
LEAVE:REJECT        - Reject leave requests

PAYROLL:CREATE      - Create payroll
PAYROLL:READ        - View payroll
PAYROLL:PROCESS     - Process payroll
PAYROLL:APPROVE     - Approve payroll
```

---

## Support & Resources

- **API Documentation**: http://localhost:8080/swagger-ui.html
- **Database Schema**: Check `src/main/resources/db/migration/`
- **Exception Classes**: `src/main/java/com/worksyncx/hrms/exception/`
- **Security Config**: `src/main/java/com/worksyncx/hrms/config/SecurityConfig.java`

---

**Last Updated**: November 2025
**Version**: 1.0
**Maintained by**: WorkSyncX Development Team
