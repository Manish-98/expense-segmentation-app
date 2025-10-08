import { useAuth } from '../context/AuthContext';
import { useNavigate } from 'react-router-dom';
import Button from '../components/Button';

const DashboardPage = () => {
  const { user, logout } = useAuth();
  const navigate = useNavigate();

  const handleLogout = () => {
    logout();
    navigate('/login');
  };

  return (
    <div className="min-h-screen bg-gray-100">
      <nav className="bg-white shadow-sm">
        <div className="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8">
          <div className="flex justify-between h-16 items-center">
            <div className="flex-shrink-0">
              <h1 className="text-xl font-bold text-gray-900">
                Expense Segmentation App
              </h1>
            </div>
            <div className="flex items-center">
              <span className="text-gray-700 mr-4">
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

      <main className="max-w-7xl mx-auto py-6 sm:px-6 lg:px-8">
        <div className="px-4 py-6 sm:px-0">
          <div className="bg-white rounded-lg shadow p-6">
            <h2 className="text-2xl font-bold text-gray-900 mb-4">
              Dashboard
            </h2>
            <div className="border-t border-gray-200 pt-4">
              <p className="text-gray-700">
                Welcome to your dashboard, <span className="font-semibold">{user?.name}</span>!
              </p>
              <p className="text-gray-600 mt-2">
                Email: {user?.email}
              </p>
              <p className="text-gray-600 mt-2">
                Role: {user?.role || 'User'}
              </p>

              {/* Quick Actions */}
              {(user?.role === 'ADMIN' || user?.role === 'MANAGER' || user?.role === 'FINANCE') && (
                <div className="mt-6">
                  <h3 className="text-lg font-semibold text-gray-900 mb-3">Quick Actions</h3>
                  <div className="flex flex-wrap gap-3">
                    {(user?.role === 'ADMIN' || user?.role === 'MANAGER') && (
                      <button
                        onClick={() => navigate('/users')}
                        className="px-4 py-2 bg-blue-600 text-white rounded-lg hover:bg-blue-700 transition-colors font-medium"
                      >
                        Manage Users
                      </button>
                    )}
                    {(user?.role === 'ADMIN' || user?.role === 'FINANCE') && (
                      <button
                        onClick={() => navigate('/departments')}
                        className="px-4 py-2 bg-green-600 text-white rounded-lg hover:bg-green-700 transition-colors font-medium"
                      >
                        {user?.role === 'ADMIN' ? 'Manage Departments' : 'View Departments'}
                      </button>
                    )}
                  </div>
                </div>
              )}

              <div className="mt-6 p-4 bg-blue-50 border border-blue-200 rounded-lg">
                <p className="text-blue-900 font-medium">User Management Available!</p>
                <p className="text-blue-700 text-sm mt-1">
                  {user?.role === 'ADMIN' && 'As an Admin, you can view and manage all users in the system.'}
                  {user?.role === 'MANAGER' && 'As a Manager, you can view users in your department.'}
                  {user?.role !== 'ADMIN' && user?.role !== 'MANAGER' && 'Authentication and user management are now set up. Future features (expenses, approvals, etc.) will be added in upcoming phases.'}
                </p>
              </div>
            </div>
          </div>
        </div>
      </main>
    </div>
  );
};

export default DashboardPage;
