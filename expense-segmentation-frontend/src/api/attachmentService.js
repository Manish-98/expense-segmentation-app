import axiosClient from './axiosClient';

/**
 * Expense Attachment API Service
 */
const attachmentService = {
  /**
   * Upload an attachment for an expense
   * @param {string} expenseId - Expense ID
   * @param {File} file - File to upload
   * @returns {Promise} Uploaded attachment details
   */
  uploadAttachment: async (expenseId, file) => {
    const formData = new FormData();
    formData.append('file', file);

    const response = await axiosClient.post(
      `/expenses/${expenseId}/attachments`,
      formData,
      {
        headers: {
          'Content-Type': 'multipart/form-data',
        },
      }
    );
    return response.data;
  },

  /**
   * Get all attachments for an expense
   * @param {string} expenseId - Expense ID
   * @returns {Promise} List of attachments
   */
  getAttachments: async (expenseId) => {
    const response = await axiosClient.get(`/expenses/${expenseId}/attachments`);
    return response.data;
  },

  /**
   * Download an attachment
   * @param {string} expenseId - Expense ID
   * @param {string} attachmentId - Attachment ID
   * @returns {Promise} File blob
   */
  downloadAttachment: async (expenseId, attachmentId) => {
    const response = await axiosClient.get(
      `/expenses/${expenseId}/attachments/${attachmentId}`,
      {
        responseType: 'blob',
      }
    );
    return response;
  },

  /**
   * Delete an attachment
   * @param {string} expenseId - Expense ID
   * @param {string} attachmentId - Attachment ID
   * @returns {Promise}
   */
  deleteAttachment: async (expenseId, attachmentId) => {
    const response = await axiosClient.delete(
      `/expenses/${expenseId}/attachments/${attachmentId}`
    );
    return response.data;
  },
};

export default attachmentService;
