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
