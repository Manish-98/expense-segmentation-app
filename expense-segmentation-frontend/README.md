# Expense Segmentation App - Frontend

A modern React.js frontend for the Expense Segmentation App, featuring user authentication, registration, and JWT-based session management.

## 🎯 Overview

This is **Phase 1** of the frontend implementation, focusing on:

- User Registration
- User Login
- JWT Token Persistence
- Protected Routes
- Basic Dashboard

## 🧱 Tech Stack

- **React 18+** - UI library
- **Vite** - Build tool and dev server
- **React Router v6** - Client-side routing
- **Axios** - HTTP client for API calls
- **Tailwind CSS** - Utility-first CSS framework
- **React Hook Form** - Form handling
- **Yup** - Form validation
- **LocalStorage** - JWT token persistence

## 📁 Project Structure

```
src/
├── api/
│   └── axiosClient.js          # Axios instance with JWT interceptor
├── components/
│   ├── Input.jsx               # Reusable input component
│   └── Button.jsx              # Reusable button component
├── pages/
│   ├── LoginPage.jsx           # Login page
│   ├── RegisterPage.jsx        # Registration page
│   └── DashboardPage.jsx       # Protected dashboard
├── context/
│   └── AuthContext.jsx         # Global auth state management
├── router/
│   └── AppRouter.jsx           # Route configuration
├── App.jsx                     # Main app component
├── main.jsx                    # React entry point
└── index.css                   # Global styles with Tailwind
```

## 🚀 Getting Started

### Prerequisites

- Node.js (v18 or higher)
- npm or yarn
- Backend server running (see `../expense-segmentation-backend`)

### Installation

1. **Navigate to the frontend directory:**

   ```bash
   cd expense-segmentation-frontend
   ```

2. **Install dependencies:**

   ```bash
   npm install
   ```

3. **Set up environment variables:**

   Create a `.env` file in the root of the frontend directory:

   ```bash
   cp .env.example .env
   ```

   Update the `.env` file with your backend URL:

   ```env
   VITE_API_BASE_URL=http://localhost:8080
   ```

   > **Note:** If your backend is running on a different port or host, update the URL accordingly.

### Running the Application

Start the development server:

```bash
npm run dev
```

The application will be available at `http://localhost:3000`

### Building for Production

To create a production build:

```bash
npm run build
```

To preview the production build:

```bash
npm run preview
```

## 🔐 Features

### 1. User Registration

- Navigate to `/register` or click "Sign up" from the login page
- Fill in your name, email, and password (minimum 6 characters)
- Upon successful registration, you'll be automatically logged in and redirected to the dashboard

### 2. User Login

- Navigate to `/login` or click "Sign in" from the registration page
- Enter your email and password
- Upon successful login, you'll be redirected to the dashboard
- Your JWT token will be stored in localStorage for persistent sessions

### 3. Protected Dashboard

- Accessible only when logged in
- Displays user information (name, email, role)
- Includes logout functionality
- Automatically redirects to login if not authenticated

### 4. Automatic Token Management

- JWT token is automatically attached to all API requests via Axios interceptor
- Token is persisted in localStorage
- Automatic redirect to login on 401 (Unauthorized) responses
- Token validation on app load

## 🧪 Testing the Application

### Testing Registration Flow

1. Start the backend server
2. Start the frontend dev server (`npm run dev`)
3. Navigate to `http://localhost:3000/register`
4. Fill in the registration form:
   - Name: "John Doe"
   - Email: "john@example.com"
   - Password: "password123"
5. Click "Sign Up"
6. You should be redirected to the dashboard

### Testing Login Flow

1. Navigate to `http://localhost:3000/login`
2. Enter your credentials
3. Click "Sign In"
4. You should be redirected to the dashboard

### Testing Protected Routes

1. Open a new incognito/private browser window
2. Navigate to `http://localhost:3000/dashboard`
3. You should be redirected to `/login`
4. After logging in, you'll be redirected back to the dashboard

### Testing Logout

1. From the dashboard, click the "Logout" button
2. You should be redirected to the login page
3. Try accessing `/dashboard` again - you should be redirected to login

### Testing Token Persistence

1. Log in to the application
2. Close the browser tab
3. Open a new tab and navigate to `http://localhost:3000`
4. You should still be logged in (redirected to dashboard)

## 🔧 API Integration

The frontend communicates with the following backend endpoints:

- `POST /auth/register` - User registration
- `POST /auth/login` - User login
- `GET /auth/me` - Get current user (requires JWT token)

All API calls use the base URL defined in `VITE_API_BASE_URL` environment variable.

## 📝 Environment Variables

| Variable | Description | Default |
|----------|-------------|---------|
| `VITE_API_BASE_URL` | Backend API base URL | `http://localhost:8080` |

## 🎨 Styling

This project uses Tailwind CSS for styling. The configuration can be found in:

- `tailwind.config.js` - Tailwind configuration
- `postcss.config.js` - PostCSS configuration
- `src/index.css` - Global styles and Tailwind imports

## 🔍 Troubleshooting

### CORS Errors

If you encounter CORS errors, ensure your backend has CORS properly configured to allow requests from `http://localhost:3000`.

### API Connection Issues

1. Verify the backend is running
2. Check the `VITE_API_BASE_URL` in your `.env` file
3. Ensure the URL doesn't have a trailing slash

### Token Not Persisting

1. Check browser console for errors
2. Verify localStorage is enabled in your browser
3. Check if the backend is returning a `token` field in the response

### Build Errors

1. Delete `node_modules` and reinstall:
   ```bash
   rm -rf node_modules
   npm install
   ```

2. Clear Vite cache:
   ```bash
   rm -rf node_modules/.vite
   ```

## 🚧 Future Enhancements

Phase 1 is now complete! Future phases will include:

- Expense creation and management
- Invoice upload and processing
- Approval workflows
- Admin dashboard
- User management
- Role-based access control
- Reports and analytics

## 📄 License

This project is part of the Expense Segmentation App.

## 🤝 Contributing

1. Ensure all changes pass linting: `npm run lint`
2. Test your changes thoroughly
3. Follow the existing code style and patterns

## 📞 Support

For issues or questions, please refer to the main project repository.
