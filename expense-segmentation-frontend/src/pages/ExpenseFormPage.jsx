import { useState } from 'react';
import { useAuth } from '../context/AuthContext';
import { useNavigate } from 'react-router-dom';
import expenseService from '../api/expenseService';
import Input from '../components/Input';
import Select from '../components/Select';
import Button from '../components/Button';

const ExpenseFormPage = () => {
  const { user, logout } = useAuth();
  const navigate = useNavigate();
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState(null);
  const [successMessage, setSuccessMessage] = useState(null);

  // Get today's date in YYYY-MM-DD format
  const getTodayDate = () => {
    const today = new Date();
    return today.toISOString().split('T')[0];
  };

  const [formData, setFormData] = useState({
    date: getTodayDate(),
    vendor: '',
    amount: '',
    description: '',
    type: 'EXPENSE',
  });

  const handleLogout = () => {
    logout();
    navigate('/login');
  };

  const handleInputChange = (e) => {
    const { name, value } = e.target;
    setFormData({
      ...formData,
      [name]: value,
    });
  };

  const handleSubmit = async (e) => {
    e.preventDefault();
    setLoading(true);
    setError(null);
    setSuccessMessage(null);

    try {
      // Validate required fields
      if (!formData.vendor || !formData.amount || !formData.type) {
        setError('Vendor, amount, and type are required');
        setLoading(false);
        return;
      }

      // Validate amount is positive
      const amount = parseFloat(formData.amount);
      if (isNaN(amount) || amount <= 0) {
        setError('Amount must be a positive number');
        setLoading(false);
        return;
      }

      // Build payload
      const payload = {
        vendor: formData.vendor.trim(),
        amount: amount,
        type: formData.type,
      };

      // Add date if provided (defaults to today on backend if null)
      if (formData.date) {
        payload.date = formData.date;
      }

      // Add description if provided
      if (formData.description && formData.description.trim()) {
        payload.description = formData.description.trim();
      }

      const response = await expenseService.createExpense(payload);

      setSuccessMessage(
        `${formData.type === 'EXPENSE' ? 'Expense' : 'Invoice'} created successfully! ID: ${response.id}`
      );

      // Reset form
      setFormData({
        date: getTodayDate(),
        vendor: '',
        amount: '',
        description: '',
        type: 'EXPENSE',
      });

      // Clear success message after 5 seconds
      setTimeout(() => setSuccessMessage(null), 5000);
    } catch (err) {
      console.error('Failed to create expense:', err);
      setError(err.response?.data?.message || 'Failed to create expense/invoice');
    } finally {
      setLoading(false);
    }
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
              {(user?.role === 'ADMIN' || user?.role === 'FINANCE') && (
                <button
                  onClick={() => navigate('/departments')}
                  className="text-gray-700 hover:text-gray-900 font-medium"
                >
                  Departments
                </button>
              )}
              {(user?.role === 'ADMIN' || user?.role === 'MANAGER') && (
                <button
                  onClick={() => navigate('/users')}
                  className="text-gray-700 hover:text-gray-900 font-medium"
                >
                  Users
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
      <main className="max-w-3xl mx-auto py-6 sm:px-6 lg:px-8">
        <div className="px-4 py-6 sm:px-0">
          {/* Header */}
          <div className="mb-6">
            <h2 className="text-2xl font-bold text-gray-900">
              Submit Expense or Invoice
            </h2>
            <p className="text-gray-600 mt-1">
              Enter the details of your expense or invoice for processing
            </p>
          </div>

          {/* Success/Error Messages */}
          {successMessage && (
            <div className="mb-4 p-4 bg-green-50 border border-green-200 rounded-lg">
              <p className="text-green-800">{successMessage}</p>
            </div>
          )}
          {error && (
            <div className="mb-4 p-4 bg-red-50 border border-red-200 rounded-lg">
              <p className="text-red-800">{error}</p>
            </div>
          )}

          {/* Form */}
          <div className="bg-white rounded-lg shadow p-6">
            <form onSubmit={handleSubmit}>
              <Select
                label="Type"
                name="type"
                value={formData.type}
                onChange={handleInputChange}
                required
                options={[
                  { value: 'EXPENSE', label: 'Expense' },
                  { value: 'INVOICE', label: 'Invoice' },
                ]}
              />

              <Input
                label="Date"
                type="date"
                name="date"
                value={formData.date}
                onChange={handleInputChange}
                required
                max={getTodayDate()}
              />

              <Input
                label="Vendor"
                type="text"
                name="vendor"
                value={formData.vendor}
                onChange={handleInputChange}
                required
                maxLength={255}
                placeholder="Enter vendor name"
              />

              <Input
                label="Amount"
                type="number"
                name="amount"
                value={formData.amount}
                onChange={handleInputChange}
                required
                min="0.01"
                step="0.01"
                placeholder="0.00"
              />

              <div className="mb-4">
                <label
                  htmlFor="description"
                  className="block text-sm font-medium text-gray-700 mb-2"
                >
                  Description (Optional)
                </label>
                <textarea
                  id="description"
                  name="description"
                  value={formData.description}
                  onChange={handleInputChange}
                  rows={4}
                  maxLength={5000}
                  placeholder="Enter any additional details about this expense or invoice"
                  className="w-full px-3 py-2 border border-gray-300 rounded-lg shadow-sm focus:outline-none focus:ring-2 focus:ring-blue-500 focus:border-blue-500"
                />
                <p className="text-xs text-gray-500 mt-1">
                  {formData.description.length}/5000 characters
                </p>
              </div>

              <div className="flex space-x-3 mt-6">
                <Button type="submit" isLoading={loading}>
                  Submit {formData.type === 'EXPENSE' ? 'Expense' : 'Invoice'}
                </Button>
                <Button
                  type="button"
                  variant="secondary"
                  onClick={() => navigate('/dashboard')}
                  disabled={loading}
                >
                  Cancel
                </Button>
              </div>
            </form>
          </div>
        </div>
      </main>
    </div>
  );
};

export default ExpenseFormPage;
