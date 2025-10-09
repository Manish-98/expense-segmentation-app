import { useState, useEffect } from 'react';
import { useAuth } from '../context/AuthContext';
import { useNavigate } from 'react-router-dom';
import expenseService from '../api/expenseService';
import PageLayout from '../components/Layout/PageLayout';
import PageContainer from '../components/Layout/PageContainer';
import Card from '../components/Layout/Card';
import Navbar from '../components/Navigation/Navbar';
import Button from '../components/Button';
import LoadingSpinner from '../components/Feedback/LoadingSpinner';
import Alert from '../components/Feedback/Alert';
import Badge from '../components/Feedback/Badge';
import EmptyState from '../components/Feedback/EmptyState';
import { useExpenseBadges } from '../hooks/useExpenseBadges';
import { useFormatters } from '../hooks/useFormatters';

const ExpenseListPage = () => {
  const { user } = useAuth();
  const navigate = useNavigate();
  const [expenses, setExpenses] = useState([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState(null);

  // Pagination state
  const [page, setPage] = useState(0);
  const [size] = useState(10);
  const [totalPages, setTotalPages] = useState(0);
  const [totalElements, setTotalElements] = useState(0);

  // Filter state
  const [filters, setFilters] = useState({
    dateFrom: '',
    dateTo: '',
    type: '',
    status: '',
  });

  const { getStatusVariant, getTypeVariant } = useExpenseBadges();
  const { formatCurrency, formatDate } = useFormatters();

  const fetchExpenses = async () => {
    setLoading(true);
    setError(null);

    try {
      const params = { page, size };

      // Add filters if they have values
      if (filters.dateFrom) params.dateFrom = filters.dateFrom;
      if (filters.dateTo) params.dateTo = filters.dateTo;
      if (filters.type) params.type = filters.type;
      if (filters.status) params.status = filters.status;

      const response = await expenseService.getExpenses(params);
      setExpenses(response.expenses);
      setTotalPages(response.totalPages);
      setTotalElements(response.totalElements);
    } catch (err) {
      console.error('Failed to fetch expenses:', err);
      setError(err.response?.data?.message || 'Failed to load expenses');
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => {
    fetchExpenses();
  }, [page]);

  const handleFilterChange = (e) => {
    const { name, value } = e.target;
    setFilters({
      ...filters,
      [name]: value,
    });
  };

  const handleApplyFilters = () => {
    setPage(0); // Reset to first page when applying filters
    fetchExpenses();
  };

  const handleClearFilters = () => {
    setFilters({
      dateFrom: '',
      dateTo: '',
      type: '',
      status: '',
    });
    setPage(0);
    // Fetch will trigger automatically due to page change
    setTimeout(() => fetchExpenses(), 0);
  };

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

        <div className="mb-6 flex justify-between items-center">
          <div>
            <h2 className="text-2xl font-bold text-gray-900">Expenses</h2>
            <p className="text-gray-600 mt-1">
              {user?.role === 'ADMIN' || user?.role === 'FINANCE'
                ? 'View and manage all expenses'
                : 'View your submitted expenses'}
            </p>
          </div>
          <Button onClick={() => navigate('/expenses/new')}>
            Submit New Expense
          </Button>
        </div>

        <Card title="Filters" className="mb-6">
          <div className="grid grid-cols-1 md:grid-cols-4 gap-4">
            <div>
              <label className="block text-sm font-medium text-gray-700 mb-2">
                Date From
              </label>
              <input
                type="date"
                name="dateFrom"
                value={filters.dateFrom}
                onChange={handleFilterChange}
                className="w-full px-3 py-2 border border-gray-300 rounded-lg focus:outline-none focus:ring-2 focus:ring-blue-500"
              />
            </div>
            <div>
              <label className="block text-sm font-medium text-gray-700 mb-2">
                Date To
              </label>
              <input
                type="date"
                name="dateTo"
                value={filters.dateTo}
                onChange={handleFilterChange}
                className="w-full px-3 py-2 border border-gray-300 rounded-lg focus:outline-none focus:ring-2 focus:ring-blue-500"
              />
            </div>
            <div>
              <label className="block text-sm font-medium text-gray-700 mb-2">
                Type
              </label>
              <select
                name="type"
                value={filters.type}
                onChange={handleFilterChange}
                className="w-full px-3 py-2 border border-gray-300 rounded-lg focus:outline-none focus:ring-2 focus:ring-blue-500"
              >
                <option value="">All</option>
                <option value="EXPENSE">Expense</option>
                <option value="INVOICE">Invoice</option>
              </select>
            </div>
            <div>
              <label className="block text-sm font-medium text-gray-700 mb-2">
                Status
              </label>
              <select
                name="status"
                value={filters.status}
                onChange={handleFilterChange}
                className="w-full px-3 py-2 border border-gray-300 rounded-lg focus:outline-none focus:ring-2 focus:ring-blue-500"
              >
                <option value="">All</option>
                <option value="SUBMITTED">Submitted</option>
                <option value="APPROVED">Approved</option>
                <option value="REJECTED">Rejected</option>
                <option value="PENDING">Pending</option>
              </select>
            </div>
          </div>
          <div className="mt-4 flex space-x-3">
            <Button onClick={handleApplyFilters}>Apply Filters</Button>
            <Button variant="secondary" onClick={handleClearFilters}>
              Clear Filters
            </Button>
          </div>
        </Card>

        <Card className="overflow-hidden">
          {expenses.length === 0 ? (
            <EmptyState
              icon="📝"
              title="No expenses found"
              description="Try adjusting your filters or submit a new expense"
              action={
                <Button onClick={() => navigate('/expenses/new')}>
                  Submit New Expense
                </Button>
              }
            />
          ) : (
              <>
                <table className="min-w-full divide-y divide-gray-200">
                  <thead className="bg-gray-50">
                    <tr>
                      <th className="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">
                        Date
                      </th>
                      <th className="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">
                        Vendor
                      </th>
                      <th className="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">
                        Amount
                      </th>
                      <th className="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">
                        Type
                      </th>
                      <th className="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">
                        Status
                      </th>
                      {(user?.role === 'ADMIN' || user?.role === 'FINANCE') && (
                        <th className="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">
                          Created By
                        </th>
                      )}
                      <th className="px-6 py-3 text-right text-xs font-medium text-gray-500 uppercase tracking-wider">
                        Actions
                      </th>
                    </tr>
                  </thead>
                  <tbody className="bg-white divide-y divide-gray-200">
                    {expenses.map((expense) => (
                      <tr key={expense.id} className="hover:bg-gray-50">
                        <td className="px-6 py-4 whitespace-nowrap text-sm text-gray-900">
                          {formatDate(expense.date)}
                        </td>
                        <td className="px-6 py-4 whitespace-nowrap text-sm text-gray-900">
                          {expense.vendor}
                        </td>
                        <td className="px-6 py-4 whitespace-nowrap text-sm font-medium text-gray-900">
                          {formatCurrency(expense.amount)}
                        </td>
                        <td className="px-6 py-4 whitespace-nowrap">
                          <Badge variant={getTypeVariant(expense.type)}>
                            {expense.type}
                          </Badge>
                        </td>
                        <td className="px-6 py-4 whitespace-nowrap">
                          <Badge variant={getStatusVariant(expense.status)}>
                            {expense.status}
                          </Badge>
                        </td>
                        {(user?.role === 'ADMIN' || user?.role === 'FINANCE') && (
                          <td className="px-6 py-4 whitespace-nowrap text-sm text-gray-900">
                            {expense.createdByName}
                          </td>
                        )}
                        <td className="px-6 py-4 whitespace-nowrap text-right text-sm font-medium">
                          <button
                            onClick={() => navigate(`/expenses/${expense.id}`)}
                            className="text-blue-600 hover:text-blue-900"
                          >
                            View Details
                          </button>
                        </td>
                      </tr>
                    ))}
                  </tbody>
                </table>

                {/* Pagination */}
                <div className="bg-white px-4 py-3 flex items-center justify-between border-t border-gray-200 sm:px-6">
                  <div className="flex-1 flex justify-between sm:hidden">
                    <button
                      onClick={() => setPage(page - 1)}
                      disabled={page === 0}
                      className="relative inline-flex items-center px-4 py-2 border border-gray-300 text-sm font-medium rounded-md text-gray-700 bg-white hover:bg-gray-50 disabled:opacity-50 disabled:cursor-not-allowed"
                    >
                      Previous
                    </button>
                    <button
                      onClick={() => setPage(page + 1)}
                      disabled={page >= totalPages - 1}
                      className="ml-3 relative inline-flex items-center px-4 py-2 border border-gray-300 text-sm font-medium rounded-md text-gray-700 bg-white hover:bg-gray-50 disabled:opacity-50 disabled:cursor-not-allowed"
                    >
                      Next
                    </button>
                  </div>
                  <div className="hidden sm:flex-1 sm:flex sm:items-center sm:justify-between">
                    <div>
                      <p className="text-sm text-gray-700">
                        Showing <span className="font-medium">{page * size + 1}</span> to{' '}
                        <span className="font-medium">
                          {Math.min((page + 1) * size, totalElements)}
                        </span>{' '}
                        of <span className="font-medium">{totalElements}</span> results
                      </p>
                    </div>
                    <div>
                      <nav className="relative z-0 inline-flex rounded-md shadow-sm -space-x-px">
                        <button
                          onClick={() => setPage(page - 1)}
                          disabled={page === 0}
                          className="relative inline-flex items-center px-2 py-2 rounded-l-md border border-gray-300 bg-white text-sm font-medium text-gray-500 hover:bg-gray-50 disabled:opacity-50 disabled:cursor-not-allowed"
                        >
                          Previous
                        </button>
                        <span className="relative inline-flex items-center px-4 py-2 border border-gray-300 bg-white text-sm font-medium text-gray-700">
                          Page {page + 1} of {totalPages}
                        </span>
                        <button
                          onClick={() => setPage(page + 1)}
                          disabled={page >= totalPages - 1}
                          className="relative inline-flex items-center px-2 py-2 rounded-r-md border border-gray-300 bg-white text-sm font-medium text-gray-500 hover:bg-gray-50 disabled:opacity-50 disabled:cursor-not-allowed"
                        >
                          Next
                        </button>
                      </nav>
                    </div>
                  </div>
                </div>
            </>
          )}
        </Card>
      </PageContainer>
    </PageLayout>
  );
};

export default ExpenseListPage;
