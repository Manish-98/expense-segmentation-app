import axiosClient from './axiosClient';

export const categoryService = {
  getAllCategories: async () => {
    const response = await axiosClient.get('/categories');
    return response.data;
  },

  createCategory: async (categoryData) => {
    const response = await axiosClient.post('/categories', categoryData);
    return response.data;
  },

  deactivateCategory: async (categoryId) => {
    await axiosClient.delete(`/categories/${categoryId}`);
  }
};