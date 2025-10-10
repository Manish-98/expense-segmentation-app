import axiosClient from './axiosClient';

export const expenseSegmentService = {
  getExpenseSegments: async (expenseId) => {
    const response = await axiosClient.get(`/expenses/${expenseId}/segments`);
    return response.data;
  },

  createExpenseSegment: async (expenseId, segmentData) => {
    const response = await axiosClient.post(`/expenses/${expenseId}/segments`, segmentData);
    return response.data;
  },

  createMultipleExpenseSegments: async (expenseId, segmentsData) => {
    const response = await axiosClient.post(`/expenses/${expenseId}/segments/batch`, segmentsData);
    return response.data;
  },

  replaceAllExpenseSegments: async (expenseId, segmentsData) => {
    const response = await axiosClient.put(`/expenses/${expenseId}/segments`, segmentsData);
    return response.data;
  }
};