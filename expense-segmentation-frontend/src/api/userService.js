import axiosClient from './axiosClient';

/**
 * User Management API Service
 */
const userService = {
  /**
   * Get all users (Admin only)
   * @returns {Promise} List of all users
   */
  getAllUsers: async () => {
    const response = await axiosClient.get('/users');
    return response.data;
  },

  /**
   * Get users in manager's department (Manager only)
   * @returns {Promise} List of department users
   */
  getDepartmentUsers: async () => {
    const response = await axiosClient.get('/users/department');
    return response.data;
  },

  /**
   * Update user (Admin only)
   * @param {string} userId - User ID
   * @param {object} updateData - Update data (role, departmentId)
   * @returns {Promise} Updated user
   */
  updateUser: async (userId, updateData) => {
    const response = await axiosClient.patch(`/users/${userId}`, updateData);
    return response.data;
  },

  /**
   * Deactivate user (Admin only)
   * @param {string} userId - User ID
   * @returns {Promise} void
   */
  deactivateUser: async (userId) => {
    await axiosClient.delete(`/users/${userId}`);
  },

  /**
   * Get all departments
   * @returns {Promise} List of departments
   */
  getAllDepartments: async () => {
    const response = await axiosClient.get('/departments');
    return response.data;
  },

  /**
   * Get all roles
   * @returns {Promise} List of roles
   */
  getAllRoles: async () => {
    const response = await axiosClient.get('/roles');
    return response.data;
  },
};

export default userService;
