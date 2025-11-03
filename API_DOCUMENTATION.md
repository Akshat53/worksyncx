# WorkSyncX HRMS API Documentation

Complete API documentation for frontend integration.

## Base URL
```
http://localhost:8080/api
```

## Common Headers

All authenticated requests require:
```
Authorization: Bearer <JWT_TOKEN>
Content-Type: application/json
```

---

## 1. Authentication Module

### 1.1 Register Company
Creates a new tenant (company) with admin user.

**Endpoint:** `POST /auth/register`

**Headers:**
```json
{
  "Content-Type": "application/json"
}
```

**Request Body:**
```json
{
  "companyName": "TechCorp Inc",
  "email": "admin@techcorp.com",
  "password": "SecurePass123",
  "firstName": "John",
  "lastName": "Doe",
  "phone": "+1234567890",
  "website": "https://techcorp.com",
  "industry": "Technology"
}
```

**Response:** `201 Created`
```json
{
  "token": "eyJhbGciOiJIUzUxMiJ9...",
  "type": "Bearer",
  "userId": 1,
  "tenantId": 1,
  "email": "admin@techcorp.com",
  "firstName": "John",
  "lastName": "Doe",
  "roles": ["TENANT_ADMIN"]
}
```

**Error Response:** `400 Bad Request`
```json
{
  "error": "Registration failed",
  "message": "Email is already registered!"
}
```

---

### 1.2 Login
Authenticate user and get JWT token.

**Endpoint:** `POST /auth/login`

**Headers:**
```json
{
  "Content-Type": "application/json"
}
```

**Request Body:**
```json
{
  "email": "admin@techcorp.com",
  "password": "SecurePass123"
}
```

**Response:** `200 OK`
```json
{
  "token": "eyJhbGciOiJIUzUxMiJ9...",
  "type": "Bearer",
  "userId": 1,
  "tenantId": 1,
  "email": "admin@techcorp.com",
  "firstName": "John",
  "lastName": "Doe",
  "roles": ["TENANT_ADMIN"]
}
```

**Error Response:** `401 Unauthorized`
```json
{
  "error": "Invalid email or password",
  "message": "Bad credentials"
}
```

---

### 1.3 Get Current User
Get authenticated user details.

**Endpoint:** `GET /auth/me`

**Headers:**
```json
{
  "Authorization": "Bearer eyJhbGciOiJIUzUxMiJ9...",
  "Content-Type": "application/json"
}
```

**Response:** `200 OK`
```json
{
  "id": 1,
  "tenantId": 1,
  "email": "admin@techcorp.com",
  "firstName": "John",
  "lastName": "Doe",
  "isActive": true,
  "roles": ["TENANT_ADMIN"]
}
```

---

### 1.4 Logout
Logout current user.

**Endpoint:** `POST /auth/logout`

**Headers:**
```json
{
  "Authorization": "Bearer eyJhbGciOiJIUzUxMiJ9..."
}
```

**Response:** `200 OK`
```json
{
  "message": "Logout successful"
}
```

---

## 2. Department Management

### 2.1 Create Department

**Endpoint:** `POST /departments`

**Headers:**
```json
{
  "Authorization": "Bearer <token>",
  "Content-Type": "application/json"
}
```

**Request Body:**
```json
{
  "name": "Engineering",
  "code": "ENG",
  "description": "Software Engineering Department",
  "headId": null,
  "parentDepartmentId": null
}
```

**Response:** `201 Created`
```json
{
  "id": 1,
  "tenantId": 1,
  "name": "Engineering",
  "code": "ENG",
  "description": "Software Engineering Department",
  "headId": null,
  "parentDepartmentId": null,
  "isActive": true,
  "createdAt": "2025-11-03T10:30:00",
  "updatedAt": "2025-11-03T10:30:00"
}
```

---

### 2.2 Get All Departments

**Endpoint:** `GET /departments`

**Headers:**
```json
{
  "Authorization": "Bearer <token>"
}
```

**Response:** `200 OK`
```json
[
  {
    "id": 1,
    "tenantId": 1,
    "name": "Engineering",
    "code": "ENG",
    "description": "Software Engineering Department",
    "headId": null,
    "parentDepartmentId": null,
    "isActive": true,
    "createdAt": "2025-11-03T10:30:00",
    "updatedAt": "2025-11-03T10:30:00"
  }
]
```

---

### 2.3 Get Department by ID

**Endpoint:** `GET /departments/{id}`

**Headers:**
```json
{
  "Authorization": "Bearer <token>"
}
```

**Response:** `200 OK`
```json
{
  "id": 1,
  "tenantId": 1,
  "name": "Engineering",
  "code": "ENG",
  "description": "Software Engineering Department",
  "headId": null,
  "parentDepartmentId": null,
  "isActive": true,
  "createdAt": "2025-11-03T10:30:00",
  "updatedAt": "2025-11-03T10:30:00"
}
```

---

### 2.4 Update Department

**Endpoint:** `PUT /departments/{id}`

**Headers:**
```json
{
  "Authorization": "Bearer <token>",
  "Content-Type": "application/json"
}
```

**Request Body:**
```json
{
  "name": "Software Engineering",
  "code": "SE",
  "description": "Updated description",
  "headId": 5,
  "parentDepartmentId": null
}
```

**Response:** `200 OK`
```json
{
  "id": 1,
  "tenantId": 1,
  "name": "Software Engineering",
  "code": "SE",
  "description": "Updated description",
  "headId": 5,
  "parentDepartmentId": null,
  "isActive": true,
  "createdAt": "2025-11-03T10:30:00",
  "updatedAt": "2025-11-03T11:45:00"
}
```

---

### 2.5 Delete Department

**Endpoint:** `DELETE /departments/{id}`

**Headers:**
```json
{
  "Authorization": "Bearer <token>"
}
```

**Response:** `200 OK`
```json
{
  "message": "Department deactivated successfully"
}
```

---

## 3. Designation Management

### 3.1 Create Designation

**Endpoint:** `POST /designations`

**Headers:**
```json
{
  "Authorization": "Bearer <token>",
  "Content-Type": "application/json"
}
```

**Request Body:**
```json
{
  "name": "Senior Software Engineer",
  "code": "SSE",
  "description": "Senior level software engineer",
  "departmentId": 1,
  "salaryRangeMin": 80000.00,
  "salaryRangeMax": 120000.00,
  "level": "SENIOR"
}
```

**Response:** `201 Created`
```json
{
  "id": 1,
  "tenantId": 1,
  "name": "Senior Software Engineer",
  "code": "SSE",
  "description": "Senior level software engineer",
  "departmentId": 1,
  "salaryRangeMin": 80000.00,
  "salaryRangeMax": 120000.00,
  "level": "SENIOR",
  "isActive": true,
  "createdAt": "2025-11-03T10:30:00"
}
```

---

### 3.2 Get All Designations

**Endpoint:** `GET /designations`

**Query Parameters:**
- `departmentId` (optional): Filter by department

**Headers:**
```json
{
  "Authorization": "Bearer <token>"
}
```

**Response:** `200 OK`
```json
[
  {
    "id": 1,
    "tenantId": 1,
    "name": "Senior Software Engineer",
    "code": "SSE",
    "description": "Senior level software engineer",
    "departmentId": 1,
    "salaryRangeMin": 80000.00,
    "salaryRangeMax": 120000.00,
    "level": "SENIOR",
    "isActive": true,
    "createdAt": "2025-11-03T10:30:00"
  }
]
```

---

## 4. Employee Management

### 4.1 Create Employee

**Endpoint:** `POST /employees`

**Headers:**
```json
{
  "Authorization": "Bearer <token>",
  "Content-Type": "application/json"
}
```

**Request Body:**
```json
{
  "employeeCode": "EMP001",
  "firstName": "Jane",
  "lastName": "Smith",
  "email": "jane.smith@techcorp.com",
  "phone": "+1234567891",
  "dateOfBirth": "1990-05-15",
  "gender": "FEMALE",
  "nationality": "American",
  "departmentId": 1,
  "designationId": 1,
  "managerId": null,
  "dateOfJoining": "2025-01-01",
  "employmentType": "PERMANENT",
  "employmentStatus": "ACTIVE",
  "basicSalary": 100000.00,
  "currency": "USD",
  "address": "123 Main St",
  "city": "San Francisco",
  "state": "CA",
  "country": "USA",
  "postalCode": "94102",
  "emergencyContactName": "John Smith",
  "emergencyContactPhone": "+1234567892",
  "emergencyContactRelation": "Spouse",
  "bankName": "Chase Bank",
  "bankAccount": "1234567890",
  "ifscCode": "CHASE001",
  "pan": "ABCDE1234F"
}
```

**Response:** `201 Created`
```json
{
  "id": 1,
  "tenantId": 1,
  "employeeCode": "EMP001",
  "firstName": "Jane",
  "lastName": "Smith",
  "email": "jane.smith@techcorp.com",
  "phone": "+1234567891",
  "dateOfBirth": "1990-05-15",
  "gender": "FEMALE",
  "nationality": "American",
  "departmentId": 1,
  "designationId": 1,
  "managerId": null,
  "dateOfJoining": "2025-01-01",
  "dateOfLeaving": null,
  "employmentType": "PERMANENT",
  "employmentStatus": "ACTIVE",
  "basicSalary": 100000.00,
  "currency": "USD",
  "createdAt": "2025-11-03T10:30:00"
}
```

---

### 4.2 Get All Employees

**Endpoint:** `GET /employees`

**Query Parameters:**
- `departmentId` (optional): Filter by department
- `employmentStatus` (optional): Filter by status (ACTIVE, ON_LEAVE, SUSPENDED, SEPARATED)
- `page` (optional): Page number (default: 0)
- `size` (optional): Page size (default: 20)

**Headers:**
```json
{
  "Authorization": "Bearer <token>"
}
```

**Response:** `200 OK`
```json
{
  "content": [
    {
      "id": 1,
      "tenantId": 1,
      "employeeCode": "EMP001",
      "firstName": "Jane",
      "lastName": "Smith",
      "email": "jane.smith@techcorp.com",
      "phone": "+1234567891",
      "departmentId": 1,
      "designationId": 1,
      "employmentStatus": "ACTIVE",
      "dateOfJoining": "2025-01-01"
    }
  ],
  "totalElements": 1,
  "totalPages": 1,
  "size": 20,
  "number": 0
}
```

---

### 4.3 Get Employee by ID

**Endpoint:** `GET /employees/{id}`

**Headers:**
```json
{
  "Authorization": "Bearer <token>"
}
```

**Response:** `200 OK`
```json
{
  "id": 1,
  "tenantId": 1,
  "employeeCode": "EMP001",
  "firstName": "Jane",
  "lastName": "Smith",
  "email": "jane.smith@techcorp.com",
  "phone": "+1234567891",
  "dateOfBirth": "1990-05-15",
  "gender": "FEMALE",
  "nationality": "American",
  "departmentId": 1,
  "designationId": 1,
  "managerId": null,
  "dateOfJoining": "2025-01-01",
  "dateOfLeaving": null,
  "employmentType": "PERMANENT",
  "employmentStatus": "ACTIVE",
  "basicSalary": 100000.00,
  "currency": "USD",
  "address": "123 Main St",
  "city": "San Francisco",
  "state": "CA",
  "country": "USA",
  "postalCode": "94102",
  "emergencyContactName": "John Smith",
  "emergencyContactPhone": "+1234567892",
  "emergencyContactRelation": "Spouse",
  "bankName": "Chase Bank",
  "bankAccount": "1234567890",
  "ifscCode": "CHASE001",
  "pan": "ABCDE1234F",
  "createdAt": "2025-11-03T10:30:00",
  "updatedAt": "2025-11-03T10:30:00"
}
```

---

### 4.4 Update Employee

**Endpoint:** `PUT /employees/{id}`

**Headers:**
```json
{
  "Authorization": "Bearer <token>",
  "Content-Type": "application/json"
}
```

**Request Body:** (Same as Create Employee)

**Response:** `200 OK` (Same as Get Employee)

---

### 4.5 Delete Employee (Soft Delete)

**Endpoint:** `DELETE /employees/{id}`

**Headers:**
```json
{
  "Authorization": "Bearer <token>"
}
```

**Response:** `200 OK`
```json
{
  "message": "Employee marked as inactive successfully"
}
```

---

## 5. Attendance Management

### 5.1 Check-In

**Endpoint:** `POST /attendance/check-in`

**Headers:**
```json
{
  "Authorization": "Bearer <token>",
  "Content-Type": "application/json"
}
```

**Request Body:**
```json
{
  "employeeId": 1,
  "checkInTime": "09:00:00",
  "location": "Office, Floor 3"
}
```

**Response:** `201 Created`
```json
{
  "id": 1,
  "tenantId": 1,
  "employeeId": 1,
  "attendanceDate": "2025-11-03",
  "checkInTime": "09:00:00",
  "checkOutTime": null,
  "workHours": null,
  "status": "PRESENT",
  "location": "Office, Floor 3",
  "createdAt": "2025-11-03T09:00:00"
}
```

---

### 5.2 Check-Out

**Endpoint:** `POST /attendance/check-out`

**Headers:**
```json
{
  "Authorization": "Bearer <token>",
  "Content-Type": "application/json"
}
```

**Request Body:**
```json
{
  "employeeId": 1,
  "checkOutTime": "18:00:00"
}
```

**Response:** `200 OK`
```json
{
  "id": 1,
  "tenantId": 1,
  "employeeId": 1,
  "attendanceDate": "2025-11-03",
  "checkInTime": "09:00:00",
  "checkOutTime": "18:00:00",
  "workHours": 9.00,
  "status": "PRESENT",
  "location": "Office, Floor 3",
  "updatedAt": "2025-11-03T18:00:00"
}
```

---

### 5.3 Get Attendance Records

**Endpoint:** `GET /attendance`

**Query Parameters:**
- `employeeId` (optional): Filter by employee
- `startDate` (optional): Start date (YYYY-MM-DD)
- `endDate` (optional): End date (YYYY-MM-DD)

**Headers:**
```json
{
  "Authorization": "Bearer <token>"
}
```

**Response:** `200 OK`
```json
[
  {
    "id": 1,
    "tenantId": 1,
    "employeeId": 1,
    "attendanceDate": "2025-11-03",
    "checkInTime": "09:00:00",
    "checkOutTime": "18:00:00",
    "workHours": 9.00,
    "status": "PRESENT",
    "location": "Office, Floor 3"
  }
]
```

---

### 5.4 Manual Attendance Entry (Admin)

**Endpoint:** `POST /attendance/manual`

**Headers:**
```json
{
  "Authorization": "Bearer <token>",
  "Content-Type": "application/json"
}
```

**Request Body:**
```json
{
  "employeeId": 1,
  "attendanceDate": "2025-11-02",
  "checkInTime": "09:00:00",
  "checkOutTime": "18:00:00",
  "status": "PRESENT",
  "notes": "Manual entry for sick day recovery"
}
```

**Response:** `201 Created`

---

## 6. Leave Management

### 6.1 Create Leave Type (Admin)

**Endpoint:** `POST /leave-types`

**Headers:**
```json
{
  "Authorization": "Bearer <token>",
  "Content-Type": "application/json"
}
```

**Request Body:**
```json
{
  "name": "Sick Leave",
  "code": "SL",
  "daysPerYear": 12,
  "isPaid": true,
  "requiresApproval": true,
  "colorCode": "#FF5733"
}
```

**Response:** `201 Created`
```json
{
  "id": 1,
  "tenantId": 1,
  "name": "Sick Leave",
  "code": "SL",
  "daysPerYear": 12,
  "isPaid": true,
  "requiresApproval": true,
  "colorCode": "#FF5733",
  "isActive": true,
  "createdAt": "2025-11-03T10:30:00"
}
```

---

### 6.2 Get All Leave Types

**Endpoint:** `GET /leave-types`

**Headers:**
```json
{
  "Authorization": "Bearer <token>"
}
```

**Response:** `200 OK`
```json
[
  {
    "id": 1,
    "tenantId": 1,
    "name": "Sick Leave",
    "code": "SL",
    "daysPerYear": 12,
    "isPaid": true,
    "requiresApproval": true,
    "colorCode": "#FF5733",
    "isActive": true
  },
  {
    "id": 2,
    "tenantId": 1,
    "name": "Casual Leave",
    "code": "CL",
    "daysPerYear": 10,
    "isPaid": true,
    "requiresApproval": true,
    "colorCode": "#33FF57",
    "isActive": true
  }
]
```

---

### 6.3 Request Leave

**Endpoint:** `POST /leave-requests`

**Headers:**
```json
{
  "Authorization": "Bearer <token>",
  "Content-Type": "application/json"
}
```

**Request Body:**
```json
{
  "employeeId": 1,
  "leaveTypeId": 1,
  "startDate": "2025-11-10",
  "endDate": "2025-11-12",
  "totalDays": 3,
  "reason": "Medical appointment and recovery"
}
```

**Response:** `201 Created`
```json
{
  "id": 1,
  "tenantId": 1,
  "employeeId": 1,
  "leaveTypeId": 1,
  "startDate": "2025-11-10",
  "endDate": "2025-11-12",
  "totalDays": 3,
  "reason": "Medical appointment and recovery",
  "status": "PENDING",
  "approvedBy": null,
  "approvedAt": null,
  "createdAt": "2025-11-03T10:30:00"
}
```

---

### 6.4 Get Leave Requests

**Endpoint:** `GET /leave-requests`

**Query Parameters:**
- `employeeId` (optional): Filter by employee
- `status` (optional): Filter by status (PENDING, APPROVED, REJECTED, CANCELLED)

**Headers:**
```json
{
  "Authorization": "Bearer <token>"
}
```

**Response:** `200 OK`
```json
[
  {
    "id": 1,
    "tenantId": 1,
    "employeeId": 1,
    "leaveTypeId": 1,
    "leaveTypeName": "Sick Leave",
    "startDate": "2025-11-10",
    "endDate": "2025-11-12",
    "totalDays": 3,
    "reason": "Medical appointment and recovery",
    "status": "PENDING",
    "createdAt": "2025-11-03T10:30:00"
  }
]
```

---

### 6.5 Approve Leave

**Endpoint:** `POST /leave-requests/{id}/approve`

**Headers:**
```json
{
  "Authorization": "Bearer <token>"
}
```

**Response:** `200 OK`
```json
{
  "id": 1,
  "employeeId": 1,
  "status": "APPROVED",
  "approvedBy": 1,
  "approvedAt": "2025-11-03T11:00:00",
  "message": "Leave approved successfully"
}
```

---

### 6.6 Reject Leave

**Endpoint:** `POST /leave-requests/{id}/reject`

**Headers:**
```json
{
  "Authorization": "Bearer <token>",
  "Content-Type": "application/json"
}
```

**Request Body:**
```json
{
  "rejectionReason": "Insufficient leave balance"
}
```

**Response:** `200 OK`
```json
{
  "id": 1,
  "employeeId": 1,
  "status": "REJECTED",
  "approvedBy": 1,
  "rejectionReason": "Insufficient leave balance",
  "approvedAt": "2025-11-03T11:00:00"
}
```

---

## 7. Payroll Management

### 7.1 Create Payroll Cycle

**Endpoint:** `POST /payroll-cycles`

**Headers:**
```json
{
  "Authorization": "Bearer <token>",
  "Content-Type": "application/json"
}
```

**Request Body:**
```json
{
  "name": "November 2025",
  "month": 11,
  "year": 2025,
  "startDate": "2025-11-01",
  "endDate": "2025-11-30",
  "salaryDate": "2025-12-01"
}
```

**Response:** `201 Created`
```json
{
  "id": 1,
  "tenantId": 1,
  "name": "November 2025",
  "month": 11,
  "year": 2025,
  "startDate": "2025-11-01",
  "endDate": "2025-11-30",
  "salaryDate": "2025-12-01",
  "status": "DRAFT",
  "createdAt": "2025-11-03T10:30:00"
}
```

---

### 7.2 Get All Payroll Cycles

**Endpoint:** `GET /payroll-cycles`

**Headers:**
```json
{
  "Authorization": "Bearer <token>"
}
```

**Response:** `200 OK`
```json
[
  {
    "id": 1,
    "tenantId": 1,
    "name": "November 2025",
    "month": 11,
    "year": 2025,
    "startDate": "2025-11-01",
    "endDate": "2025-11-30",
    "salaryDate": "2025-12-01",
    "status": "DRAFT"
  }
]
```

---

### 7.3 Generate Payroll

**Endpoint:** `POST /payrolls`

**Headers:**
```json
{
  "Authorization": "Bearer <token>",
  "Content-Type": "application/json"
}
```

**Request Body:**
```json
{
  "employeeId": 1,
  "payrollCycleId": 1,
  "basicSalary": 100000.00,
  "hra": 20000.00,
  "dearnessAllowance": 5000.00,
  "otherAllowances": 3000.00,
  "incomeTax": 15000.00,
  "professionalTax": 2000.00,
  "employeePf": 12000.00,
  "employeeEsi": 1000.00,
  "otherDeductions": 500.00
}
```

**Response:** `201 Created`
```json
{
  "id": 1,
  "tenantId": 1,
  "employeeId": 1,
  "payrollCycleId": 1,
  "basicSalary": 100000.00,
  "hra": 20000.00,
  "dearnessAllowance": 5000.00,
  "otherAllowances": 3000.00,
  "grossSalary": 128000.00,
  "incomeTax": 15000.00,
  "professionalTax": 2000.00,
  "employeePf": 12000.00,
  "employeeEsi": 1000.00,
  "otherDeductions": 500.00,
  "totalDeductions": 30500.00,
  "netSalary": 97500.00,
  "status": "DRAFT",
  "createdAt": "2025-11-03T10:30:00"
}
```

---

### 7.4 Get Payrolls

**Endpoint:** `GET /payrolls`

**Query Parameters:**
- `payrollCycleId` (optional): Filter by cycle
- `employeeId` (optional): Filter by employee

**Headers:**
```json
{
  "Authorization": "Bearer <token>"
}
```

**Response:** `200 OK`
```json
[
  {
    "id": 1,
    "tenantId": 1,
    "employeeId": 1,
    "employeeName": "Jane Smith",
    "payrollCycleId": 1,
    "cycleName": "November 2025",
    "grossSalary": 128000.00,
    "totalDeductions": 30500.00,
    "netSalary": 97500.00,
    "status": "DRAFT"
  }
]
```

---

### 7.5 Approve Payroll

**Endpoint:** `POST /payrolls/{id}/approve`

**Headers:**
```json
{
  "Authorization": "Bearer <token>"
}
```

**Response:** `200 OK`
```json
{
  "id": 1,
  "status": "APPROVED",
  "message": "Payroll approved successfully"
}
```

---

### 7.6 Process Payroll (Mark as Paid)

**Endpoint:** `POST /payrolls/{id}/process`

**Headers:**
```json
{
  "Authorization": "Bearer <token>",
  "Content-Type": "application/json"
}
```

**Request Body:**
```json
{
  "bankTransferRef": "TXN123456789"
}
```

**Response:** `200 OK`
```json
{
  "id": 1,
  "status": "PAID",
  "bankTransferRef": "TXN123456789",
  "paidDate": "2025-11-03T15:30:00",
  "message": "Payroll processed successfully"
}
```

---

## Error Responses

All endpoints may return these error responses:

### 400 Bad Request
```json
{
  "status": 400,
  "error": "Bad Request",
  "message": "Validation failed: Email is required",
  "path": "/api/auth/register"
}
```

### 401 Unauthorized
```json
{
  "status": 401,
  "error": "Unauthorized",
  "message": "JWT token is expired or invalid",
  "path": "/api/employees"
}
```

### 403 Forbidden
```json
{
  "status": 403,
  "error": "Forbidden",
  "message": "Access denied. Insufficient permissions",
  "path": "/api/payrolls"
}
```

### 404 Not Found
```json
{
  "status": 404,
  "error": "Not Found",
  "message": "Employee not found with id: 123",
  "path": "/api/employees/123"
}
```

### 500 Internal Server Error
```json
{
  "status": 500,
  "error": "Internal Server Error",
  "message": "An unexpected error occurred",
  "path": "/api/employees"
}
```

---

## Authentication Flow

### Step 1: Register/Login
```javascript
// Register new company
const registerResponse = await fetch('http://localhost:8080/api/auth/register', {
  method: 'POST',
  headers: { 'Content-Type': 'application/json' },
  body: JSON.stringify({
    companyName: "TechCorp Inc",
    email: "admin@techcorp.com",
    password: "SecurePass123",
    firstName: "John",
    lastName: "Doe"
  })
});

const { token } = await registerResponse.json();
localStorage.setItem('token', token);
```

### Step 2: Use Token for Authenticated Requests
```javascript
// Example: Get employees
const token = localStorage.getItem('token');
const employeesResponse = await fetch('http://localhost:8080/api/employees', {
  headers: {
    'Authorization': `Bearer ${token}`,
    'Content-Type': 'application/json'
  }
});

const employees = await employeesResponse.json();
```

### Step 3: Handle Token Expiry
```javascript
// If 401 response, redirect to login
if (response.status === 401) {
  localStorage.removeItem('token');
  window.location.href = '/login';
}
```

---

## Frontend Integration Notes

### 1. Token Management
- Store JWT token in `localStorage` or secure cookie
- Include token in all authenticated requests
- Token expires after 24 hours (configurable)
- Implement token refresh mechanism if needed

### 2. Error Handling
- Always handle 401 (redirect to login)
- Handle 403 (show permission denied message)
- Handle 400 (show validation errors to user)
- Handle 500 (show generic error message)

### 3. Multi-Tenancy
- Each user belongs to one tenant
- All data is automatically filtered by tenant
- No need to send tenantId in requests (extracted from token)

### 4. Role-Based Access
- Check user roles from `/auth/me` endpoint
- Hide/show UI elements based on roles:
  - `TENANT_ADMIN`: Full access
  - `HR_MANAGER`: Employee, Attendance, Leave, Payroll
  - `MANAGER`: Own team management
  - `EMPLOYEE`: Own data only

### 5. Date/Time Formats
- Dates: `YYYY-MM-DD` (e.g., "2025-11-03")
- Times: `HH:mm:ss` (e.g., "09:00:00")
- DateTimes: ISO 8601 format (e.g., "2025-11-03T10:30:00")

### 6. Pagination
Most list endpoints support pagination:
- `page`: Page number (0-indexed)
- `size`: Items per page
- Default: `page=0`, `size=20`

Example:
```
GET /api/employees?page=0&size=10
```

---

## Sample Frontend Code

### React Example

```javascript
// API Service
import axios from 'axios';

const API_BASE_URL = 'http://localhost:8080/api';

const api = axios.create({
  baseURL: API_BASE_URL,
  headers: {
    'Content-Type': 'application/json'
  }
});

// Add token to all requests
api.interceptors.request.use(
  config => {
    const token = localStorage.getItem('token');
    if (token) {
      config.headers.Authorization = `Bearer ${token}`;
    }
    return config;
  },
  error => Promise.reject(error)
);

// Handle 401 errors
api.interceptors.response.use(
  response => response,
  error => {
    if (error.response?.status === 401) {
      localStorage.removeItem('token');
      window.location.href = '/login';
    }
    return Promise.reject(error);
  }
);

// Auth Service
export const authService = {
  register: (data) => api.post('/auth/register', data),
  login: (data) => api.post('/auth/login', data),
  getCurrentUser: () => api.get('/auth/me'),
  logout: () => api.post('/auth/logout')
};

// Employee Service
export const employeeService = {
  getAll: (params) => api.get('/employees', { params }),
  getById: (id) => api.get(`/employees/${id}`),
  create: (data) => api.post('/employees', data),
  update: (id, data) => api.put(`/employees/${id}`, data),
  delete: (id) => api.delete(`/employees/${id}`)
};

// Usage in Component
const EmployeeList = () => {
  const [employees, setEmployees] = useState([]);

  useEffect(() => {
    employeeService.getAll({ page: 0, size: 20 })
      .then(response => setEmployees(response.data.content))
      .catch(error => console.error(error));
  }, []);

  return (
    <div>
      {employees.map(emp => (
        <div key={emp.id}>{emp.firstName} {emp.lastName}</div>
      ))}
    </div>
  );
};
```

---

## Testing with cURL

```bash
# Register
curl -X POST http://localhost:8080/api/auth/register \
  -H "Content-Type: application/json" \
  -d '{
    "companyName": "TechCorp Inc",
    "email": "admin@techcorp.com",
    "password": "SecurePass123",
    "firstName": "John",
    "lastName": "Doe"
  }'

# Login
curl -X POST http://localhost:8080/api/auth/login \
  -H "Content-Type: application/json" \
  -d '{
    "email": "admin@techcorp.com",
    "password": "SecurePass123"
  }'

# Get employees (replace TOKEN)
curl -X GET http://localhost:8080/api/employees \
  -H "Authorization: Bearer TOKEN"
```

---

## Support & Contact

For API issues or questions:
- Email: support@worksyncx.com
- Documentation: https://docs.worksyncx.com
- GitHub: https://github.com/worksyncx/hrms-service

---

**Last Updated:** November 3, 2025
**API Version:** 1.0.0
**Maintained by:** WorkSyncX Development Team
