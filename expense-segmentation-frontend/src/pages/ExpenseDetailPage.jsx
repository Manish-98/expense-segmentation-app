import { useState, useEffect } from 'react';
import { useAuth } from '../context/AuthContext';
import { useNavigate, useParams } from 'react-router-dom';
import expenseService from '../api/expenseService';
import attachmentService from '../api/attachmentService';
import { expenseSegmentService } from '../api/expenseSegmentService';
import PageLayout from '../components/Layout/PageLayout';
import PageContainer from '../components/Layout/PageContainer';
import Card from '../components/Layout/Card';
import Navbar from '../components/Navigation/Navbar';
import LoadingSpinner from '../components/Feedback/LoadingSpinner';
import Alert from '../components/Feedback/Alert';
import Badge from '../components/Feedback/Badge';
import Button from '../components/Button';
import FileUpload from '../components/FileUpload';
import Modal from '../components/Modal';
import Input from '../components/Input';
import Select from '../components/Select';
import { useExpenseBadges } from '../hooks/useExpenseBadges';
import { useFormatters } from '../hooks/useFormatters';

const ExpenseDetailPage = () => {
  const { user } = useAuth();
  const navigate = useNavigate();
  const { id } = useParams();
  const [expense, setExpense] = useState(null);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState(null);
  const [attachments, setAttachments] = useState([]);
  const [loadingAttachments, setLoadingAttachments] = useState(false);
  const [segments, setSegments] = useState([]);
  const [loadingSegments, setLoadingSegments] = useState(false);
  const [showUploadModal, setShowUploadModal] = useState(false);
  const [selectedFile, setSelectedFile] = useState(null);
  const [uploadingFile, setUploadingFile] = useState(false);
  const [uploadError, setUploadError] = useState(null);
  
  // Segment management state
  const [showAddSegment, setShowAddSegment] = useState(false);
  const [segmentForm, setSegmentForm] = useState({
    category: '',
    amount: ''
  });
  const [segmentFormError, setSegmentFormError] = useState(null);
  const [savingSegment, setSavingSegment] = useState(false);

  const { getStatusVariant, getTypeVariant } = useExpenseBadges();
  const { formatCurrency, formatDate, formatDateTime } = useFormatters();

  useEffect(() => {
    const fetchExpense = async () => {
      setLoading(true);
      setError(null);

      try {
        const data = await expenseService.getExpenseById(id);
        setExpense(data);
        // Fetch attachments and segments
        const fetchAttachments = async () => {
          setLoadingAttachments(true);
          try {
            const attachmentData = await attachmentService.getAttachments(id);
            setAttachments(attachmentData);
          } catch (err) {
            console.error('Failed to fetch attachments:', err);
          } finally {
            setLoadingAttachments(false);
          }
        };

        fetchAttachments();
        fetchSegments();
      } catch (err) {
        console.error('Failed to fetch expense:', err);
        setError(err.response?.data?.message || 'Failed to load expense details');
      } finally {
        setLoading(false);
      }
    };

    if (id) {
      fetchExpense();
    }
  }, [id]);



  const handleFileSelect = (file) => {
    setSelectedFile(file);
    setUploadError(null);
  };

  const handleUpload = async () => {
    if (!selectedFile) return;

    setUploadingFile(true);
    setUploadError(null);

    try {
      await attachmentService.uploadAttachment(id, selectedFile);
      setShowUploadModal(false);
      setSelectedFile(null);
      fetchAttachments(); // Refresh attachments list
    } catch (err) {
      console.error('Failed to upload file:', err);
      setUploadError(err.response?.data?.message || 'Failed to upload file');
    } finally {
      setUploadingFile(false);
    }
  };

  const handleDownload = async (attachmentId, filename) => {
    try {
      const response = await attachmentService.downloadAttachment(id, attachmentId);

      // Create blob and download
      const url = window.URL.createObjectURL(new Blob([response.data]));
      const link = document.createElement('a');
      link.href = url;
      link.setAttribute('download', filename);
      document.body.appendChild(link);
      link.click();
      link.remove();
      window.URL.revokeObjectURL(url);
    } catch (err) {
      console.error('Failed to download file:', err);
      alert('Failed to download file');
    }
  };

  const handleDelete = async (attachmentId) => {
    if (!window.confirm('Are you sure you want to delete this attachment?')) {
      return;
    }

    try {
      await attachmentService.deleteAttachment(id, attachmentId);
      fetchAttachments(); // Refresh attachments list
    } catch (err) {
      console.error('Failed to delete attachment:', err);
      alert(err.response?.data?.message || 'Failed to delete attachment');
    }
  };

  const formatFileSize = (bytes) => {
    if (bytes < 1024) return bytes + ' B';
    if (bytes < 1024 * 1024) return (bytes / 1024).toFixed(1) + ' KB';
    return (bytes / (1024 * 1024)).toFixed(1) + ' MB';
  };

  const fetchSegments = async () => {
    setLoadingSegments(true);
    try {
      const segmentData = await expenseSegmentService.getExpenseSegments(id);
      setSegments(segmentData);
    } catch (err) {
      console.error('Failed to fetch segments:', err);
    } finally {
      setLoadingSegments(false);
    }
  };

  const handleSegmentInputChange = (e) => {
    const { name, value } = e.target;
    setSegmentForm({
      ...segmentForm,
      [name]: value
    });
    setSegmentFormError(null);
  };

  const validateSegmentForm = () => {
    if (!segmentForm.category.trim()) {
      setSegmentFormError('Category is required');
      return false;
    }
    if (!segmentForm.amount || parseFloat(segmentForm.amount) <= 0) {
      setSegmentFormError('Amount must be a positive number');
      return false;
    }
    if (parseFloat(segmentForm.amount) > expense.amount) {
      setSegmentFormError(`Amount cannot exceed total expense amount (${formatCurrency(expense.amount)})`);
      return false;
    }
    return true;
  };

  const handleSaveSegment = async () => {
    if (!validateSegmentForm()) return;

    setSavingSegment(true);
    setSegmentFormError(null);

    try {
      if (segments.length === 0) {
        // Add single segment to expense with no segments
        const segmentData = {
          category: segmentForm.category.trim(),
          amount: parseFloat(segmentForm.amount)
        };

        await expenseSegmentService.createExpenseSegment(id, segmentData);
      } else {
        // Replace all existing segments with new single segment
        const segmentsData = {
          segments: [{
            category: segmentForm.category.trim(),
            amount: parseFloat(segmentForm.amount)
          }]
        };

        await expenseSegmentService.replaceAllExpenseSegments(id, segmentsData);
      }
      
      // Reset form and refresh segments
      setSegmentForm({ category: '', amount: '' });
      setShowAddSegment(false);
      fetchSegments();
    } catch (err) {
      console.error('Failed to save segment:', err);
      setSegmentFormError(err.response?.data?.message || 'Failed to save segment');
    } finally {
      setSavingSegment(false);
    }
  };

  const handleCancelAddSegment = () => {
    setSegmentForm({ category: '', amount: '' });
    setSegmentFormError(null);
    setShowAddSegment(false);
  };

  if (loading) {
    return (
      <PageLayout>
        <Navbar />
        <LoadingSpinner fullScreen />
      </PageLayout>
    );
  }

  return (
    <PageLayout>
      <Navbar />
      <PageContainer className="max-w-4xl">
        <div className="mb-6 flex items-center">
          <button
            onClick={() => navigate('/expenses')}
            className="mr-4 text-gray-600 hover:text-gray-900"
          >
            ← Back to Expenses
          </button>
          <h2 className="text-2xl font-bold text-gray-900">
            Expense Details
          </h2>
        </div>

        {error && (
          <Alert
            type="error"
            message={error}
            onClose={() => setError(null)}
          />
        )}

        {expense ? (
          <Card className="overflow-hidden">
            <div className="bg-gray-50 px-6 py-4 border-b border-gray-200">
              <div className="flex justify-between items-start">
                <div>
                  <h3 className="text-lg font-semibold text-gray-900">
                    {expense.vendor}
                  </h3>
                  <p className="text-sm text-gray-500 mt-1">ID: {expense.id}</p>
                </div>
                <div className="flex space-x-2">
                  <Badge variant={getTypeVariant(expense.type)} size="md">
                    {expense.type}
                  </Badge>
                  <Badge variant={getStatusVariant(expense.status)} size="md">
                    {expense.status}
                  </Badge>
                </div>
              </div>
            </div>

            <div className="px-6 py-6">
              <dl className="grid grid-cols-1 gap-6 sm:grid-cols-2">
                <div>
                  <dt className="text-sm font-medium text-gray-500">Amount</dt>
                  <dd className="mt-1 text-2xl font-bold text-gray-900">
                    {formatCurrency(expense.amount)}
                  </dd>
                </div>

                <div>
                  <dt className="text-sm font-medium text-gray-500">Date</dt>
                  <dd className="mt-1 text-lg text-gray-900">
                    {formatDate(expense.date)}
                  </dd>
                </div>

                <div>
                  <dt className="text-sm font-medium text-gray-500">Vendor</dt>
                  <dd className="mt-1 text-sm text-gray-900">
                    {expense.vendor}
                  </dd>
                </div>

                <div>
                  <dt className="text-sm font-medium text-gray-500">Type</dt>
                  <dd className="mt-1 text-sm text-gray-900">
                    {expense.type}
                  </dd>
                </div>

                <div>
                  <dt className="text-sm font-medium text-gray-500">Status</dt>
                  <dd className="mt-1 text-sm text-gray-900">
                    {expense.status}
                  </dd>
                </div>

                <div>
                  <dt className="text-sm font-medium text-gray-500">Created By</dt>
                  <dd className="mt-1 text-sm text-gray-900">
                    {expense.createdByName} ({expense.createdByEmail})
                  </dd>
                </div>

                <div>
                  <dt className="text-sm font-medium text-gray-500">Created At</dt>
                  <dd className="mt-1 text-sm text-gray-900">
                    {formatDateTime(expense.createdAt)}
                  </dd>
                </div>

                <div>
                  <dt className="text-sm font-medium text-gray-500">Updated At</dt>
                  <dd className="mt-1 text-sm text-gray-900">
                    {formatDateTime(expense.updatedAt)}
                  </dd>
                </div>

                {expense.description && (
                  <div className="sm:col-span-2">
                    <dt className="text-sm font-medium text-gray-500">Description</dt>
                    <dd className="mt-1 text-sm text-gray-900 whitespace-pre-wrap">
                      {expense.description}
                    </dd>
                  </div>
                )}
              </dl>
            </div>

            <div className="bg-gray-50 px-6 py-4 border-t border-gray-200">
              <div className="flex justify-between items-center mb-4">
                <h4 className="text-sm font-medium text-gray-900">Expense Segments</h4>
                {!showAddSegment && (
                  <Button
                    size="sm"
                    onClick={() => setShowAddSegment(true)}
                  >
                    {segments.length === 0 ? 'Add Segment' : 'Manage Segments'}
                  </Button>
                )}
              </div>
              
              {loadingSegments ? (
                <div className="flex justify-center py-4">
                  <LoadingSpinner size="sm" />
                </div>
              ) : showAddSegment ? (
                <div className="bg-white p-4 rounded-lg border border-gray-200">
                  <h5 className="text-sm font-medium text-gray-900 mb-3">
                    {segments.length === 0 ? 'Add New Segment' : 'Replace All Segments'}
                  </h5>
                  {segments.length > 0 && (
                    <p className="text-xs text-gray-500 mb-3">
                      This will replace all existing segments with new ones
                    </p>
                  )}
                  
                  {segmentFormError && (
                    <Alert
                      type="error"
                      message={segmentFormError}
                      onClose={() => setSegmentFormError(null)}
                      className="mb-3"
                    />
                  )}
                  
                  <div className="grid grid-cols-1 md:grid-cols-2 gap-4 mb-3">
                    <Select
                      label="Category"
                      name="category"
                      value={segmentForm.category}
                      onChange={handleSegmentInputChange}
                      error={segmentFormError && !segmentForm.category ? 'Category is required' : ''}
                      options={[
                        { value: 'Travel', label: 'Travel' },
                        { value: 'Meals', label: 'Meals' },
                        { value: 'Supplies', label: 'Supplies' },
                        { value: 'Entertainment', label: 'Entertainment' },
                        { value: 'Office', label: 'Office' },
                        { value: 'Training', label: 'Training' },
                        { value: 'Other', label: 'Other' }
                      ]}
                      required
                    />
                    
                    <Input
                      label="Amount"
                      type="number"
                      name="amount"
                      value={segmentForm.amount}
                      onChange={handleSegmentInputChange}
                      error={segmentFormError && (!segmentForm.amount || parseFloat(segmentForm.amount) <= 0) ? 'Amount must be positive' : ''}
                      placeholder="0.00"
                      min="0.01"
                      max={expense.amount}
                      step="0.01"
                      required
                    />
                  </div>
                  
                  <div className="text-sm text-gray-600 mb-3">
                    Remaining amount: {formatCurrency(expense.amount - (parseFloat(segmentForm.amount) || 0))}
                  </div>
                  
                  <div className="flex justify-end space-x-2">
                    <Button
                      variant="secondary"
                      size="sm"
                      onClick={handleCancelAddSegment}
                      disabled={savingSegment}
                    >
                      Cancel
                    </Button>
                    <Button
                      size="sm"
                      onClick={handleSaveSegment}
                      isLoading={savingSegment}
                      disabled={!segmentForm.category || !segmentForm.amount}
                    >
                      Save Segment
                    </Button>
                  </div>
                </div>
              ) : segments.length === 0 ? (
                <div className="text-center py-8">
                  <p className="text-sm text-gray-400 italic mb-4">No segments available</p>
                  <p className="text-xs text-gray-500">Click "Add Segment" to start categorizing this expense</p>
                </div>
              ) : (
                <div className="overflow-hidden shadow ring-1 ring-black ring-opacity-5 md:rounded-lg">
                  <table className="min-w-full divide-y divide-gray-300">
                    <thead className="bg-gray-100">
                      <tr>
                        <th scope="col" className="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">
                          Category
                        </th>
                        <th scope="col" className="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">
                          Amount
                        </th>
                        <th scope="col" className="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">
                          Percentage
                        </th>
                      </tr>
                    </thead>
                    <tbody className="bg-white divide-y divide-gray-200">
                      {segments.map((segment) => (
                        <tr key={segment.id}>
                          <td className="px-6 py-4 whitespace-nowrap text-sm font-medium text-gray-900">
                            {segment.category}
                          </td>
                          <td className="px-6 py-4 whitespace-nowrap text-sm text-gray-900">
                            {formatCurrency(segment.amount)}
                          </td>
                          <td className="px-6 py-4 whitespace-nowrap text-sm text-gray-900">
                            {segment.percentage}%
                          </td>
                        </tr>
                      ))}
                    </tbody>
                  </table>
                </div>
              )}
            </div>

            <div className="bg-gray-50 px-6 py-4 border-t border-gray-200">
              <div className="flex justify-between items-center mb-4">
                <h4 className="text-sm font-medium text-gray-900">Attachments</h4>
                {(user.id === expense.createdById || ['FINANCE', 'ADMIN'].includes(user.role)) && (
                  <Button
                    size="sm"
                    onClick={() => setShowUploadModal(true)}
                  >
                    Upload Attachment
                  </Button>
                )}
              </div>

              {loadingAttachments ? (
                <div className="flex justify-center py-4">
                  <LoadingSpinner size="sm" />
                </div>
              ) : attachments.length === 0 ? (
                <p className="text-sm text-gray-400 italic">No attachments yet</p>
              ) : (
                <div className="space-y-2">
                  {attachments.map((attachment) => (
                    <div
                      key={attachment.id}
                      className="flex items-center justify-between p-3 bg-white rounded border border-gray-200"
                    >
                      <div className="flex items-center space-x-3">
                        <svg
                          className="w-5 h-5 text-gray-400"
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
                        <div>
                          <p className="text-sm font-medium text-gray-900">
                            {attachment.originalFilename}
                          </p>
                          <p className="text-xs text-gray-500">
                            {formatFileSize(attachment.fileSize)} • Uploaded by{' '}
                            {attachment.uploadedByName}
                          </p>
                        </div>
                      </div>
                      <div className="flex space-x-2">
                        <button
                          onClick={() =>
                            handleDownload(attachment.id, attachment.originalFilename)
                          }
                          className="text-blue-600 hover:text-blue-800 text-sm"
                        >
                          Download
                        </button>
                        {(user.id === attachment.uploadedBy ||
                          ['FINANCE', 'ADMIN'].includes(user.role)) && (
                          <button
                            onClick={() => handleDelete(attachment.id)}
                            className="text-red-600 hover:text-red-800 text-sm"
                          >
                            Delete
                          </button>
                        )}
                      </div>
                    </div>
                  ))}
                </div>
              )}
            </div>

            <div className="bg-white px-6 py-4 border-t border-gray-200 flex justify-end space-x-3">
              <Button
                variant="secondary"
                onClick={() => navigate('/expenses')}
              >
                Back to List
              </Button>
            </div>
          </Card>
        ) : null}
      </PageContainer>

      {/* Upload Modal */}
      <Modal
        isOpen={showUploadModal}
        title="Upload Attachment"
        onClose={() => {
          setShowUploadModal(false);
          setSelectedFile(null);
          setUploadError(null);
        }}
      >
          <div className="space-y-4">
            {uploadError && (
              <Alert
                type="error"
                message={uploadError}
                onClose={() => setUploadError(null)}
              />
            )}

            <FileUpload
              onFileSelect={handleFileSelect}
              maxSize={10485760}
              accept=".pdf,.jpg,.jpeg,.png"
            />

            {selectedFile && (
              <div className="text-sm text-gray-600">
                Selected: {selectedFile.name} ({formatFileSize(selectedFile.size)})
              </div>
            )}

            <div className="flex justify-end space-x-3">
              <Button
                variant="secondary"
                onClick={() => {
                  setShowUploadModal(false);
                  setSelectedFile(null);
                  setUploadError(null);
                }}
                disabled={uploadingFile}
              >
                Cancel
              </Button>
              <Button
                onClick={handleUpload}
                disabled={!selectedFile || uploadingFile}
                isLoading={uploadingFile}
              >
                Upload
              </Button>
            </div>
          </div>
      </Modal>
    </PageLayout>
  );
};

export default ExpenseDetailPage;
