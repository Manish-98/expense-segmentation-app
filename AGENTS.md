# Agent Guidelines for Expense Segmentation App

## Build/Lint/Test Commands

### Backend (Java/Spring Boot)
- `./gradlew test` - Run all tests
- `./gradlew test --tests "ClassName"` - Run single test class
- `./gradlew test --tests "*methodName"` - Run specific test method
- `./gradlew build` - Full build with tests, coverage, and format checks
- `./gradlew spotlessApply` - Auto-format code
- `./gradlew spotlessCheck` - Check code formatting
- `./gradlew jacocoTestReport` - Generate coverage report
- `make test` - Run tests (from backend directory)
- `make coverage` - Generate coverage report

### Frontend (React/Vite)
- `npm test` - Run tests (if configured)
- `npm run lint` - Run ESLint
- `npm run build` - Build for production
- `npm run dev` - Start development server

## Code Style Guidelines

### Backend (Java)
- Use Google Java Format (AOSP style) with 4-space indentation
- Lombok annotations: `@RequiredArgsConstructor`, `@Slf4j`, `@Data` for DTOs
- Package structure: controller, service, repository, dto, model, config, exception
- Spring Boot annotations: `@RestController`, `@Service`, `@Repository`, `@Valid`
- Security: `@PreAuthorize` for method-level security
- OpenAPI: `@Tag`, `@Operation`, `@Parameter` for API documentation
- Error handling: Custom exceptions extending `RuntimeException`

### Frontend (React)
- Functional components with hooks
- Tailwind CSS for styling (no custom CSS files)
- React Router for navigation
- Axios for API calls with centralized client
- React Hook Form with Yup validation
- ESLint configuration (React recommended rules)
- File structure: components/, pages/, hooks/, context/, api/

### Testing
- Write unit tests for all service layer business logic
- Write integration tests for controllers, repositories, and end-to-end workflows
- Use @SpringBootTest for integration tests with test database
- Mock external dependencies in unit tests using @MockBean
- Test both happy path and error scenarios
- Maintain 95% coverage for business logic (excluding DTOs/models)

### General
- Use meaningful variable/function names
- Keep functions small and focused
- Handle errors gracefully with proper HTTP status codes
- Follow existing patterns and conventions