import { useState, useEffect } from 'react';
import { useAuth } from '../context/AuthContext';
import userService from '../api/userService';
import Navbar from '../components/Navigation/Navbar';
import PageLayout from '../components/Layout/PageLayout';
import PageContainer from '../components/Layout/PageContainer';
import Card from '../components/Layout/Card';
import LoadingSpinner from '../components/Feedback/LoadingSpinner';
import Alert from '../components/Feedback/Alert';
import EmptyState from '../components/Feedback/EmptyState';
import Badge from '../components/Feedback/Badge';
import Modal from '../components/Modal';
import Select from '../components/Select';
import Button from '../components/Button';
import { useRoleBadges } from '../hooks/useRoleBadges';

const UserManagementPage = () => {
  const { user } = useAuth();
  const { getRoleVariant } = useRoleBadges();
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

  const getStatusVariant = (status) => {
    return status === 'ACTIVE' ? 'success' : 'danger';
  };

  if (loading) {
    return (
      <PageLayout>
        <LoadingSpinner size="lg" centered />
      </PageLayout>
    );
  }

  return (
    <PageLayout>
      <Navbar />

      <PageContainer>
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
          <Alert variant="success" className="mb-4" onClose={() => setSuccessMessage(null)}>
            {successMessage}
          </Alert>
        )}
        {error && (
          <Alert variant="danger" className="mb-4" onClose={() => setError(null)}>
            {error}
          </Alert>
        )}

        {/* Users Table */}
        <Card>
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
                      className="px-6 py-4"
                    >
                      <EmptyState
                        title="No users found"
                        message="There are no users to display."
                      />
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
                        <Badge variant={getRoleVariant(u.role)}>
                          {u.role}
                        </Badge>
                      </td>
                      <td className="px-6 py-4 whitespace-nowrap">
                        <div className="text-sm text-gray-900">
                          {u.departmentName || 'N/A'}
                        </div>
                      </td>
                      <td className="px-6 py-4 whitespace-nowrap">
                        <Badge variant={getStatusVariant(u.status)}>
                          {u.status}
                        </Badge>
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
        </Card>
      </PageContainer>

      {/* Edit User Modal */}
      <Modal
        isOpen={editModalOpen}
        onClose={closeEditModal}
        title={`Edit User: ${editingUser?.name}`}
      >
        <form onSubmit={handleEditSubmit}>
          {editError && (
            <Alert variant="danger" className="mb-4">
              {editError}
            </Alert>
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
    </PageLayout>
  );
};

export default UserManagementPage;
