import { useState, useEffect, useCallback } from 'react';
import { useAuth } from '../context/AuthContext';
import { useNavigate, useParams } from 'react-router-dom';
import expenseService from '../api/expenseService';
import attachmentService from '../api/attachmentService';
import { expenseSegmentService } from '../api/expenseSegmentService';
import { categoryService } from '../api/categoryService';
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
import SegmentForm from '../components/SegmentForm';
import SegmentDisplay from '../components/SegmentDisplay';
import HelpTooltip from '../components/HelpTooltip';
import { useExpenseBadges } from '../hooks/useExpenseBadges';
import { useFormatters } from '../hooks/useFormatters';
import usePermissions from '../hooks/usePermissions';

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
  const [segmentForms, setSegmentForms] = useState([{
    id: Date.now(),
    category: '',
    amount: '',
    percentage: ''
  }]);
  const [segmentFormError, setSegmentFormError] = useState(null);
  const [savingSegment, setSavingSegment] = useState(false);
  
  // Edit segment state
  const [editingSegmentId, setEditingSegmentId] = useState(null);
  const [editSegmentForm, setEditSegmentForm] = useState({});
  const [savingEditSegment, setSavingEditSegment] = useState(false);
  const [editSegmentError, setEditSegmentError] = useState(null);
  
  // Categories for dropdown
  const [categories, setCategories] = useState([]);
  const [loadingCategories, setLoadingCategories] = useState(false);

  const { getStatusVariant, getTypeVariant } = useExpenseBadges();
  const { formatCurrency, formatDate, formatDateTime } = useFormatters();
  const { canModifySegments, canUploadAttachments, canDeleteAttachments } = usePermissions();

  const fetchAttachments = useCallback(async () => {
    setLoadingAttachments(true);
    try {
      const attachmentData = await attachmentService.getAttachments(id);
      setAttachments(attachmentData);
    } catch (err) {
      console.error('Failed to fetch attachments:', err);
    } finally {
      setLoadingAttachments(false);
    }
  }, [id]);

  const fetchSegments = useCallback(async () => {
    setLoadingSegments(true);
    try {
      const segmentData = await expenseSegmentService.getExpenseSegments(id);
      setSegments(segmentData);
    } catch (err) {
      console.error('Failed to fetch segments:', err);
    } finally {
      setLoadingSegments(false);
    }
  }, [id]);

  const fetchCategories = useCallback(async () => {
    setLoadingCategories(true);
    try {
      const categoryData = await categoryService.getAllCategories();
      setCategories(categoryData.map(cat => cat.name));
    } catch (err) {
      console.error('Failed to fetch categories:', err);
      // Fallback to hardcoded categories if API fails
      setCategories(['Travel', 'Meals', 'Supplies', 'Entertainment', 'Office', 'Training', 'Other']);
    } finally {
      setLoadingCategories(false);
    }
  }, []);

  useEffect(() => {
    const fetchExpense = async () => {
      setLoading(true);
      setError(null);

      try {
        const data = await expenseService.getExpenseById(id);
        setExpense(data);
        // Fetch attachments, segments, and categories
        fetchAttachments();
        fetchSegments();
        fetchCategories();
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
  }, [id, fetchAttachments, fetchSegments, fetchCategories]);

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

  const handleDeleteAttachment = async (attachmentId) => {
    if (!confirm('Are you sure you want to delete this attachment?')) return;

    try {
      await attachmentService.deleteAttachment(attachmentId);
      fetchAttachments(); // Refresh attachments list
    } catch (err) {
      console.error('Failed to delete attachment:', err);
      setError(err.response?.data?.message || 'Failed to delete attachment');
    }
  };

  const formatFileSize = (bytes) => {
    if (bytes < 1024) return bytes + ' B';
    if (bytes < 1024 * 1024) return (bytes / 1024).toFixed(1) + ' KB';
    return (bytes / (1024 * 1024)).toFixed(1) + ' MB';
  };

  const handleSegmentInputChange = (index, e) => {
    const { name, value } = e.target;
    const updatedForms = [...segmentForms];
    updatedForms[index] = {
      ...updatedForms[index],
      [name]: value
    };

    // Auto-calculate percentage when amount changes
    if (name === 'amount' && expense && value) {
      const amount = parseFloat(value);
      if (amount > 0) {
        const percentage = ((amount / expense.amount) * 100).toFixed(2);
        updatedForms[index].percentage = percentage;
      } else {
        updatedForms[index].percentage = '';
      }
    }

    setSegmentForms(updatedForms);
    setSegmentFormError(null);
  };

  const addSegmentRow = () => {
    setSegmentForms([...segmentForms, {
      id: Date.now(),
      category: '',
      amount: '',
      percentage: ''
    }]);
  };

  const removeSegmentRow = (index) => {
    if (segmentForms.length > 1) {
      const updatedForms = segmentForms.filter((_, i) => i !== index);
      setSegmentForms(updatedForms);
      setSegmentFormError(null);
    }
  };

  const validateSegmentForms = () => {
    // Check if all categories are filled
    for (let i = 0; i < segmentForms.length; i++) {
      const form = segmentForms[i];
      if (!form.category.trim()) {
        setSegmentFormError(`Category is required for segment ${i + 1}`);
        return false;
      }
      if (!form.amount || parseFloat(form.amount) <= 0) {
        setSegmentFormError(`Amount must be a positive number for segment ${i + 1}`);
        return false;
      }
      if (parseFloat(form.amount) > expense.amount) {
        setSegmentFormError(`Amount cannot exceed total expense amount (${formatCurrency(expense.amount)}) for segment ${i + 1}`);
        return false;
      }
      // Validate percentage if manually entered
      if (form.percentage && (parseFloat(form.percentage) < 0 || parseFloat(form.percentage) > 100)) {
        setSegmentFormError(`Percentage must be between 0 and 100 for segment ${i + 1}`);
        return false;
      }
    }

    // Check for duplicate categories
    const categories = segmentForms.map(form => form.category.trim().toLowerCase()).filter(cat => cat);
    const uniqueCategories = new Set(categories);
    if (categories.length !== uniqueCategories.size) {
      setSegmentFormError('Segment categories must be unique');
      return false;
    }

    // Check total amount equals expense amount
    const totalAmount = segmentForms.reduce((sum, form) => sum + (parseFloat(form.amount) || 0), 0);
    const difference = Math.abs(totalAmount - expense.amount);
    if (difference > 0.01) {
      setSegmentFormError(`Total segments amount (${formatCurrency(totalAmount)}) must equal expense amount (${formatCurrency(expense.amount)})`);
      return false;
    }

    // Check total percentage equals 100%
    const totalPercentage = segmentForms.reduce((sum, form) => sum + (parseFloat(form.percentage) || 0), 0);
    if (Math.abs(totalPercentage - 100) > 0.01) {
      setSegmentFormError(`Total percentage (${totalPercentage.toFixed(2)}%) must equal 100%`);
      return false;
    }

    return true;
  };

  const handleSaveSegments = async () => {
    if (!validateSegmentForms()) return;

    setSavingSegment(true);
    setSegmentFormError(null);

    try {
      // Prepare segments data
      const segmentsData = {
        segments: segmentForms.map(form => ({
          category: form.category.trim(),
          amount: parseFloat(form.amount),
          percentage: parseFloat(form.percentage) || null
        }))
      };

      // Use batch API for multiple segments
      await expenseSegmentService.replaceAllExpenseSegments(id, segmentsData);
      
      // Reset form and refresh segments
      setSegmentForms([{
        id: Date.now(),
        category: '',
        amount: '',
        percentage: ''
      }]);
      setShowAddSegment(false);
      fetchSegments();
    } catch (err) {
      console.error('Failed to save segments:', err);
      setSegmentFormError(err.response?.data?.message || 'Failed to save segments');
    } finally {
      setSavingSegment(false);
    }
  };

  const handleCancelAddSegment = () => {
    setSegmentForms([{
      id: Date.now(),
      category: '',
      amount: '',
      percentage: ''
    }]);
    setSegmentFormError(null);
    setShowAddSegment(false);
  };

  // Edit segment functions
  const handleEditSegment = (segmentId) => {
    const segment = segments.find(s => s.id === segmentId);
    if (segment) {
      setEditingSegmentId(segmentId);
      setEditSegmentForm({
        category: segment.category,
        amount: segment.amount.toString()
      });
      setEditSegmentError(null);
    }
  };

  const handleEditSegmentChange = (segmentId, e) => {
    const { name, value } = e.target;
    setEditSegmentForm(prev => ({
      ...prev,
      [name]: value
    }));
    setEditSegmentError(null);
  };

  const handleSaveEditSegment = async (segmentId, formData) => {
    if (!formData.category.trim() || !formData.amount || parseFloat(formData.amount) <= 0) {
      setEditSegmentError('Category and amount are required');
      return;
    }

    if (parseFloat(formData.amount) > expense.amount) {
      setEditSegmentError(`Amount cannot exceed total expense amount (${formatCurrency(expense.amount)})`);
      return;
    }

    setSavingEditSegment(true);
    setEditSegmentError(null);

    try {
      await expenseSegmentService.updateExpenseSegment(id, segmentId, {
        category: formData.category.trim(),
        amount: parseFloat(formData.amount)
      });
      
      setEditingSegmentId(null);
      setEditSegmentForm({});
      fetchSegments();
    } catch (err) {
      console.error('Failed to update segment:', err);
      setEditSegmentError(err.response?.data?.message || 'Failed to update segment');
    } finally {
      setSavingEditSegment(false);
    }
  };

  const handleCancelEditSegment = () => {
    setEditingSegmentId(null);
    setEditSegmentForm({});
    setEditSegmentError(null);
  };

  const handleDeleteSegment = async (segmentId) => {
    if (!confirm('Are you sure you want to delete this segment? At least one segment must remain.')) {
      return;
    }

    try {
      await expenseSegmentService.deleteExpenseSegment(id, segmentId);
      fetchSegments();
    } catch (err) {
      console.error('Failed to delete segment:', err);
      setError(err.response?.data?.message || 'Failed to delete segment');
    }
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
                <div className="flex items-center">
                  <h4 className="text-sm font-medium text-gray-900">Expense Segments</h4>
                  <HelpTooltip type="segment" />
                </div>
                {!showAddSegment && canModifySegments(expense) && (
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
                    {segments.length === 0 ? 'Add New Segments' : 'Replace All Segments'}
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
                  
                  <div className="space-y-3 mb-4">
                     {segmentForms.map((form, index) => (
                       <SegmentForm
                         key={form.id}
                         segment={form}
                         index={index}
                         isLast={index === segmentForms.length - 1}
                         canRemove={segmentForms.length > 1}
                         onInputChange={handleSegmentInputChange}
                         onAddRow={addSegmentRow}
                         onRemoveRow={removeSegmentRow}
                         expenseAmount={expense.amount}
                         categories={categories}
                       />
                     ))}
                  </div>
                  
                  <div className="text-sm text-gray-600 mb-3 space-y-1">
                    <div>Total segments amount: {formatCurrency(segmentForms.reduce((sum, form) => sum + (parseFloat(form.amount) || 0), 0))}</div>
                    <div>Expense amount: {formatCurrency(expense.amount)}</div>
                    <div>Remaining: {formatCurrency(expense.amount - segmentForms.reduce((sum, form) => sum + (parseFloat(form.amount) || 0), 0))}</div>
                    <div>Total percentage: {segmentForms.reduce((sum, form) => sum + (parseFloat(form.percentage) || 0), 0).toFixed(2)}%</div>
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
                      onClick={handleSaveSegments}
                      isLoading={savingSegment}
                      disabled={segmentForms.some(form => !form.category || !form.amount)}
                    >
                      Save Segments
                    </Button>
                  </div>
                </div>
               ) : segments.length === 0 ? (
                <div className="text-center py-8">
                  <p className="text-sm text-gray-400 italic mb-4">No segments available</p>
                  <p className="text-xs text-gray-500">Click &quot;Add Segment&quot; to start categorizing this expense</p>
                </div>
              ) : (
                <div className="space-y-3">
                  {editSegmentError && (
                    <Alert
                      type="error"
                      message={editSegmentError}
                      onClose={() => setEditSegmentError(null)}
                    />
                  )}
                  
                  {segments.map((segment) => (
                    <SegmentDisplay
                      key={segment.id}
                      segment={segment}
                      isEditing={editingSegmentId === segment.id}
                      onEdit={handleEditSegment}
                      onSave={handleSaveEditSegment}
                      onCancel={handleCancelEditSegment}
                      onDelete={handleDeleteSegment}
                      onInputChange={handleEditSegmentChange}
                      expenseAmount={expense.amount}
                      categories={categories}
                      canModify={canModifySegments(expense)}
                    />
                  ))}
                </div>
              )}
            </div>

            <div className="bg-gray-50 px-6 py-4 border-t border-gray-200">
              <div className="flex justify-between items-center mb-4">
                <h4 className="text-sm font-medium text-gray-900">Attachments</h4>
                {canUploadAttachments(expense) && (
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
                         {canDeleteAttachments(attachment) && (
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
