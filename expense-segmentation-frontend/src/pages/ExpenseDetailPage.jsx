import { useState, useEffect } from 'react';
import { useAuth } from '../context/AuthContext';
import { useNavigate, useParams } from 'react-router-dom';
import expenseService from '../api/expenseService';
import attachmentService from '../api/attachmentService';
import Button from '../components/Button';
import FileUpload from '../components/FileUpload';
import Modal from '../components/Modal';

const ExpenseDetailPage = () => {
  const { user, logout } = useAuth();
  const navigate = useNavigate();
  const { id } = useParams();
  const [expense, setExpense] = useState(null);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState(null);
  const [attachments, setAttachments] = useState([]);
  const [loadingAttachments, setLoadingAttachments] = useState(false);
  const [uploadingFile, setUploadingFile] = useState(false);
  const [showUploadModal, setShowUploadModal] = useState(false);
  const [selectedFile, setSelectedFile] = useState(null);
  const [uploadError, setUploadError] = useState(null);

  const handleLogout = () => {
    logout();
    navigate('/login');
  };

  useEffect(() => {
    const fetchExpense = async () => {
      setLoading(true);
      setError(null);

      try {
        const data = await expenseService.getExpenseById(id);
        setExpense(data);
        // Fetch attachments
        fetchAttachments();
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

  const fetchAttachments = async () => {
    setLoadingAttachments(true);
    try {
      const data = await attachmentService.getAttachments(id);
      setAttachments(data);
    } catch (err) {
      console.error('Failed to fetch attachments:', err);
    } finally {
      setLoadingAttachments(false);
    }
  };

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

  const formatDate = (dateString) => {
    return new Date(dateString).toLocaleDateString();
  };

  const formatDateTime = (dateTimeString) => {
    return new Date(dateTimeString).toLocaleString();
  };

  const formatAmount = (amount) => {
    return new Intl.NumberFormat('en-US', {
      style: 'currency',
      currency: 'USD',
    }).format(amount);
  };

  const getStatusBadgeColor = (status) => {
    switch (status) {
      case 'SUBMITTED':
        return 'bg-blue-100 text-blue-800';
      case 'APPROVED':
        return 'bg-green-100 text-green-800';
      case 'REJECTED':
        return 'bg-red-100 text-red-800';
      case 'PENDING':
        return 'bg-yellow-100 text-yellow-800';
      default:
        return 'bg-gray-100 text-gray-800';
    }
  };

  const getTypeBadgeColor = (type) => {
    return type === 'EXPENSE'
      ? 'bg-purple-100 text-purple-800'
      : 'bg-indigo-100 text-indigo-800';
  };

  return (
    <div className="min-h-screen bg-gray-100">
      {/* Navigation */}
      <nav className="bg-white shadow-sm">
        <div className="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8">
          <div className="flex justify-between h-16 items-center">
            <div className="flex-shrink-0">
              <h1 className="text-xl font-bold text-gray-900">
                Expense Segmentation App
              </h1>
            </div>
            <div className="flex items-center space-x-4">
              <button
                onClick={() => navigate('/dashboard')}
                className="text-gray-700 hover:text-gray-900 font-medium"
              >
                Dashboard
              </button>
              <button
                onClick={() => navigate('/expenses')}
                className="text-blue-600 hover:text-blue-800 font-medium"
              >
                Expenses
              </button>
              {(user?.role === 'ADMIN' || user?.role === 'MANAGER') && (
                <button
                  onClick={() => navigate('/users')}
                  className="text-gray-700 hover:text-gray-900 font-medium"
                >
                  Users
                </button>
              )}
              {(user?.role === 'ADMIN' || user?.role === 'FINANCE') && (
                <button
                  onClick={() => navigate('/departments')}
                  className="text-gray-700 hover:text-gray-900 font-medium"
                >
                  Departments
                </button>
              )}
              <span className="text-gray-700">
                Welcome, {user?.name || 'User'}
              </span>
              <button
                onClick={handleLogout}
                className="px-4 py-2 bg-red-600 text-white rounded-lg hover:bg-red-700 transition-colors"
              >
                Logout
              </button>
            </div>
          </div>
        </div>
      </nav>

      {/* Main Content */}
      <main className="max-w-4xl mx-auto py-6 sm:px-6 lg:px-8">
        <div className="px-4 py-6 sm:px-0">
          {/* Header */}
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

          {/* Error Message */}
          {error && (
            <div className="mb-4 p-4 bg-red-50 border border-red-200 rounded-lg">
              <p className="text-red-800">{error}</p>
            </div>
          )}

          {/* Loading State */}
          {loading ? (
            <div className="bg-white rounded-lg shadow p-6 flex justify-center items-center">
              <div className="w-12 h-12 border-4 border-blue-600 border-t-transparent rounded-full animate-spin"></div>
            </div>
          ) : expense ? (
            <div className="bg-white rounded-lg shadow overflow-hidden">
              {/* Header Section */}
              <div className="bg-gray-50 px-6 py-4 border-b border-gray-200">
                <div className="flex justify-between items-start">
                  <div>
                    <h3 className="text-lg font-semibold text-gray-900">
                      {expense.vendor}
                    </h3>
                    <p className="text-sm text-gray-500 mt-1">ID: {expense.id}</p>
                  </div>
                  <div className="flex space-x-2">
                    <span className={`px-3 py-1 text-sm font-semibold rounded-full ${getTypeBadgeColor(expense.type)}`}>
                      {expense.type}
                    </span>
                    <span className={`px-3 py-1 text-sm font-semibold rounded-full ${getStatusBadgeColor(expense.status)}`}>
                      {expense.status}
                    </span>
                  </div>
                </div>
              </div>

              {/* Details Section */}
              <div className="px-6 py-6">
                <dl className="grid grid-cols-1 gap-6 sm:grid-cols-2">
                  {/* Amount */}
                  <div>
                    <dt className="text-sm font-medium text-gray-500">Amount</dt>
                    <dd className="mt-1 text-2xl font-bold text-gray-900">
                      {formatAmount(expense.amount)}
                    </dd>
                  </div>

                  {/* Date */}
                  <div>
                    <dt className="text-sm font-medium text-gray-500">Date</dt>
                    <dd className="mt-1 text-lg text-gray-900">
                      {formatDate(expense.date)}
                    </dd>
                  </div>

                  {/* Vendor */}
                  <div>
                    <dt className="text-sm font-medium text-gray-500">Vendor</dt>
                    <dd className="mt-1 text-sm text-gray-900">
                      {expense.vendor}
                    </dd>
                  </div>

                  {/* Type */}
                  <div>
                    <dt className="text-sm font-medium text-gray-500">Type</dt>
                    <dd className="mt-1 text-sm text-gray-900">
                      {expense.type}
                    </dd>
                  </div>

                  {/* Status */}
                  <div>
                    <dt className="text-sm font-medium text-gray-500">Status</dt>
                    <dd className="mt-1 text-sm text-gray-900">
                      {expense.status}
                    </dd>
                  </div>

                  {/* Created By */}
                  <div>
                    <dt className="text-sm font-medium text-gray-500">Created By</dt>
                    <dd className="mt-1 text-sm text-gray-900">
                      {expense.createdByName} ({expense.createdByEmail})
                    </dd>
                  </div>

                  {/* Created At */}
                  <div>
                    <dt className="text-sm font-medium text-gray-500">Created At</dt>
                    <dd className="mt-1 text-sm text-gray-900">
                      {formatDateTime(expense.createdAt)}
                    </dd>
                  </div>

                  {/* Updated At */}
                  <div>
                    <dt className="text-sm font-medium text-gray-500">Updated At</dt>
                    <dd className="mt-1 text-sm text-gray-900">
                      {formatDateTime(expense.updatedAt)}
                    </dd>
                  </div>

                  {/* Description - Full Width */}
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

              {/* Attachments Section */}
              <div className="bg-gray-50 px-6 py-4 border-t border-gray-200">
                <div className="flex justify-between items-center mb-4">
                  <h4 className="text-sm font-medium text-gray-700">Attachments</h4>
                  <Button
                    variant="primary"
                    size="sm"
                    onClick={() => setShowUploadModal(true)}
                  >
                    Upload File
                  </Button>
                </div>

                {loadingAttachments ? (
                  <div className="flex justify-center py-4">
                    <div className="w-8 h-8 border-4 border-blue-600 border-t-transparent rounded-full animate-spin"></div>
                  </div>
                ) : attachments.length > 0 ? (
                  <div className="space-y-2">
                    {attachments.map((attachment) => (
                      <div
                        key={attachment.id}
                        className="flex items-center justify-between bg-white p-3 rounded-lg border border-gray-200"
                      >
                        <div className="flex items-center space-x-3 flex-1">
                          {/* File icon based on type */}
                          {attachment.mimeType.startsWith('image/') ? (
                            <svg className="w-8 h-8 text-blue-500" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                              <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M4 16l4.586-4.586a2 2 0 012.828 0L16 16m-2-2l1.586-1.586a2 2 0 012.828 0L20 14m-6-6h.01M6 20h12a2 2 0 002-2V6a2 2 0 00-2-2H6a2 2 0 00-2 2v12a2 2 0 002 2z" />
                            </svg>
                          ) : (
                            <svg className="w-8 h-8 text-red-500" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                              <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M7 21h10a2 2 0 002-2V9.414a1 1 0 00-.293-.707l-5.414-5.414A1 1 0 0012.586 3H7a2 2 0 00-2 2v14a2 2 0 002 2z" />
                            </svg>
                          )}
                          <div className="flex-1 min-w-0">
                            <p className="text-sm font-medium text-gray-900 truncate">
                              {attachment.originalFilename}
                            </p>
                            <p className="text-xs text-gray-500">
                              {formatFileSize(attachment.fileSize)} • Uploaded by {attachment.uploadedByName} on {new Date(attachment.uploadedAt).toLocaleDateString()}
                            </p>
                          </div>
                        </div>
                        <div className="flex items-center space-x-2">
                          <button
                            onClick={() => handleDownload(attachment.id, attachment.originalFilename)}
                            className="p-2 text-blue-600 hover:text-blue-800 hover:bg-blue-50 rounded transition-colors"
                            title="Download"
                          >
                            <svg className="w-5 h-5" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                              <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M4 16v1a3 3 0 003 3h10a3 3 0 003-3v-1m-4-4l-4 4m0 0l-4-4m4 4V4" />
                            </svg>
                          </button>
                          <button
                            onClick={() => handleDelete(attachment.id)}
                            className="p-2 text-red-600 hover:text-red-800 hover:bg-red-50 rounded transition-colors"
                            title="Delete"
                          >
                            <svg className="w-5 h-5" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                              <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M19 7l-.867 12.142A2 2 0 0116.138 21H7.862a2 2 0 01-1.995-1.858L5 7m5 4v6m4-6v6m1-10V4a1 1 0 00-1-1h-4a1 1 0 00-1 1v3M4 7h16" />
                            </svg>
                          </button>
                        </div>
                      </div>
                    ))}
                  </div>
                ) : (
                  <p className="text-sm text-gray-500 italic">No attachments yet</p>
                )}
              </div>

              {/* Actions */}
              <div className="bg-white px-6 py-4 border-t border-gray-200 flex justify-end space-x-3">
                <Button
                  variant="secondary"
                  onClick={() => navigate('/expenses')}
                >
                  Back to List
                </Button>
              </div>
            </div>
          ) : null}
        </div>
      </main>

      {/* Upload Modal */}
      {showUploadModal && (
        <Modal
          isOpen={showUploadModal}
          onClose={() => {
            setShowUploadModal(false);
            setSelectedFile(null);
            setUploadError(null);
          }}
          title="Upload Attachment"
        >
          <div className="space-y-4">
            <FileUpload onFileSelect={handleFileSelect} />

            {selectedFile && (
              <div className="bg-blue-50 border border-blue-200 rounded-lg p-3">
                <p className="text-sm text-gray-700">
                  <span className="font-medium">Selected file:</span> {selectedFile.name}
                </p>
                <p className="text-xs text-gray-500 mt-1">
                  Size: {formatFileSize(selectedFile.size)}
                </p>
              </div>
            )}

            {uploadError && (
              <div className="bg-red-50 border border-red-200 rounded-lg p-3">
                <p className="text-sm text-red-800">{uploadError}</p>
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
                variant="primary"
                onClick={handleUpload}
                disabled={!selectedFile || uploadingFile}
              >
                {uploadingFile ? 'Uploading...' : 'Upload'}
              </Button>
            </div>
          </div>
        </Modal>
      )}
    </div>
  );
};

export default ExpenseDetailPage;
