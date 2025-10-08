import axiosClient from './axiosClient';

/**
 * Department Management API Service
 */
const departmentService = {
  /**
   * Get all departments (Admin and Finance)
   * @returns {Promise} List of all departments
   */
  getAllDepartments: async () => {
    const response = await axiosClient.get('/departments');
    return response.data;
  },

  /**
   * Get department by ID (Admin and Finance)
   * @param {string} departmentId - Department ID
   * @returns {Promise} Department details
   */
  getDepartmentById: async (departmentId) => {
    const response = await axiosClient.get(`/departments/${departmentId}`);
    return response.data;
  },

  /**
   * Create department (Admin only)
   * @param {object} departmentData - Department data (name, code, managerId)
   * @returns {Promise} Created department
   */
  createDepartment: async (departmentData) => {
    const response = await axiosClient.post('/departments', departmentData);
    return response.data;
  },

  /**
   * Update department (Admin only)
   * @param {string} departmentId - Department ID
   * @param {object} updateData - Update data (name, managerId)
   * @returns {Promise} Updated department
   */
  updateDepartment: async (departmentId, updateData) => {
    const response = await axiosClient.patch(`/departments/${departmentId}`, updateData);
    return response.data;
  },

  /**
   * Get all users (for manager dropdown)
   * @returns {Promise} List of users
   */
  getAllUsers: async () => {
    const response = await axiosClient.get('/users');
    return response.data;
  },
};

export default departmentService;
