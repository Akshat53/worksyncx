# HRMS API Testing Guide

## Quick Start

### Import Postman Collection

1. Open Postman
2. Click **Import** button
3. Select the file: `HRMS_API_Postman_Collection.json`
4. The collection will be imported with all 50+ API endpoints

### Collection Variables

The collection automatically manages these variables:
- `base_url` - API base URL (default: http://localhost:8080)
- `jwt_token` - Automatically set after login/register
- `tenant_id` - Automatically set after login/register
- `department_id` - Set after creating a department
- `designation_id` - Set after creating a designation
- `employee_id` - Set after creating an employee

---

## cURL Commands for Quick Testing

### 1. Authentication APIs

#### Register Company (Creates Tenant + Admin User)
```bash
curl -X POST http://localhost:8080/api/auth/register \
  -H "Content-Type: application/json" \
  -d '{
    "companyName": "TechCorp Solutions",
    "email": "admin@techcorp.com",
    "password": "Admin@123",
    "firstName": "John",
    "lastName": "Doe",
    "phone": "+1234567890",
    "website": "https://techcorp.com",
    "industry": "Technology"
  }'
```

**Response:** Save the `token` from response for subsequent requests

#### Login
```bash
curl -X POST http://localhost:8080/api/auth/login \
  -H "Content-Type: application/json" \
  -d '{
    "email": "admin@techcorp.com",
    "password": "Admin@123"
  }'
```

#### Get Current User
```bash
curl -X GET http://localhost:8080/api/auth/me \
  -H "Authorization: Bearer YOUR_JWT_TOKEN"
```

---

### 2. Department APIs

#### Create Department
```bash
curl -X POST http://localhost:8080/api/departments \
  -H "Authorization: Bearer YOUR_JWT_TOKEN" \
  -H "Content-Type: application/json" \
  -d '{
    "name": "Engineering",
    "code": "ENG",
    "description": "Engineering department",
    "isActive": true
  }'
```

**Response:** Save the `id` for creating designations

#### Get All Departments
```bash
curl -X GET http://localhost:8080/api/departments \
  -H "Authorization: Bearer YOUR_JWT_TOKEN"
```

#### Get Active Departments
```bash
curl -X GET "http://localhost:8080/api/departments?activeOnly=true" \
  -H "Authorization: Bearer YOUR_JWT_TOKEN"
```

#### Get Department by ID
```bash
curl -X GET http://localhost:8080/api/departments/1 \
  -H "Authorization: Bearer YOUR_JWT_TOKEN"
```

#### Update Department
```bash
curl -X PUT http://localhost:8080/api/departments/1 \
  -H "Authorization: Bearer YOUR_JWT_TOKEN" \
  -H "Content-Type: application/json" \
  -d '{
    "name": "Engineering & Technology",
    "code": "ENG",
    "description": "Updated description",
    "isActive": true
  }'
```

#### Delete Department
```bash
curl -X DELETE http://localhost:8080/api/departments/1 \
  -H "Authorization: Bearer YOUR_JWT_TOKEN"
```

---

### 3. Designation APIs

#### Create Designation
```bash
curl -X POST http://localhost:8080/api/designations \
  -H "Authorization: Bearer YOUR_JWT_TOKEN" \
  -H "Content-Type: application/json" \
  -d '{
    "name": "Senior Software Engineer",
    "code": "SSE",
    "description": "Senior level position",
    "departmentId": 1,
    "salaryRangeMin": 80000,
    "salaryRangeMax": 120000,
    "level": "Senior",
    "isActive": true
  }'
```

#### Get All Designations
```bash
curl -X GET http://localhost:8080/api/designations \
  -H "Authorization: Bearer YOUR_JWT_TOKEN"
```

#### Get Designations by Department
```bash
curl -X GET "http://localhost:8080/api/designations?departmentId=1" \
  -H "Authorization: Bearer YOUR_JWT_TOKEN"
```

#### Get Active Designations
```bash
curl -X GET "http://localhost:8080/api/designations?activeOnly=true" \
  -H "Authorization: Bearer YOUR_JWT_TOKEN"
```

---

### 4. Employee APIs

#### Create Employee
```bash
curl -X POST http://localhost:8080/api/employees \
  -H "Authorization: Bearer YOUR_JWT_TOKEN" \
  -H "Content-Type: application/json" \
  -d '{
    "employeeCode": "EMP001",
    "firstName": "Alice",
    "lastName": "Johnson",
    "email": "alice.johnson@techcorp.com",
    "phone": "+1234567891",
    "dateOfBirth": "1990-05-15",
    "gender": "FEMALE",
    "nationality": "American",
    "departmentId": 1,
    "designationId": 1,
    "dateOfJoining": "2024-01-15",
    "employmentType": "PERMANENT",
    "employmentStatus": "ACTIVE",
    "basicSalary": 95000,
    "currency": "USD",
    "address": "123 Main St",
    "city": "San Francisco",
    "state": "California",
    "country": "USA",
    "postalCode": "94105"
  }'
```

#### Get All Employees
```bash
curl -X GET http://localhost:8080/api/employees \
  -H "Authorization: Bearer YOUR_JWT_TOKEN"
```

#### Get Active Employees
```bash
curl -X GET "http://localhost:8080/api/employees?status=ACTIVE" \
  -H "Authorization: Bearer YOUR_JWT_TOKEN"
```

#### Get Employees by Department
```bash
curl -X GET "http://localhost:8080/api/employees?departmentId=1" \
  -H "Authorization: Bearer YOUR_JWT_TOKEN"
```

#### Get Employee by Code
```bash
curl -X GET http://localhost:8080/api/employees/code/EMP001 \
  -H "Authorization: Bearer YOUR_JWT_TOKEN"
```

---

### 5. Attendance APIs

#### Check In
```bash
curl -X POST http://localhost:8080/api/attendance/check-in/1 \
  -H "Authorization: Bearer YOUR_JWT_TOKEN" \
  -H "Content-Type: application/json" \
  -d '{
    "location": "Office - San Francisco",
    "notes": "Regular check-in"
  }'
```

#### Check Out
```bash
curl -X POST http://localhost:8080/api/attendance/check-out/1 \
  -H "Authorization: Bearer YOUR_JWT_TOKEN" \
  -H "Content-Type: application/json" \
  -d '{
    "notes": "Regular check-out"
  }'
```

#### Mark Attendance (Manual)
```bash
curl -X POST http://localhost:8080/api/attendance/mark \
  -H "Authorization: Bearer YOUR_JWT_TOKEN" \
  -H "Content-Type: application/json" \
  -d '{
    "employeeId": 1,
    "attendanceDate": "2024-11-01",
    "checkInTime": "09:00:00",
    "checkOutTime": "18:00:00",
    "workHours": 9.0,
    "status": "PRESENT",
    "location": "Office"
  }'
```

#### Get Today's Attendance
```bash
curl -X GET http://localhost:8080/api/attendance/employee/1/today \
  -H "Authorization: Bearer YOUR_JWT_TOKEN"
```

#### Get Attendance (Date Range)
```bash
curl -X GET "http://localhost:8080/api/attendance/employee/1?startDate=2024-11-01&endDate=2024-11-30" \
  -H "Authorization: Bearer YOUR_JWT_TOKEN"
```

---

### 6. Leave Management APIs

#### Create Leave Type
```bash
curl -X POST http://localhost:8080/api/leave/types \
  -H "Authorization: Bearer YOUR_JWT_TOKEN" \
  -H "Content-Type: application/json" \
  -d '{
    "name": "Casual Leave",
    "code": "CL",
    "daysPerYear": 12,
    "isPaid": true,
    "requiresApproval": true,
    "colorCode": "#4CAF50",
    "isActive": true
  }'
```

#### Get All Leave Types
```bash
curl -X GET http://localhost:8080/api/leave/types \
  -H "Authorization: Bearer YOUR_JWT_TOKEN"
```

#### Create Leave Request
```bash
curl -X POST http://localhost:8080/api/leave/requests \
  -H "Authorization: Bearer YOUR_JWT_TOKEN" \
  -H "Content-Type: application/json" \
  -d '{
    "employeeId": 1,
    "leaveTypeId": 1,
    "startDate": "2024-11-15",
    "endDate": "2024-11-17",
    "totalDays": 3,
    "reason": "Personal work"
  }'
```

#### Get All Leave Requests
```bash
curl -X GET http://localhost:8080/api/leave/requests \
  -H "Authorization: Bearer YOUR_JWT_TOKEN"
```

#### Get Pending Leave Requests
```bash
curl -X GET "http://localhost:8080/api/leave/requests?status=PENDING" \
  -H "Authorization: Bearer YOUR_JWT_TOKEN"
```

#### Approve Leave Request
```bash
curl -X POST http://localhost:8080/api/leave/requests/1/approve \
  -H "Authorization: Bearer YOUR_JWT_TOKEN" \
  -H "Content-Type: application/json" \
  -d '{
    "notes": "Approved"
  }'
```

#### Reject Leave Request
```bash
curl -X POST http://localhost:8080/api/leave/requests/1/reject \
  -H "Authorization: Bearer YOUR_JWT_TOKEN" \
  -H "Content-Type: application/json" \
  -d '{
    "rejectionReason": "Not enough leaves available"
  }'
```

#### Cancel Leave Request
```bash
curl -X POST http://localhost:8080/api/leave/requests/1/cancel \
  -H "Authorization: Bearer YOUR_JWT_TOKEN"
```

---

### 7. Payroll Management APIs

#### Create Payroll Cycle
```bash
curl -X POST http://localhost:8080/api/payroll/cycles \
  -H "Authorization: Bearer YOUR_JWT_TOKEN" \
  -H "Content-Type: application/json" \
  -d '{
    "name": "November 2024 Payroll",
    "month": 11,
    "year": 2024,
    "startDate": "2024-11-01",
    "endDate": "2024-11-30",
    "salaryDate": "2024-12-05",
    "status": "DRAFT"
  }'
```

#### Get All Payroll Cycles
```bash
curl -X GET http://localhost:8080/api/payroll/cycles \
  -H "Authorization: Bearer YOUR_JWT_TOKEN"
```

#### Create Payroll
```bash
curl -X POST http://localhost:8080/api/payroll \
  -H "Authorization: Bearer YOUR_JWT_TOKEN" \
  -H "Content-Type: application/json" \
  -d '{
    "employeeId": 1,
    "payrollCycleId": 1,
    "basicSalary": 95000,
    "hra": 15000,
    "dearnessAllowance": 5000,
    "otherAllowances": 3000,
    "incomeTax": 12000,
    "professionalTax": 200,
    "employeePf": 5700,
    "employeeEsi": 0,
    "otherDeductions": 0,
    "status": "DRAFT"
  }'
```

**Note:** Gross salary, total deductions, and net salary are calculated automatically if not provided.

#### Get All Payrolls
```bash
curl -X GET http://localhost:8080/api/payroll \
  -H "Authorization: Bearer YOUR_JWT_TOKEN"
```

#### Get Payrolls by Cycle
```bash
curl -X GET "http://localhost:8080/api/payroll?cycleId=1" \
  -H "Authorization: Bearer YOUR_JWT_TOKEN"
```

#### Get Payrolls by Employee
```bash
curl -X GET "http://localhost:8080/api/payroll?employeeId=1" \
  -H "Authorization: Bearer YOUR_JWT_TOKEN"
```

#### Mark Payroll as Paid
```bash
curl -X POST http://localhost:8080/api/payroll/1/mark-paid \
  -H "Authorization: Bearer YOUR_JWT_TOKEN" \
  -H "Content-Type: application/json" \
  -d '{
    "bankTransferRef": "TXN123456789"
  }'
```

---

## Testing Workflow

### Step-by-Step Testing Guide

1. **Register/Login**
   ```bash
   # Register a new company
   curl -X POST http://localhost:8080/api/auth/register ...
   # Save the JWT token from response
   ```

2. **Create Department**
   ```bash
   curl -X POST http://localhost:8080/api/departments ...
   # Save the department ID
   ```

3. **Create Designation**
   ```bash
   curl -X POST http://localhost:8080/api/designations ...
   # Save the designation ID
   ```

4. **Create Employee**
   ```bash
   curl -X POST http://localhost:8080/api/employees ...
   # Save the employee ID
   ```

5. **Test Attendance**
   ```bash
   # Check in
   curl -X POST http://localhost:8080/api/attendance/check-in/1 ...

   # Check out
   curl -X POST http://localhost:8080/api/attendance/check-out/1 ...
   ```

6. **Test Leave Management**
   ```bash
   # Create leave type
   curl -X POST http://localhost:8080/api/leave/types ...

   # Create leave request
   curl -X POST http://localhost:8080/api/leave/requests ...

   # Approve/Reject
   curl -X POST http://localhost:8080/api/leave/requests/1/approve ...
   ```

7. **Test Payroll**
   ```bash
   # Create payroll cycle
   curl -X POST http://localhost:8080/api/payroll/cycles ...

   # Create payroll
   curl -X POST http://localhost:8080/api/payroll ...

   # Mark as paid
   curl -X POST http://localhost:8080/api/payroll/1/mark-paid ...
   ```

---

## Common Response Codes

- **200 OK** - Successful GET/PUT request
- **201 Created** - Successful POST request
- **400 Bad Request** - Validation error or business logic error
- **401 Unauthorized** - Missing or invalid JWT token
- **404 Not Found** - Resource not found
- **500 Internal Server Error** - Server error

---

## Tips

1. **Use Postman Collection Variables**: The collection automatically manages JWT token and IDs
2. **Check Response Status**: Always verify the response status code
3. **Multi-Tenancy**: All data is isolated by tenant - each company sees only their own data
4. **JWT Token**: Include `Authorization: Bearer <token>` header in all protected endpoints
5. **Date Format**: Use `YYYY-MM-DD` format for dates (e.g., "2024-11-15")
6. **Time Format**: Use `HH:mm:ss` format for times (e.g., "09:30:00")

---

## Troubleshooting

### "Unauthorized" Error
- Ensure you're sending the JWT token in the Authorization header
- Token expires after 24 hours - login again to get a new token

### "Department/Designation not found"
- Make sure you're using valid IDs from your tenant
- Check that the department/designation was created successfully

### "Employee already checked in"
- Each employee can only check in once per day
- Check today's attendance before attempting check-in

### Schema Validation Errors
- Verify all required fields are provided
- Check data types match the API specification
- Ensure foreign key references (departmentId, designationId) exist
