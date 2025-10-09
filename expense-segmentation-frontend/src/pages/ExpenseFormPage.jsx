import { useState } from 'react';
import { useAuth } from '../context/AuthContext';
import { useNavigate } from 'react-router-dom';
import expenseService from '../api/expenseService';
import attachmentService from '../api/attachmentService';
import PageLayout from '../components/Layout/PageLayout';
import PageContainer from '../components/Layout/PageContainer';
import Card from '../components/Layout/Card';
import Navbar from '../components/Navigation/Navbar';
import Alert from '../components/Feedback/Alert';
import Input from '../components/Input';
import Select from '../components/Select';
import Button from '../components/Button';
import FileUpload from '../components/FileUpload';

const ExpenseFormPage = () => {
  const { user } = useAuth();
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
  const [selectedFiles, setSelectedFiles] = useState([]);

  const handleInputChange = (e) => {
    const { name, value } = e.target;
    setFormData({
      ...formData,
      [name]: value,
    });
  };

  const handleFileSelect = (file) => {
    // Add file to the list if not already present
    if (!selectedFiles.some(f => f.name === file.name && f.size === file.size)) {
      setSelectedFiles([...selectedFiles, file]);
    }
  };

  const handleRemoveFile = (index) => {
    setSelectedFiles(selectedFiles.filter((_, i) => i !== index));
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

      // Upload attachments if files were selected
      if (selectedFiles.length > 0) {
        let uploadedCount = 0;
        let failedCount = 0;

        for (const file of selectedFiles) {
          try {
            await attachmentService.uploadAttachment(response.id, file);
            uploadedCount++;
          } catch (attachmentErr) {
            console.error('Failed to upload attachment:', attachmentErr);
            failedCount++;
          }
        }

        if (failedCount === 0) {
          setSuccessMessage(
            `${formData.type === 'EXPENSE' ? 'Expense' : 'Invoice'} created successfully with ${uploadedCount} attachment(s)! ID: ${response.id}`
          );
        } else if (uploadedCount > 0) {
          setSuccessMessage(
            `${formData.type === 'EXPENSE' ? 'Expense' : 'Invoice'} created successfully (ID: ${response.id}). ${uploadedCount} of ${selectedFiles.length} attachments uploaded. ${failedCount} failed - you can add them later from the expense details page.`
          );
        } else {
          setSuccessMessage(
            `${formData.type === 'EXPENSE' ? 'Expense' : 'Invoice'} created successfully (ID: ${response.id}), but all attachment uploads failed. You can add them later from the expense details page.`
          );
        }
      } else {
        setSuccessMessage(
          `${formData.type === 'EXPENSE' ? 'Expense' : 'Invoice'} created successfully! ID: ${response.id}`
        );
      }

      // Reset form
      setFormData({
        date: getTodayDate(),
        vendor: '',
        amount: '',
        description: '',
        type: 'EXPENSE',
      });
      setSelectedFiles([]);

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
    <PageLayout>
      <Navbar />
      <PageContainer className="max-w-3xl">
        <div className="mb-6">
          <h2 className="text-2xl font-bold text-gray-900">
            Submit Expense or Invoice
          </h2>
          <p className="text-gray-600 mt-1">
            Enter the details of your expense or invoice for processing
          </p>
        </div>

        {successMessage && (
          <Alert
            type="success"
            message={successMessage}
            onClose={() => setSuccessMessage(null)}
          />
        )}
        {error && (
          <Alert
            type="error"
            message={error}
            onClose={() => setError(null)}
          />
        )}

        <Card>
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

              <div className="mb-4">
                <label className="block text-sm font-medium text-gray-700 mb-2">
                  Attachments (Optional)
                </label>
                <FileUpload
                  onFileSelect={handleFileSelect}
                  maxSize={10485760}
                  accept=".pdf,.jpg,.jpeg,.png"
                />
                {selectedFiles.length > 0 && (
                  <div className="mt-3 space-y-2">
                    <p className="text-sm font-medium text-gray-700">
                      Selected files ({selectedFiles.length}):
                    </p>
                    {selectedFiles.map((file, index) => (
                      <div
                        key={index}
                        className="flex items-center justify-between p-2 bg-gray-50 rounded border border-gray-200"
                      >
                        <div className="flex items-center space-x-2">
                          <svg
                            className="w-4 h-4 text-gray-400"
                            fill="none"
                            stroke="currentColor"
                            viewBox="0 0 24 24"
                          >
                            <path
                              strokeLinecap="round"
                              strokeLinejoin="round"
                              strokeWidth={2}
                              d="M15.172 7l-6.586 6.586a2 2 0 102.828 2.828l6.414-6.586a4 4 0 00-5.656-5.656l-6.415 6.585a6 6 0 108.486 8.486L20.5 13"
                            />
                          </svg>
                          <span className="text-sm text-gray-700">
                            {file.name} ({(file.size / 1024 / 1024).toFixed(2)} MB)
                          </span>
                        </div>
                        <button
                          type="button"
                          onClick={() => handleRemoveFile(index)}
                          className="text-red-600 hover:text-red-800 text-sm"
                        >
                          Remove
                        </button>
                      </div>
                    ))}
                  </div>
                )}
                <p className="text-xs text-gray-500 mt-1">
                  You can add multiple files. Click to select another file.
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
        </Card>
      </PageContainer>
    </PageLayout>
  );
};

export default ExpenseFormPage;
