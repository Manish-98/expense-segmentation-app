import { useState, useEffect } from 'react';
import { useAuth } from '../context/AuthContext';
import { useNavigate } from 'react-router-dom';
import userService from '../api/userService';
import Modal from '../components/Modal';
import Select from '../components/Select';
import Button from '../components/Button';

const UserManagementPage = () => {
  const { user, logout } = useAuth();
  const navigate = useNavigate();
  const [users, setUsers] = useState([]);
  const [departments, setDepartments] = useState([]);
  const [roles, setRoles] = useState([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState(null);
  const [successMessage, setSuccessMessage] = useState(null);

  // Edit modal state
  const [editModalOpen, setEditModalOpen] = useState(false);
  const [editingUser, setEditingUser] = useState(null);
  const [editFormData, setEditFormData] = useState({ role: '', departmentId: '' });
  const [editLoading, setEditLoading] = useState(false);
  const [editError, setEditError] = useState(null);

  const isAdmin = user?.role === 'ADMIN';
  const isManager = user?.role === 'MANAGER';

  useEffect(() => {
    fetchData();
  }, []);

  const fetchData = async () => {
    setLoading(true);
    setError(null);
    try {
      // Fetch users based on role
      let usersData;
      if (isAdmin) {
        usersData = await userService.getAllUsers();
      } else if (isManager) {
        usersData = await userService.getDepartmentUsers();
      } else {
        setError('You do not have permission to view this page');
        setLoading(false);
        return;
      }
      setUsers(usersData);

      // Fetch departments and roles if admin (for editing)
      if (isAdmin) {
        try {
          const [depts, rolesData] = await Promise.all([
            userService.getAllDepartments(),
            userService.getAllRoles(),
          ]);
          setDepartments(depts);
          setRoles(rolesData);
        } catch (err) {
          console.error('Failed to fetch departments/roles:', err);
        }
      }
    } catch (err) {
      console.error('Failed to fetch users:', err);
      setError(err.response?.data?.message || 'Failed to load users');
    } finally {
      setLoading(false);
    }
  };

  const handleLogout = () => {
    logout();
    navigate('/login');
  };

  const openEditModal = (userToEdit) => {
    setEditingUser(userToEdit);
    setEditFormData({
      role: userToEdit.role || '',
      departmentId: userToEdit.departmentId || '',
    });
    setEditModalOpen(true);
    setEditError(null);
  };

  const closeEditModal = () => {
    setEditModalOpen(false);
    setEditingUser(null);
    setEditFormData({ role: '', departmentId: '' });
    setEditError(null);
  };

  const handleEditSubmit = async (e) => {
    e.preventDefault();
    setEditLoading(true);
    setEditError(null);

    try {
      // Build update payload - only include changed fields
      const updateData = {};
      if (editFormData.role && editFormData.role !== editingUser.role) {
        updateData.role = editFormData.role;
      }
      if (editFormData.departmentId && editFormData.departmentId !== editingUser.departmentId) {
        updateData.departmentId = editFormData.departmentId;
      }

      // Validate at least one field changed
      if (Object.keys(updateData).length === 0) {
        setEditError('Please change at least one field');
        setEditLoading(false);
        return;
      }

      await userService.updateUser(editingUser.id, updateData);

      // Update local state
      setUsers(users.map(u =>
        u.id === editingUser.id
          ? { ...u, ...updateData, departmentName: departments.find(d => d.id === updateData.departmentId)?.name || u.departmentName }
          : u
      ));

      setSuccessMessage('User updated successfully');
      setTimeout(() => setSuccessMessage(null), 3000);
      closeEditModal();
    } catch (err) {
      console.error('Failed to update user:', err);
      setEditError(err.response?.data?.message || 'Failed to update user');
    } finally {
      setEditLoading(false);
    }
  };

  const handleDeactivate = async (userId, userName) => {
    if (!window.confirm(`Are you sure you want to deactivate ${userName}?`)) {
      return;
    }

    try {
      await userService.deactivateUser(userId);

      // Update local state
      setUsers(users.map(u =>
        u.id === userId ? { ...u, status: 'INACTIVE' } : u
      ));

      setSuccessMessage('User deactivated successfully');
      setTimeout(() => setSuccessMessage(null), 3000);
    } catch (err) {
      console.error('Failed to deactivate user:', err);
      setError(err.response?.data?.message || 'Failed to deactivate user');
      setTimeout(() => setError(null), 3000);
    }
  };

  const getStatusBadgeClass = (status) => {
    return status === 'ACTIVE'
      ? 'px-2 py-1 text-xs font-semibold rounded-full bg-green-100 text-green-800'
      : 'px-2 py-1 text-xs font-semibold rounded-full bg-red-100 text-red-800';
  };

  const getRoleBadgeClass = (role) => {
    const colorMap = {
      ADMIN: 'bg-purple-100 text-purple-800',
      MANAGER: 'bg-blue-100 text-blue-800',
      FINANCE: 'bg-yellow-100 text-yellow-800',
      EMPLOYEE: 'bg-gray-100 text-gray-800',
      OWNER: 'bg-red-100 text-red-800',
    };
    return `px-2 py-1 text-xs font-semibold rounded-full ${colorMap[role] || 'bg-gray-100 text-gray-800'}`;
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
              {(isAdmin || user?.role === 'FINANCE') && (
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
      <main className="max-w-7xl mx-auto py-6 sm:px-6 lg:px-8">
        <div className="px-4 py-6 sm:px-0">
          {/* Header */}
          <div className="mb-6">
            <h2 className="text-2xl font-bold text-gray-900">
              User Management
            </h2>
            <p className="text-gray-600 mt-1">
              {isAdmin ? 'View and manage all users' : 'View users in your department'}
            </p>
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

          {/* Users Table */}
          <div className="bg-white rounded-lg shadow overflow-hidden">
            <div className="overflow-x-auto">
              <table className="min-w-full divide-y divide-gray-200">
                <thead className="bg-gray-50">
                  <tr>
                    <th className="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">
                      Name
                    </th>
                    <th className="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">
                      Email
                    </th>
                    <th className="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">
                      Role
                    </th>
                    <th className="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">
                      Department
                    </th>
                    <th className="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">
                      Status
                    </th>
                    {isAdmin && (
                      <th className="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">
                        Actions
                      </th>
                    )}
                  </tr>
                </thead>
                <tbody className="bg-white divide-y divide-gray-200">
                  {users.length === 0 ? (
                    <tr>
                      <td
                        colSpan={isAdmin ? 6 : 5}
                        className="px-6 py-4 text-center text-gray-500"
                      >
                        No users found
                      </td>
                    </tr>
                  ) : (
                    users.map((u) => (
                      <tr key={u.id} className="hover:bg-gray-50">
                        <td className="px-6 py-4 whitespace-nowrap">
                          <div className="text-sm font-medium text-gray-900">
                            {u.name}
                          </div>
                        </td>
                        <td className="px-6 py-4 whitespace-nowrap">
                          <div className="text-sm text-gray-500">{u.email}</div>
                        </td>
                        <td className="px-6 py-4 whitespace-nowrap">
                          <span className={getRoleBadgeClass(u.role)}>
                            {u.role}
                          </span>
                        </td>
                        <td className="px-6 py-4 whitespace-nowrap">
                          <div className="text-sm text-gray-900">
                            {u.departmentName || 'N/A'}
                          </div>
                        </td>
                        <td className="px-6 py-4 whitespace-nowrap">
                          <span className={getStatusBadgeClass(u.status)}>
                            {u.status}
                          </span>
                        </td>
                        {isAdmin && (
                          <td className="px-6 py-4 whitespace-nowrap text-sm">
                            <div className="flex space-x-2">
                              <button
                                onClick={() => openEditModal(u)}
                                disabled={u.status === 'INACTIVE'}
                                className="text-blue-600 hover:text-blue-800 font-medium disabled:text-gray-400 disabled:cursor-not-allowed"
                              >
                                Edit
                              </button>
                              <button
                                onClick={() => handleDeactivate(u.id, u.name)}
                                disabled={u.status === 'INACTIVE'}
                                className="text-red-600 hover:text-red-800 font-medium disabled:text-gray-400 disabled:cursor-not-allowed"
                              >
                                Deactivate
                              </button>
                            </div>
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

      {/* Edit User Modal */}
      <Modal
        isOpen={editModalOpen}
        onClose={closeEditModal}
        title={`Edit User: ${editingUser?.name}`}
      >
        <form onSubmit={handleEditSubmit}>
          {editError && (
            <div className="mb-4 p-3 bg-red-50 border border-red-200 rounded-lg">
              <p className="text-red-800 text-sm">{editError}</p>
            </div>
          )}

          <Select
            label="Role"
            value={editFormData.role}
            onChange={(e) =>
              setEditFormData({ ...editFormData, role: e.target.value })
            }
            options={roles.map((r) => ({
              value: r.name,
              label: r.name,
            }))}
          />

          <Select
            label="Department"
            value={editFormData.departmentId}
            onChange={(e) =>
              setEditFormData({ ...editFormData, departmentId: e.target.value })
            }
            options={departments.map((d) => ({
              value: d.id,
              label: d.name,
            }))}
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

export default UserManagementPage;
