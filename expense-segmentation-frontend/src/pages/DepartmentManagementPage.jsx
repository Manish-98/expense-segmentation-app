import { useState, useEffect } from 'react';
import { useAuth } from '../context/AuthContext';
import { useNavigate } from 'react-router-dom';
import departmentService from '../api/departmentService';
import Modal from '../components/Modal';
import Select from '../components/Select';
import Input from '../components/Input';
import Button from '../components/Button';

const DepartmentManagementPage = () => {
  const { user, logout } = useAuth();
  const navigate = useNavigate();
  const [departments, setDepartments] = useState([]);
  const [users, setUsers] = useState([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState(null);
  const [successMessage, setSuccessMessage] = useState(null);

  // Create modal state
  const [createModalOpen, setCreateModalOpen] = useState(false);
  const [createFormData, setCreateFormData] = useState({ name: '', code: '', managerId: '' });
  const [createLoading, setCreateLoading] = useState(false);
  const [createError, setCreateError] = useState(null);

  // Edit modal state
  const [editModalOpen, setEditModalOpen] = useState(false);
  const [editingDepartment, setEditingDepartment] = useState(null);
  const [editFormData, setEditFormData] = useState({ name: '', managerId: '' });
  const [editLoading, setEditLoading] = useState(false);
  const [editError, setEditError] = useState(null);

  const isAdmin = user?.role === 'ADMIN';
  const isFinance = user?.role === 'FINANCE';

  useEffect(() => {
    fetchData();
  }, []);

  const fetchData = async () => {
    setLoading(true);
    setError(null);
    try {
      // Fetch departments (both Admin and Finance can view)
      if (isAdmin || isFinance) {
        const depts = await departmentService.getAllDepartments();
        setDepartments(depts);

        // Fetch users if admin (for manager dropdown)
        if (isAdmin) {
          try {
            const usersData = await departmentService.getAllUsers();
            setUsers(usersData);
          } catch (err) {
            console.error('Failed to fetch users:', err);
          }
        }
      } else {
        setError('You do not have permission to view this page');
      }
    } catch (err) {
      console.error('Failed to fetch departments:', err);
      setError(err.response?.data?.message || 'Failed to load departments');
    } finally {
      setLoading(false);
    }
  };

  const handleLogout = () => {
    logout();
    navigate('/login');
  };

  // Create Department Handlers
  const openCreateModal = () => {
    setCreateFormData({ name: '', code: '', managerId: '' });
    setCreateModalOpen(true);
    setCreateError(null);
  };

  const closeCreateModal = () => {
    setCreateModalOpen(false);
    setCreateFormData({ name: '', code: '', managerId: '' });
    setCreateError(null);
  };

  const handleCreateSubmit = async (e) => {
    e.preventDefault();
    setCreateLoading(true);
    setCreateError(null);

    try {
      // Validate required fields
      if (!createFormData.name || !createFormData.code) {
        setCreateError('Department name and code are required');
        setCreateLoading(false);
        return;
      }

      // Build payload
      const payload = {
        name: createFormData.name.trim(),
        code: createFormData.code.trim(),
      };

      // Add managerId if provided
      if (createFormData.managerId) {
        payload.managerId = createFormData.managerId;
      }

      const newDepartment = await departmentService.createDepartment(payload);

      // Update local state
      setDepartments([...departments, newDepartment]);

      setSuccessMessage('Department created successfully');
      setTimeout(() => setSuccessMessage(null), 3000);
      closeCreateModal();
    } catch (err) {
      console.error('Failed to create department:', err);
      setCreateError(err.response?.data?.message || 'Failed to create department');
    } finally {
      setCreateLoading(false);
    }
  };

  // Edit Department Handlers
  const openEditModal = (department) => {
    setEditingDepartment(department);
    setEditFormData({
      name: department.name || '',
      managerId: department.managerId || '',
    });
    setEditModalOpen(true);
    setEditError(null);
  };

  const closeEditModal = () => {
    setEditModalOpen(false);
    setEditingDepartment(null);
    setEditFormData({ name: '', managerId: '' });
    setEditError(null);
  };

  const handleEditSubmit = async (e) => {
    e.preventDefault();
    setEditLoading(true);
    setEditError(null);

    try {
      // Build update payload - only include changed fields
      const updateData = {};

      if (editFormData.name && editFormData.name !== editingDepartment.name) {
        updateData.name = editFormData.name.trim();
      }

      // Handle managerId - can be set, changed, or cleared
      if (editFormData.managerId !== editingDepartment.managerId) {
        updateData.managerId = editFormData.managerId || null;
      }

      // Validate at least one field changed
      if (Object.keys(updateData).length === 0) {
        setEditError('Please change at least one field');
        setEditLoading(false);
        return;
      }

      const updatedDepartment = await departmentService.updateDepartment(
        editingDepartment.id,
        updateData
      );

      // Update local state
      setDepartments(departments.map(d =>
        d.id === editingDepartment.id ? updatedDepartment : d
      ));

      setSuccessMessage('Department updated successfully');
      setTimeout(() => setSuccessMessage(null), 3000);
      closeEditModal();
    } catch (err) {
      console.error('Failed to update department:', err);
      setEditError(err.response?.data?.message || 'Failed to update department');
    } finally {
      setEditLoading(false);
    }
  };

  const formatDate = (dateString) => {
    if (!dateString) return 'N/A';
    return new Date(dateString).toLocaleDateString('en-US', {
      year: 'numeric',
      month: 'short',
      day: 'numeric',
    });
  };

  if (loading) {
    return (
      <div className="min-h-screen flex items-center justify-center">
        <div className="w-12 h-12 border-4 border-blue-600 border-t-transparent rounded-full animate-spin"></div>
      </div>
    );
  }

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
                className="text-gray-700 hover:text-gray-900 font-medium"
              >
                Expenses
              </button>
              {(isAdmin || user?.role === 'MANAGER') && (
                <button
                  onClick={() => navigate('/users')}
                  className="text-gray-700 hover:text-gray-900 font-medium"
                >
                  Users
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
      <main className="max-w-7xl mx-auto py-6 sm:px-6 lg:px-8">
        <div className="px-4 py-6 sm:px-0">
          {/* Header */}
          <div className="mb-6 flex justify-between items-center">
            <div>
              <h2 className="text-2xl font-bold text-gray-900">
                Department Management
              </h2>
              <p className="text-gray-600 mt-1">
                {isAdmin ? 'Create, view, and manage departments' : 'View all departments'}
              </p>
            </div>
            {isAdmin && (
              <Button onClick={openCreateModal}>
                Create Department
              </Button>
            )}
          </div>

          {/* Success/Error Messages */}
          {successMessage && (
            <div className="mb-4 p-4 bg-green-50 border border-green-200 rounded-lg">
              <p className="text-green-800">{successMessage}</p>
            </div>
          )}
          {error && (
            <div className="mb-4 p-4 bg-red-50 border border-red-200 rounded-lg">
              <p className="text-red-800">{error}</p>
            </div>
          )}

          {/* Departments Table */}
          <div className="bg-white rounded-lg shadow overflow-hidden">
            <div className="overflow-x-auto">
              <table className="min-w-full divide-y divide-gray-200">
                <thead className="bg-gray-50">
                  <tr>
                    <th className="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">
                      Name
                    </th>
                    <th className="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">
                      Code
                    </th>
                    <th className="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">
                      Manager
                    </th>
                    <th className="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">
                      Created At
                    </th>
                    <th className="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">
                      Updated At
                    </th>
                    {isAdmin && (
                      <th className="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">
                        Actions
                      </th>
                    )}
                  </tr>
                </thead>
                <tbody className="bg-white divide-y divide-gray-200">
                  {departments.length === 0 ? (
                    <tr>
                      <td
                        colSpan={isAdmin ? 6 : 5}
                        className="px-6 py-4 text-center text-gray-500"
                      >
                        No departments found
                      </td>
                    </tr>
                  ) : (
                    departments.map((dept) => (
                      <tr key={dept.id} className="hover:bg-gray-50">
                        <td className="px-6 py-4 whitespace-nowrap">
                          <div className="text-sm font-medium text-gray-900">
                            {dept.name}
                          </div>
                        </td>
                        <td className="px-6 py-4 whitespace-nowrap">
                          <div className="text-sm text-gray-900">{dept.code}</div>
                        </td>
                        <td className="px-6 py-4 whitespace-nowrap">
                          <div className="text-sm text-gray-900">
                            {dept.managerName || 'N/A'}
                          </div>
                        </td>
                        <td className="px-6 py-4 whitespace-nowrap">
                          <div className="text-sm text-gray-500">
                            {formatDate(dept.createdAt)}
                          </div>
                        </td>
                        <td className="px-6 py-4 whitespace-nowrap">
                          <div className="text-sm text-gray-500">
                            {formatDate(dept.updatedAt)}
                          </div>
                        </td>
                        {isAdmin && (
                          <td className="px-6 py-4 whitespace-nowrap text-sm">
                            <button
                              onClick={() => openEditModal(dept)}
                              className="text-blue-600 hover:text-blue-800 font-medium"
                            >
                              Edit
                            </button>
                          </td>
                        )}
                      </tr>
                    ))
                  )}
                </tbody>
              </table>
            </div>
          </div>
        </div>
      </main>

      {/* Create Department Modal */}
      <Modal
        isOpen={createModalOpen}
        onClose={closeCreateModal}
        title="Create Department"
      >
        <form onSubmit={handleCreateSubmit}>
          {createError && (
            <div className="mb-4 p-3 bg-red-50 border border-red-200 rounded-lg">
              <p className="text-red-800 text-sm">{createError}</p>
            </div>
          )}

          <Input
            label="Department Name"
            type="text"
            value={createFormData.name}
            onChange={(e) =>
              setCreateFormData({ ...createFormData, name: e.target.value })
            }
            required
            maxLength={100}
            placeholder="e.g., Engineering"
          />

          <Input
            label="Department Code"
            type="text"
            value={createFormData.code}
            onChange={(e) =>
              setCreateFormData({ ...createFormData, code: e.target.value })
            }
            required
            maxLength={20}
            placeholder="e.g., ENG"
          />

          <Select
            label="Manager (Optional)"
            value={createFormData.managerId}
            onChange={(e) =>
              setCreateFormData({ ...createFormData, managerId: e.target.value })
            }
            options={[
              { value: '', label: 'Select a manager' },
              ...users
                .filter(u => u.status === 'ACTIVE')
                .map((u) => ({
                  value: u.id,
                  label: `${u.name} (${u.email})`,
                }))
            ]}
          />

          <div className="flex space-x-3 mt-6">
            <Button type="submit" isLoading={createLoading}>
              Create Department
            </Button>
            <Button
              type="button"
              variant="secondary"
              onClick={closeCreateModal}
              disabled={createLoading}
            >
              Cancel
            </Button>
          </div>
        </form>
      </Modal>

      {/* Edit Department Modal */}
      <Modal
        isOpen={editModalOpen}
        onClose={closeEditModal}
        title={`Edit Department: ${editingDepartment?.name}`}
      >
        <form onSubmit={handleEditSubmit}>
          {editError && (
            <div className="mb-4 p-3 bg-red-50 border border-red-200 rounded-lg">
              <p className="text-red-800 text-sm">{editError}</p>
            </div>
          )}

          <Input
            label="Department Name"
            type="text"
            value={editFormData.name}
            onChange={(e) =>
              setEditFormData({ ...editFormData, name: e.target.value })
            }
            maxLength={100}
            placeholder="e.g., Engineering"
          />

          <Select
            label="Manager"
            value={editFormData.managerId}
            onChange={(e) =>
              setEditFormData({ ...editFormData, managerId: e.target.value })
            }
            options={[
              { value: '', label: 'No manager' },
              ...users
                .filter(u => u.status === 'ACTIVE')
                .map((u) => ({
                  value: u.id,
                  label: `${u.name} (${u.email})`,
                }))
            ]}
          />

          <div className="flex space-x-3 mt-6">
            <Button type="submit" isLoading={editLoading}>
              Save Changes
            </Button>
            <Button
              type="button"
              variant="secondary"
              onClick={closeEditModal}
              disabled={editLoading}
            >
              Cancel
            </Button>
          </div>
        </form>
      </Modal>
    </div>
  );
};

export default DepartmentManagementPage;
