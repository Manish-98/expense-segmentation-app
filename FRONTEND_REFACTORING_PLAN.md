# Frontend Refactoring Plan

## Executive Summary
This document outlines a comprehensive refactoring plan for the React frontend, focusing on code reusability, maintainability, and following React + Tailwind best practices.

## Analysis Summary

### Current Structure
```
src/
├── api/              # API service layer ✅
├── components/       # Reusable components (4 components)
├── context/          # Auth context ✅
├── pages/            # Page components (8 pages)
├── router/           # Routing configuration ✅
├── App.jsx
└── main.jsx
```

### Identified Issues

1. **Duplicated Code Patterns** (High Priority)
   - Navigation bar duplicated across 6 pages (100% identical)
   - Loading spinners duplicated 4 times
   - Page layout wrapper duplicated 6 times
   - Badge styling logic duplicated in 3 files
   - Status/type badge colors duplicated

2. **Missing Components** (High Priority)
   - No shared Navbar component
   - No shared PageLayout/Container component
   - No LoadingSpinner component
   - No Badge component
   - No ErrorMessage/Alert component
   - No EmptyState component
   - No Pagination component

3. **Inconsistent Patterns** (Medium Priority)
   - Inline button styling vs Button component usage
   - Mixed error handling approaches
   - No consistent spacing/typography system
   - Button variants don't cover all use cases

4. **Accessibility Gaps** (Medium Priority)
   - Missing aria labels on icon buttons
   - No keyboard navigation hints
   - Loading states not announced to screen readers

5. **Folder Organization** (Low Priority)
   - Missing hooks/ directory for custom hooks
   - Missing utils/ directory for utility functions
   - Missing constants/ for shared constants

## Refactoring Roadmap

### Phase 1: Extract Core UI Components (Week 1)

#### 1.1 Create Layout Components

**File: `src/components/Layout/PageLayout.jsx`**
```jsx
const PageLayout = ({ children }) => {
  return (
    <div className="min-h-screen bg-gray-100">
      {children}
    </div>
  );
};
```

**File: `src/components/Layout/PageContainer.jsx`**
```jsx
const PageContainer = ({ children, className = '' }) => {
  return (
    <main className={`max-w-7xl mx-auto py-6 sm:px-6 lg:px-8 ${className}`}>
      <div className="px-4 py-6 sm:px-0">
        {children}
      </div>
    </main>
  );
};
```

**File: `src/components/Layout/Card.jsx`**
```jsx
const Card = ({ children, className = '', title, actions }) => {
  return (
    <div className={`bg-white rounded-lg shadow ${className}`}>
      {title && (
        <div className="px-6 py-4 border-b border-gray-200 flex justify-between items-center">
          <h2 className="text-2xl font-bold text-gray-900">{title}</h2>
          {actions && <div className="flex gap-2">{actions}</div>}
        </div>
      )}
      <div className="p-6">
        {children}
      </div>
    </div>
  );
};
```

#### 1.2 Create Navbar Component

**File: `src/components/Navigation/Navbar.jsx`**
```jsx
import { useAuth } from '../../context/AuthContext';
import { useNavigate } from 'react-router-dom';
import NavLink from './NavLink';

const Navbar = () => {
  const { user, logout } = useAuth();
  const navigate = useNavigate();

  const handleLogout = () => {
    logout();
    navigate('/login');
  };

  return (
    <nav className="bg-white shadow-sm">
      <div className="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8">
        <div className="flex justify-between h-16 items-center">
          <div className="flex-shrink-0">
            <h1 className="text-xl font-bold text-gray-900">
              Expense Segmentation App
            </h1>
          </div>
          <div className="flex items-center space-x-4">
            <NavLink to="/dashboard">Dashboard</NavLink>
            <NavLink to="/expenses">Expenses</NavLink>

            {(user?.role === 'ADMIN' || user?.role === 'MANAGER') && (
              <NavLink to="/users">Users</NavLink>
            )}

            {(user?.role === 'ADMIN' || user?.role === 'FINANCE') && (
              <NavLink to="/departments">Departments</NavLink>
            )}

            <span className="text-gray-700">
              Welcome, {user?.name || 'User'}
            </span>

            <button
              onClick={handleLogout}
              className="px-4 py-2 bg-red-600 text-white rounded-lg hover:bg-red-700 transition-colors"
            >
              Logout
            </button>
          </div>
        </div>
      </div>
    </nav>
  );
};

export default Navbar;
```

**File: `src/components/Navigation/NavLink.jsx`**
```jsx
import { Link, useLocation } from 'react-router-dom';

const NavLink = ({ to, children, className = '' }) => {
  const location = useLocation();
  const isActive = location.pathname === to;

  const activeClass = isActive
    ? 'text-blue-600 hover:text-blue-800'
    : 'text-gray-700 hover:text-gray-900';

  return (
    <Link
      to={to}
      className={`font-medium transition-colors ${activeClass} ${className}`}
    >
      {children}
    </Link>
  );
};

export default NavLink;
```

#### 1.3 Create Feedback Components

**File: `src/components/Feedback/LoadingSpinner.jsx`**
```jsx
const LoadingSpinner = ({ size = 'md', fullScreen = false }) => {
  const sizeClasses = {
    sm: 'w-4 h-4',
    md: 'w-12 h-12',
    lg: 'w-16 h-16'
  };

  const spinner = (
    <div className={`${sizeClasses[size]} border-4 border-blue-600 border-t-transparent rounded-full animate-spin`}></div>
  );

  if (fullScreen) {
    return (
      <div className="min-h-screen flex items-center justify-center">
        {spinner}
      </div>
    );
  }

  return spinner;
};

export default LoadingSpinner;
```

**File: `src/components/Feedback/Alert.jsx`**
```jsx
const Alert = ({ type = 'info', message, onClose }) => {
  const styles = {
    success: 'bg-green-50 border-green-200 text-green-800',
    error: 'bg-red-50 border-red-200 text-red-800',
    warning: 'bg-yellow-50 border-yellow-200 text-yellow-800',
    info: 'bg-blue-50 border-blue-200 text-blue-800'
  };

  const icons = {
    success: '✓',
    error: '✕',
    warning: '⚠',
    info: 'ℹ'
  };

  return (
    <div className={`p-4 border rounded-lg ${styles[type]} flex items-start justify-between`}>
      <div className="flex items-start">
        <span className="text-xl mr-3">{icons[type]}</span>
        <p className="text-sm">{message}</p>
      </div>
      {onClose && (
        <button
          onClick={onClose}
          className="text-gray-400 hover:text-gray-600"
          aria-label="Close alert"
        >
          ✕
        </button>
      )}
    </div>
  );
};

export default Alert;
```

**File: `src/components/Feedback/Badge.jsx`**
```jsx
const Badge = ({ children, variant = 'default', size = 'sm' }) => {
  const variants = {
    default: 'bg-gray-100 text-gray-800',
    primary: 'bg-blue-100 text-blue-800',
    success: 'bg-green-100 text-green-800',
    danger: 'bg-red-100 text-red-800',
    warning: 'bg-yellow-100 text-yellow-800',
    purple: 'bg-purple-100 text-purple-800',
    indigo: 'bg-indigo-100 text-indigo-800'
  };

  const sizes = {
    xs: 'px-2 py-0.5 text-xs',
    sm: 'px-2 py-1 text-xs',
    md: 'px-3 py-1 text-sm'
  };

  return (
    <span className={`font-semibold rounded-full ${variants[variant]} ${sizes[size]}`}>
      {children}
    </span>
  );
};

export default Badge;
```

**File: `src/components/Feedback/EmptyState.jsx`**
```jsx
const EmptyState = ({ icon, title, description, action }) => {
  return (
    <div className="text-center py-12">
      {icon && <div className="text-6xl mb-4">{icon}</div>}
      <h3 className="text-lg font-medium text-gray-900 mb-2">{title}</h3>
      {description && <p className="text-gray-600 mb-6">{description}</p>}
      {action && <div>{action}</div>}
    </div>
  );
};

export default EmptyState;
```

#### 1.4 Enhance Existing Button Component

**File: `src/components/Button.jsx` (Enhanced)**
```jsx
const Button = ({
  children,
  variant = 'primary',
  size = 'md',
  isLoading = false,
  fullWidth = false,
  icon,
  ...props
}) => {
  const baseClasses = 'font-semibold transition-colors focus:outline-none focus:ring-2 focus:ring-offset-2 rounded-lg disabled:opacity-50 disabled:cursor-not-allowed';

  const variants = {
    primary: 'bg-blue-600 text-white hover:bg-blue-700 focus:ring-blue-500',
    secondary: 'bg-gray-600 text-white hover:bg-gray-700 focus:ring-gray-500',
    success: 'bg-green-600 text-white hover:bg-green-700 focus:ring-green-500',
    danger: 'bg-red-600 text-white hover:bg-red-700 focus:ring-red-500',
    outline: 'border-2 border-gray-300 text-gray-700 hover:bg-gray-50 focus:ring-gray-500',
    ghost: 'text-gray-700 hover:bg-gray-100 focus:ring-gray-500',
    purple: 'bg-purple-600 text-white hover:bg-purple-700 focus:ring-purple-500',
    indigo: 'bg-indigo-600 text-white hover:bg-indigo-700 focus:ring-indigo-500'
  };

  const sizes = {
    sm: 'px-3 py-1.5 text-sm',
    md: 'px-4 py-2 text-base',
    lg: 'px-6 py-3 text-lg'
  };

  const widthClass = fullWidth ? 'w-full' : '';

  return (
    <button
      className={`${baseClasses} ${variants[variant]} ${sizes[size]} ${widthClass}`}
      disabled={isLoading || props.disabled}
      {...props}
    >
      {isLoading ? (
        <div className="flex items-center justify-center">
          <div className="w-5 h-5 border-2 border-white border-t-transparent rounded-full animate-spin"></div>
          <span className="ml-2">Loading...</span>
        </div>
      ) : (
        <div className="flex items-center justify-center gap-2">
          {icon && <span>{icon}</span>}
          <span>{children}</span>
        </div>
      )}
    </button>
  );
};

export default Button;
```

### Phase 2: Create Utility Hooks & Functions (Week 2)

#### 2.1 Custom Hooks

**File: `src/hooks/useExpenseBadges.js`**
```jsx
export const useExpenseBadges = () => {
  const getStatusVariant = (status) => {
    const statusMap = {
      SUBMITTED: 'primary',
      APPROVED: 'success',
      REJECTED: 'danger',
      PENDING: 'warning'
    };
    return statusMap[status] || 'default';
  };

  const getTypeVariant = (type) => {
    return type === 'EXPENSE' ? 'purple' : 'indigo';
  };

  return { getStatusVariant, getTypeVariant };
};
```

**File: `src/hooks/useRoleBadges.js`**
```jsx
export const useRoleBadges = () => {
  const getRoleVariant = (role) => {
    const roleMap = {
      ADMIN: 'purple',
      MANAGER: 'primary',
      FINANCE: 'warning',
      EMPLOYEE: 'default'
    };
    return roleMap[role] || 'default';
  };

  return { getRoleVariant };
};
```

**File: `src/hooks/useFormatters.js`**
```jsx
export const useFormatters = () => {
  const formatCurrency = (amount) => {
    return new Intl.NumberFormat('en-US', {
      style: 'currency',
      currency: 'USD'
    }).format(amount);
  };

  const formatDate = (dateString) => {
    return new Date(dateString).toLocaleDateString('en-US', {
      year: 'numeric',
      month: 'long',
      day: 'numeric'
    });
  };

  const formatDateTime = (dateString) => {
    return new Date(dateString).toLocaleString('en-US', {
      year: 'numeric',
      month: 'long',
      day: 'numeric',
      hour: '2-digit',
      minute: '2-digit'
    });
  };

  return { formatCurrency, formatDate, formatDateTime };
};
```

#### 2.2 Constants

**File: `src/constants/roles.js`**
```jsx
export const ROLES = {
  ADMIN: 'ADMIN',
  MANAGER: 'MANAGER',
  FINANCE: 'FINANCE',
  EMPLOYEE: 'EMPLOYEE'
};

export const canAccessUsers = (role) => {
  return [ROLES.ADMIN, ROLES.MANAGER].includes(role);
};

export const canAccessDepartments = (role) => {
  return [ROLES.ADMIN, ROLES.FINANCE].includes(role);
};
```

**File: `src/constants/expenseStatus.js`**
```jsx
export const EXPENSE_STATUS = {
  SUBMITTED: 'SUBMITTED',
  APPROVED: 'APPROVED',
  REJECTED: 'REJECTED',
  PENDING: 'PENDING'
};

export const EXPENSE_TYPE = {
  EXPENSE: 'EXPENSE',
  INVOICE: 'INVOICE'
};
```

### Phase 3: Implement Design System (Week 2)

**File: `src/styles/design-tokens.js`**
```jsx
export const colors = {
  primary: {
    50: '#eff6ff',
    100: '#dbeafe',
    500: '#3b82f6',
    600: '#2563eb',
    700: '#1d4ed8'
  },
  success: {
    100: '#dcfce7',
    600: '#16a34a',
    800: '#166534'
  },
  danger: {
    100: '#fee2e2',
    600: '#dc2626',
    800: '#991b1b'
  },
  // ... etc
};

export const spacing = {
  xs: '0.5rem',
  sm: '1rem',
  md: '1.5rem',
  lg: '2rem',
  xl: '3rem'
};

export const typography = {
  h1: 'text-3xl font-bold',
  h2: 'text-2xl font-bold',
  h3: 'text-xl font-semibold',
  body: 'text-base',
  small: 'text-sm'
};
```

### Phase 4: Refactor Pages (Week 3-4)

#### Example Refactored Page

**Before: `ExpenseListPage.jsx` (450 lines)**

**After: `ExpenseListPage.jsx` (200 lines)**
```jsx
import { useState, useEffect } from 'react';
import { useNavigate } from 'react-router-dom';
import { useAuth } from '../context/AuthContext';
import expenseService from '../api/expenseService';

// Layout components
import PageLayout from '../components/Layout/PageLayout';
import PageContainer from '../components/Layout/PageContainer';
import Card from '../components/Layout/Card';
import Navbar from '../components/Navigation/Navbar';

// UI components
import LoadingSpinner from '../components/Feedback/LoadingSpinner';
import Alert from '../components/Feedback/Alert';
import Badge from '../components/Feedback/Badge';
import EmptyState from '../components/Feedback/EmptyState';
import Button from '../components/Button';

// Hooks
import { useExpenseBadges } from '../hooks/useExpenseBadges';
import { useFormatters } from '../hooks/useFormatters';

const ExpenseListPage = () => {
  const { user } = useAuth();
  const navigate = useNavigate();
  const [expenses, setExpenses] = useState([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState(null);

  const { getStatusVariant, getTypeVariant } = useExpenseBadges();
  const { formatCurrency, formatDate } = useFormatters();

  // ... rest of logic

  if (loading) {
    return (
      <PageLayout>
        <Navbar />
        <LoadingSpinner fullScreen />
      </PageLayout>
    );
  }

  return (
    <PageLayout>
      <Navbar />
      <PageContainer>
        {error && (
          <Alert
            type="error"
            message={error}
            onClose={() => setError(null)}
          />
        )}

        <Card
          title="Expenses"
          actions={
            <Button
              variant="primary"
              onClick={() => navigate('/expenses/new')}
            >
              Create Expense
            </Button>
          }
        >
          {expenses.length === 0 ? (
            <EmptyState
              icon="📝"
              title="No expenses yet"
              description="Start by creating your first expense"
              action={
                <Button onClick={() => navigate('/expenses/new')}>
                  Create Expense
                </Button>
              }
            />
          ) : (
            <table className="min-w-full divide-y divide-gray-200">
              {/* ... table content with Badge components */}
              <td>
                <Badge variant={getTypeVariant(expense.type)}>
                  {expense.type}
                </Badge>
              </td>
              <td>
                <Badge variant={getStatusVariant(expense.status)}>
                  {expense.status}
                </Badge>
              </td>
            </table>
          )}
        </Card>
      </PageContainer>
    </PageLayout>
  );
};
```

## Folder Structure (Proposed)

```
src/
├── api/
│   ├── axiosClient.js
│   ├── departmentService.js
│   ├── expenseService.js
│   └── userService.js
├── components/
│   ├── Layout/
│   │   ├── PageLayout.jsx
│   │   ├── PageContainer.jsx
│   │   └── Card.jsx
│   ├── Navigation/
│   │   ├── Navbar.jsx
│   │   └── NavLink.jsx
│   ├── Feedback/
│   │   ├── LoadingSpinner.jsx
│   │   ├── Alert.jsx
│   │   ├── Badge.jsx
│   │   └── EmptyState.jsx
│   ├── Form/
│   │   ├── Button.jsx
│   │   ├── Input.jsx
│   │   ├── Select.jsx
│   │   └── FormGroup.jsx
│   └── index.js (barrel exports)
├── constants/
│   ├── roles.js
│   └── expenseStatus.js
├── context/
│   └── AuthContext.jsx
├── hooks/
│   ├── useExpenseBadges.js
│   ├── useRoleBadges.js
│   └── useFormatters.js
├── pages/
│   ├── Dashboard/
│   │   └── DashboardPage.jsx
│   ├── Expenses/
│   │   ├── ExpenseListPage.jsx
│   │   ├── ExpenseFormPage.jsx
│   │   └── ExpenseDetailPage.jsx
│   ├── Users/
│   │   └── UserManagementPage.jsx
│   ├── Departments/
│   │   └── DepartmentManagementPage.jsx
│   └── Auth/
│       ├── LoginPage.jsx
│       └── RegisterPage.jsx
├── router/
│   └── AppRouter.jsx
├── styles/
│   └── design-tokens.js
├── utils/
│   └── (future utility functions)
├── App.jsx
└── main.jsx
```

## Implementation Checklist

### Week 1: Core Components
- [ ] Create Layout components (PageLayout, PageContainer, Card)
- [ ] Create Navbar component with NavLink
- [ ] Create Feedback components (LoadingSpinner, Alert, Badge, EmptyState)
- [ ] Enhance Button component with more variants
- [ ] Create barrel exports (index.js)

### Week 2: Utilities & Design System
- [ ] Create custom hooks (useExpenseBadges, useRoleBadges, useFormatters)
- [ ] Create constants files (roles, expenseStatus)
- [ ] Define design tokens
- [ ] Create FormGroup component for better form layouts

### Week 3-4: Refactor Pages
- [ ] Refactor DashboardPage
- [ ] Refactor ExpenseListPage
- [ ] Refactor ExpenseFormPage
- [ ] Refactor ExpenseDetailPage
- [ ] Refactor UserManagementPage
- [ ] Refactor DepartmentManagementPage
- [ ] Refactor Auth pages (Login, Register)

### Week 5: Polish & Testing
- [ ] Add accessibility improvements (aria-labels, keyboard navigation)
- [ ] Add PropTypes or TypeScript types
- [ ] Add component documentation
- [ ] Update README with component usage examples
- [ ] Performance optimization (React.memo where needed)

## Best Practices Applied

### 1. Component Composition
✅ Small, focused components
✅ Composition over inheritance
✅ Container/Presentational pattern

### 2. State Management
✅ Context for global state (Auth)
✅ Local state for component-specific data
✅ Custom hooks for shared logic

### 3. Accessibility
✅ Semantic HTML
✅ ARIA labels
✅ Keyboard navigation
✅ Focus management

### 4. Performance
✅ Lazy loading for routes
✅ React.memo for expensive components
✅ useCallback/useMemo where appropriate

### 5. Code Quality
✅ Consistent naming conventions
✅ Single responsibility principle
✅ DRY (Don't Repeat Yourself)
✅ Proper separation of concerns

## Migration Strategy

### Step-by-Step Approach
1. Create new components without breaking existing code
2. Test new components in isolation
3. Gradually migrate pages one at a time
4. Run tests after each migration
5. Keep both versions until migration is complete
6. Remove old code once migration is verified

### Risk Mitigation
- Feature flags for gradual rollout
- Comprehensive testing at each step
- Code review for each component
- Documentation for each new pattern

## Expected Benefits

1. **Code Reduction**: ~40% reduction in total lines of code
2. **Maintainability**: Centralized component logic
3. **Consistency**: Unified design language
4. **Developer Experience**: Faster feature development
5. **Performance**: Better bundle optimization
6. **Accessibility**: Improved screen reader support
7. **Testing**: Easier to write unit tests

## Next Steps

1. Review and approve this plan
2. Set up component development environment
3. Create component library in Storybook (optional)
4. Begin Phase 1 implementation
5. Schedule code reviews after each phase
