import { useAuth } from '../context/AuthContext';
import { useNavigate } from 'react-router-dom';
import PageLayout from '../components/Layout/PageLayout';
import PageContainer from '../components/Layout/PageContainer';
import Card from '../components/Layout/Card';
import Navbar from '../components/Navigation/Navbar';
import Button from '../components/Button';
import Alert from '../components/Feedback/Alert';

const DashboardPage = () => {
  const { user } = useAuth();
  const navigate = useNavigate();

  return (
    <PageLayout>
      <Navbar />
      <PageContainer>
        <Card title="Dashboard">
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
            <div className="mt-6">
              <h3 className="text-lg font-semibold text-gray-900 mb-3">Quick Actions</h3>
              <div className="flex flex-wrap gap-3">
                <Button
                  variant="purple"
                  onClick={() => navigate('/expenses/new')}
                >
                  Submit Expense/Invoice
                </Button>
                <Button
                  variant="indigo"
                  onClick={() => navigate('/expenses')}
                >
                  View Expenses
                </Button>
                {(user?.role === 'ADMIN' || user?.role === 'MANAGER') && (
                  <Button
                    variant="primary"
                    onClick={() => navigate('/users')}
                  >
                    Manage Users
                  </Button>
                )}
                {(user?.role === 'ADMIN' || user?.role === 'FINANCE') && (
                  <Button
                    variant="success"
                    onClick={() => navigate('/departments')}
                  >
                    {user?.role === 'ADMIN' ? 'Manage Departments' : 'View Departments'}
                  </Button>
                )}
              </div>
            </div>

            <div className="mt-6">
              <Alert
                type="info"
                message={
                  user?.role === 'ADMIN'
                    ? 'As an Admin, you can view and manage all users in the system.'
                    : user?.role === 'MANAGER'
                    ? 'As a Manager, you can view users in your department.'
                    : 'Authentication and user management are now set up. Future features (expenses, approvals, etc.) will be added in upcoming phases.'
                }
              />
            </div>
          </div>
        </Card>
      </PageContainer>
    </PageLayout>
  );
};

export default DashboardPage;
