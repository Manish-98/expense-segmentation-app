import { useState, useEffect } from 'react';
import { useAuth } from '../context/AuthContext';
import { useNavigate, useParams } from 'react-router-dom';
import expenseService from '../api/expenseService';
import Button from '../components/Button';

const ExpenseDetailPage = () => {
  const { user, logout } = useAuth();
  const navigate = useNavigate();
  const { id } = useParams();
  const [expense, setExpense] = useState(null);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState(null);

  const handleLogout = () => {
    logout();
    navigate('/login');
  };

  useEffect(() => {
    const fetchExpense = async () => {
      setLoading(true);
      setError(null);

      try {
        const data = await expenseService.getExpenseById(id);
        setExpense(data);
      } catch (err) {
        console.error('Failed to fetch expense:', err);
        setError(err.response?.data?.message || 'Failed to load expense details');
      } finally {
        setLoading(false);
      }
    };

    if (id) {
      fetchExpense();
    }
  }, [id]);

  const formatDate = (dateString) => {
    return new Date(dateString).toLocaleDateString();
  };

  const formatDateTime = (dateTimeString) => {
    return new Date(dateTimeString).toLocaleString();
  };

  const formatAmount = (amount) => {
    return new Intl.NumberFormat('en-US', {
      style: 'currency',
      currency: 'USD',
    }).format(amount);
  };

  const getStatusBadgeColor = (status) => {
    switch (status) {
      case 'SUBMITTED':
        return 'bg-blue-100 text-blue-800';
      case 'APPROVED':
        return 'bg-green-100 text-green-800';
      case 'REJECTED':
        return 'bg-red-100 text-red-800';
      case 'PENDING':
        return 'bg-yellow-100 text-yellow-800';
      default:
        return 'bg-gray-100 text-gray-800';
    }
  };

  const getTypeBadgeColor = (type) => {
    return type === 'EXPENSE'
      ? 'bg-purple-100 text-purple-800'
      : 'bg-indigo-100 text-indigo-800';
  };

  return (
    <div className="min-h-screen bg-gray-100">
      {/* Navigation */}
      <nav className="bg-white shadow-sm">
        <div className="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8">
          <div className="flex justify-between h-16 items-center">
            <div className="flex-shrink-0">
              <h1 className="text-xl font-bold text-gray-900">
                Expense Segmentation App
              </h1>
            </div>
            <div className="flex items-center space-x-4">
              <button
                onClick={() => navigate('/dashboard')}
                className="text-gray-700 hover:text-gray-900 font-medium"
              >
                Dashboard
              </button>
              <button
                onClick={() => navigate('/expenses')}
                className="text-blue-600 hover:text-blue-800 font-medium"
              >
                Expenses
              </button>
              {(user?.role === 'ADMIN' || user?.role === 'MANAGER') && (
                <button
                  onClick={() => navigate('/users')}
                  className="text-gray-700 hover:text-gray-900 font-medium"
                >
                  Users
                </button>
              )}
              {(user?.role === 'ADMIN' || user?.role === 'FINANCE') && (
                <button
                  onClick={() => navigate('/departments')}
                  className="text-gray-700 hover:text-gray-900 font-medium"
                >
                  Departments
                </button>
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

      {/* Main Content */}
      <main className="max-w-4xl mx-auto py-6 sm:px-6 lg:px-8">
        <div className="px-4 py-6 sm:px-0">
          {/* Header */}
          <div className="mb-6 flex items-center">
            <button
              onClick={() => navigate('/expenses')}
              className="mr-4 text-gray-600 hover:text-gray-900"
            >
              ← Back to Expenses
            </button>
            <h2 className="text-2xl font-bold text-gray-900">
              Expense Details
            </h2>
          </div>

          {/* Error Message */}
          {error && (
            <div className="mb-4 p-4 bg-red-50 border border-red-200 rounded-lg">
              <p className="text-red-800">{error}</p>
            </div>
          )}

          {/* Loading State */}
          {loading ? (
            <div className="bg-white rounded-lg shadow p-6 flex justify-center items-center">
              <div className="w-12 h-12 border-4 border-blue-600 border-t-transparent rounded-full animate-spin"></div>
            </div>
          ) : expense ? (
            <div className="bg-white rounded-lg shadow overflow-hidden">
              {/* Header Section */}
              <div className="bg-gray-50 px-6 py-4 border-b border-gray-200">
                <div className="flex justify-between items-start">
                  <div>
                    <h3 className="text-lg font-semibold text-gray-900">
                      {expense.vendor}
                    </h3>
                    <p className="text-sm text-gray-500 mt-1">ID: {expense.id}</p>
                  </div>
                  <div className="flex space-x-2">
                    <span className={`px-3 py-1 text-sm font-semibold rounded-full ${getTypeBadgeColor(expense.type)}`}>
                      {expense.type}
                    </span>
                    <span className={`px-3 py-1 text-sm font-semibold rounded-full ${getStatusBadgeColor(expense.status)}`}>
                      {expense.status}
                    </span>
                  </div>
                </div>
              </div>

              {/* Details Section */}
              <div className="px-6 py-6">
                <dl className="grid grid-cols-1 gap-6 sm:grid-cols-2">
                  {/* Amount */}
                  <div>
                    <dt className="text-sm font-medium text-gray-500">Amount</dt>
                    <dd className="mt-1 text-2xl font-bold text-gray-900">
                      {formatAmount(expense.amount)}
                    </dd>
                  </div>

                  {/* Date */}
                  <div>
                    <dt className="text-sm font-medium text-gray-500">Date</dt>
                    <dd className="mt-1 text-lg text-gray-900">
                      {formatDate(expense.date)}
                    </dd>
                  </div>

                  {/* Vendor */}
                  <div>
                    <dt className="text-sm font-medium text-gray-500">Vendor</dt>
                    <dd className="mt-1 text-sm text-gray-900">
                      {expense.vendor}
                    </dd>
                  </div>

                  {/* Type */}
                  <div>
                    <dt className="text-sm font-medium text-gray-500">Type</dt>
                    <dd className="mt-1 text-sm text-gray-900">
                      {expense.type}
                    </dd>
                  </div>

                  {/* Status */}
                  <div>
                    <dt className="text-sm font-medium text-gray-500">Status</dt>
                    <dd className="mt-1 text-sm text-gray-900">
                      {expense.status}
                    </dd>
                  </div>

                  {/* Created By */}
                  <div>
                    <dt className="text-sm font-medium text-gray-500">Created By</dt>
                    <dd className="mt-1 text-sm text-gray-900">
                      {expense.createdByName} ({expense.createdByEmail})
                    </dd>
                  </div>

                  {/* Created At */}
                  <div>
                    <dt className="text-sm font-medium text-gray-500">Created At</dt>
                    <dd className="mt-1 text-sm text-gray-900">
                      {formatDateTime(expense.createdAt)}
                    </dd>
                  </div>

                  {/* Updated At */}
                  <div>
                    <dt className="text-sm font-medium text-gray-500">Updated At</dt>
                    <dd className="mt-1 text-sm text-gray-900">
                      {formatDateTime(expense.updatedAt)}
                    </dd>
                  </div>

                  {/* Description - Full Width */}
                  {expense.description && (
                    <div className="sm:col-span-2">
                      <dt className="text-sm font-medium text-gray-500">Description</dt>
                      <dd className="mt-1 text-sm text-gray-900 whitespace-pre-wrap">
                        {expense.description}
                      </dd>
                    </div>
                  )}
                </dl>
              </div>

              {/* Attachments Section - Placeholder for Phase 2 */}
              <div className="bg-gray-50 px-6 py-4 border-t border-gray-200">
                <h4 className="text-sm font-medium text-gray-500 mb-2">Attachments</h4>
                <p className="text-sm text-gray-400 italic">
                  Attachment support will be available in Phase 2
                </p>
              </div>

              {/* Actions */}
              <div className="bg-white px-6 py-4 border-t border-gray-200 flex justify-end space-x-3">
                <Button
                  variant="secondary"
                  onClick={() => navigate('/expenses')}
                >
                  Back to List
                </Button>
              </div>
            </div>
          ) : null}
        </div>
      </main>
    </div>
  );
};

export default ExpenseDetailPage;
