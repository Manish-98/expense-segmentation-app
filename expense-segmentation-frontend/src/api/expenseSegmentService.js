import axiosClient from './axiosClient';

export const expenseSegmentService = {
  getExpenseSegments: async (expenseId) => {
    const response = await axiosClient.get(`/expenses/${expenseId}/segments`);
    return response.data;
  }
};