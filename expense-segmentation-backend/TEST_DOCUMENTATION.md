# Test Documentation

## Overview

Comprehensive unit and integration tests for the expense-segmentation-backend application covering all layers: Controllers, Services, Utilities, and Repositories.

## Test Coverage Summary

### 1. Controller Tests

#### **AuthControllerTest** (`controller/AuthControllerTest.java`)
Tests REST API endpoints with @WebMvcTest

**Test Scenarios:**
- ✅ `register_WithValidRequest_ShouldReturnCreated` - Successful registration
- ✅ `register_WithInvalidEmail_ShouldReturnBadRequest` - Email validation
- ✅ `register_WithShortPassword_ShouldReturnBadRequest` - Password length validation
- ✅ `register_WithBlankName_ShouldReturnBadRequest` - Name validation
- ✅ `login_WithValidCredentials_ShouldReturnOk` - Successful login
- ✅ `login_WithInvalidEmail_ShouldReturnBadRequest` - Email validation on login
- ✅ `getCurrentUser_WithAuthentication_ShouldReturnUser` - Get authenticated user
- ✅ `getCurrentUser_WithoutAuthentication_ShouldReturnUnauthorized` - Unauthorized access

**Total: 8 test cases**

---

### 2. Service Tests

#### **AuthServiceTest** (`service/AuthServiceTest.java`)
Tests business logic with mocked dependencies

**Test Scenarios:**
- ✅ `register_WithValidRequest_ShouldCreateUserAndReturnAuthResponse` - User registration flow
- ✅ `register_WithExistingEmail_ShouldThrowException` - Duplicate email handling
- ✅ `register_WhenEmployeeRoleNotFound_ShouldThrowException` - Missing role handling
- ✅ `login_WithValidCredentials_ShouldReturnAuthResponse` - Successful authentication
- ✅ `login_WithInvalidCredentials_ShouldThrowException` - Invalid credentials
- ✅ `login_WhenUserNotFoundAfterAuthentication_ShouldThrowException` - Edge case
- ✅ `getCurrentUser_WithAuthenticatedUser_ShouldReturnUserResponse` - Get current user
- ✅ `getCurrentUser_WhenUserNotFound_ShouldThrowException` - User not found

**Total: 8 test cases**

#### **CustomUserDetailsServiceTest** (`service/CustomUserDetailsServiceTest.java`)
Tests Spring Security UserDetailsService implementation

**Test Scenarios:**
- ✅ `loadUserByUsername_WithExistingUser_ShouldReturnUserDetails` - Load user
- ✅ `loadUserByUsername_WithNonExistingUser_ShouldThrowException` - User not found
- ✅ `loadUserByUsername_WithManagerRole_ShouldReturnCorrectAuthority` - Manager role
- ✅ `loadUserByUsername_WithAdminRole_ShouldReturnCorrectAuthority` - Admin role
- ✅ `loadUserByUsername_ShouldReturnEnabledUser` - User account status

**Total: 5 test cases**

---

### 3. Configuration/Utility Tests

#### **JwtTokenUtilTest** (`config/JwtTokenUtilTest.java`)
Tests JWT token generation, validation, and extraction

**Test Scenarios:**
- ✅ `generateToken_ShouldReturnValidToken` - Token generation
- ✅ `extractUsername_FromValidToken_ShouldReturnUsername` - Username extraction
- ✅ `extractExpiration_FromValidToken_ShouldReturnFutureDate` - Expiration extraction
- ✅ `validateToken_WithValidToken_ShouldReturnTrue` - Valid token validation
- ✅ `validateToken_WithWrongUsername_ShouldReturnFalse` - Wrong user validation
- ✅ `validateToken_WithExpiredToken_ShouldReturnFalse` - Expired token handling
- ✅ `extractClaim_ShouldExtractSubject` - Claims extraction
- ✅ `extractClaim_ShouldExtractIssuedAt` - Issued date extraction
- ✅ `generateToken_MultipleTokensForSameUser_ShouldHaveDifferentIssuedAt` - Token uniqueness
- ✅ `extractUsername_FromInvalidToken_ShouldThrowException` - Invalid token handling
- ✅ `validateToken_WithMalformedToken_ShouldThrowException` - Malformed token
- ✅ `extractExpiration_ShouldReturnCorrectExpirationTime` - Expiration accuracy

**Total: 12 test cases**

---

### 4. Repository Tests

#### **UserRepositoryTest** (`repository/UserRepositoryTest.java`)
Tests User repository with @DataJpaTest

**Test Scenarios:**
- ✅ `findByEmail_WithExistingEmail_ShouldReturnUser` - Find by email
- ✅ `findByEmail_WithNonExistingEmail_ShouldReturnEmpty` - Email not found
- ✅ `existsByEmail_WithExistingEmail_ShouldReturnTrue` - Email existence check
- ✅ `existsByEmail_WithNonExistingEmail_ShouldReturnFalse` - Email non-existence
- ✅ `findByStatus_ShouldReturnUsersWithGivenStatus` - Filter by status
- ✅ `findByDepartmentId_ShouldReturnUsersInDepartment` - Department filtering
- ✅ `findByRoleId_ShouldReturnUsersWithRole` - Role filtering
- ✅ `findByDepartmentIdAndStatus_ShouldReturnFilteredUsers` - Complex filtering
- ✅ `save_ShouldPersistUser` - Create user
- ✅ `delete_ShouldRemoveUser` - Delete user
- ✅ `findAll_ShouldReturnAllUsers` - Get all users

**Total: 11 test cases**

#### **RoleRepositoryTest** (`repository/RoleRepositoryTest.java`)
Tests Role repository operations

**Test Scenarios:**
- ✅ `findByName_WithExistingRole_ShouldReturnRole` - Find by role name
- ✅ `findByName_WithNonExistingRole_ShouldReturnEmpty` - Role not found
- ✅ `existsByName_WithExistingRole_ShouldReturnTrue` - Role existence
- ✅ `existsByName_WithNonExistingRole_ShouldReturnFalse` - Role non-existence
- ✅ `save_ShouldPersistRole` - Create role
- ✅ `findAll_ShouldReturnAllRoles` - Get all roles
- ✅ `delete_ShouldRemoveRole` - Delete role

**Total: 7 test cases**

#### **DepartmentRepositoryTest** (`repository/DepartmentRepositoryTest.java`)
Tests Department repository operations

**Test Scenarios:**
- ✅ `findByCode_WithExistingCode_ShouldReturnDepartment` - Find by code
- ✅ `findByCode_WithNonExistingCode_ShouldReturnEmpty` - Code not found
- ✅ `findByName_WithExistingName_ShouldReturnDepartment` - Find by name
- ✅ `findByName_WithNonExistingName_ShouldReturnEmpty` - Name not found
- ✅ `existsByCode_WithExistingCode_ShouldReturnTrue` - Code existence
- ✅ `existsByCode_WithNonExistingCode_ShouldReturnFalse` - Code non-existence
- ✅ `existsByName_WithExistingName_ShouldReturnTrue` - Name existence
- ✅ `existsByName_WithNonExistingName_ShouldReturnFalse` - Name non-existence
- ✅ `save_ShouldPersistDepartment` - Create department
- ✅ `findAll_ShouldReturnAllDepartments` - Get all departments
- ✅ `delete_ShouldRemoveDepartment` - Delete department
- ✅ `update_ShouldModifyDepartment` - Update department

**Total: 12 test cases**

---

### 5. Integration Tests

#### **AuthIntegrationTest** (`integration/AuthIntegrationTest.java`)
End-to-end tests with @SpringBootTest

**Test Scenarios:**
- ✅ `fullAuthFlow_RegisterLoginAndAccessProtectedEndpoint_ShouldSucceed` - Complete flow
- ✅ `register_WithDuplicateEmail_ShouldReturnBadRequest` - Duplicate registration
- ✅ `login_WithWrongPassword_ShouldReturnUnauthorized` - Wrong credentials
- ✅ `accessProtectedEndpoint_WithoutToken_ShouldReturnUnauthorized` - No auth
- ✅ `accessProtectedEndpoint_WithInvalidToken_ShouldReturnUnauthorized` - Invalid token
- ✅ `healthCheck_ShouldBeAccessibleWithoutAuth` - Public endpoint
- ✅ `register_WithInvalidEmail_ShouldReturnBadRequest` - Email validation
- ✅ `register_WithShortPassword_ShouldReturnBadRequest` - Password validation

**Total: 8 test cases**

---

## Test Configuration

### **Test Dependencies** (`build.gradle.kts`)
```kotlin
testImplementation("org.springframework.boot:spring-boot-starter-test")
testImplementation("org.springframework.security:spring-security-test")
testRuntimeOnly("com.h2database:h2")
```

### **Test Application Properties** (`test/resources/application-test.yml`)
- H2 in-memory database
- JWT secret for testing
- Hibernate create-drop for clean tests
- Random server port

---

## Running Tests

### Run All Tests
```bash
./gradlew test
```

### Run Specific Test Class
```bash
./gradlew test --tests AuthControllerTest
./gradlew test --tests AuthServiceTest
./gradlew test --tests JwtTokenUtilTest
```

### Run Tests with Coverage
```bash
./gradlew test jacocoTestReport
```

### Run Only Integration Tests
```bash
./gradlew test --tests '*IntegrationTest'
```

### Run Only Unit Tests
```bash
./gradlew test --tests '*Test' --exclude-tests '*IntegrationTest'
```

---

## Test Statistics

| Layer | Test Classes | Test Cases | Coverage Areas |
|-------|-------------|------------|----------------|
| Controllers | 1 | 8 | REST endpoints, validation |
| Services | 2 | 13 | Business logic, authentication |
| Config/Utils | 1 | 12 | JWT operations, token security |
| Repositories | 3 | 30 | Data access, queries, CRUD |
| Integration | 1 | 8 | End-to-end flows |
| **TOTAL** | **8** | **71** | **Full stack** |

---

## Test Patterns Used

1. **@WebMvcTest** - Controller layer testing
2. **@DataJpaTest** - Repository layer testing with in-memory database
3. **@SpringBootTest** - Full application context integration testing
4. **Mockito** - Mocking dependencies in unit tests
5. **AssertJ** - Fluent assertions for better readability
6. **MockMvc** - Testing REST endpoints without starting server
7. **TestEntityManager** - JPA test utilities

---

## Key Features Tested

### ✅ Authentication & Authorization
- User registration with validation
- Login with JWT token generation
- Token-based authentication
- Protected endpoint access

### ✅ Security
- JWT token generation and validation
- Password hashing (BCrypt)
- Token expiration handling
- Invalid token rejection

### ✅ Validation
- Email format validation
- Password strength requirements
- Required field validation
- Duplicate email prevention

### ✅ Data Persistence
- User CRUD operations
- Role management
- Department management
- Relationships (User-Role, User-Department)

### ✅ Error Handling
- Invalid credentials
- Duplicate registration
- Missing resources
- Validation errors

---

## Best Practices Followed

1. **Arrange-Act-Assert (AAA)** pattern in all tests
2. **Descriptive test names** following `methodName_scenario_expectedResult` convention
3. **Isolated tests** with proper setup and teardown
4. **Mock external dependencies** in unit tests
5. **Use real database** (H2) for integration tests
6. **Test both positive and negative scenarios**
7. **Verify exception messages** for better debugging
8. **Clean test data** with @Transactional

---

## Continuous Improvement

### Next Steps:
- Add performance tests for JWT operations
- Add security penetration tests
- Add load testing for authentication endpoints
- Increase code coverage to 90%+
- Add mutation testing with PIT

---

## Troubleshooting Tests

### Common Issues:

**Tests fail with "User not found"**
- Ensure `application-test.yml` is in `src/test/resources`
- Check that H2 dependency is in `build.gradle.kts`

**JWT validation fails**
- Verify JWT secret in test configuration
- Check token expiration time

**Repository tests fail**
- Ensure @DataJpaTest is used
- Check entity relationships are properly configured

**Integration tests fail**
- Verify @SpringBootTest configuration
- Check @ActiveProfiles("test") is set
- Ensure test database is clean between tests
