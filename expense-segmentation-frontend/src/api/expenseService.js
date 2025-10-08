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
   * Get all expenses (Finance and Admin only)
   * @returns {Promise} List of all expenses
   */
  getAllExpenses: async () => {
    const response = await axiosClient.get('/expenses');
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
