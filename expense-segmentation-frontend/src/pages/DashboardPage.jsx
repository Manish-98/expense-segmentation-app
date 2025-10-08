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
              <div className="mt-6 p-4 bg-blue-50 border border-blue-200 rounded-lg">
                <p className="text-blue-900 font-medium">Phase 1 Complete!</p>
                <p className="text-blue-700 text-sm mt-1">
                  Authentication and user management are now set up. Future features (expenses, approvals, etc.) will be added in upcoming phases.
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
