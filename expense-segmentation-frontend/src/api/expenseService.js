import axiosClient from './axiosClient';

/**
 * Expense/Invoice Management API Service
 */
const expenseService = {
  /**
   * Create a new expense or invoice
   * @param {object} expenseData - Expense data (date, vendor, amount, description, type)
   * @returns {Promise} Created expense
   */
  createExpense: async (expenseData) => {
    const response = await axiosClient.post('/expenses', expenseData);
    return response.data;
  },

  /**
   * Get expenses with pagination and filters
   * @param {object} params - Query parameters
   * @param {number} params.page - Page number (0-indexed)
   * @param {number} params.size - Page size
   * @param {string} params.dateFrom - Filter by date from (YYYY-MM-DD)
   * @param {string} params.dateTo - Filter by date to (YYYY-MM-DD)
   * @param {string} params.type - Filter by expense type (EXPENSE, INVOICE)
   * @param {string} params.status - Filter by status
   * @returns {Promise} Paginated expense list with metadata
   */
  getExpenses: async (params = {}) => {
    const response = await axiosClient.get('/expenses', { params });
    return response.data;
  },

  /**
   * Get all expenses (Finance and Admin only) - Deprecated
   * @returns {Promise} List of all expenses
   * @deprecated Use getExpenses() with pagination instead
   */
  getAllExpenses: async () => {
    const response = await axiosClient.get('/expenses/all');
    return response.data;
  },

  /**
   * Get expense by ID
   * @param {string} expenseId - Expense ID
   * @returns {Promise} Expense details
   */
  getExpenseById: async (expenseId) => {
    const response = await axiosClient.get(`/expenses/${expenseId}`);
    return response.data;
  },

  /**
   * Get expenses by user ID (Manager, Finance, Admin)
   * @param {string} userId - User ID
   * @returns {Promise} List of expenses for the user
   */
  getExpensesByUser: async (userId) => {
    const response = await axiosClient.get(`/expenses/user/${userId}`);
    return response.data;
  },
};

export default expenseService;
