import { useAuth } from '../context/AuthContext';

const usePermissions = () => {
  const { user } = useAuth();

  const canModifyExpense = (expense) => {
    if (!user || !expense) return false;
    
    // Users can modify their own expenses
    if (expense.createdById === user.id) {
      return true;
    }
    
    // Admin and Finance can modify any expense
    return ['ADMIN', 'FINANCE'].includes(user.role);
  };

  const canViewExpense = (expense) => {
    if (!user || !expense) return false;
    
    // Users can view their own expenses
    if (expense.createdById === user.id) {
      return true;
    }
    
    // Manager, Finance, and Admin can view any expense
    return ['MANAGER', 'FINANCE', 'ADMIN'].includes(user.role);
  };

  const canModifySegments = (expense) => {
    // Segment modification follows the same rules as expense modification
    return canModifyExpense(expense);
  };

  const canManageCategories = () => {
    if (!user) return false;
    
    // Only Manager, Finance, and Admin can manage categories
    return ['MANAGER', 'FINANCE', 'ADMIN'].includes(user.role);
  };

  const canUploadAttachments = (expense) => {
    if (!user || !expense) return false;
    
    // Users can upload attachments to their own expenses
    if (expense.createdById === user.id) {
      return true;
    }
    
    // Finance and Admin can upload attachments to any expense
    return ['FINANCE', 'ADMIN'].includes(user.role);
  };

  const canDeleteAttachments = (attachment) => {
    if (!user || !attachment) return false;
    
    // Users can delete their own attachments
    if (attachment.uploadedBy === user.id) {
      return true;
    }
    
    // Finance and Admin can delete any attachments
    return ['FINANCE', 'ADMIN'].includes(user.role);
  };

  return {
    canModifyExpense,
    canViewExpense,
    canModifySegments,
    canManageCategories,
    canUploadAttachments,
    canDeleteAttachments
  };
};

export default usePermissions;