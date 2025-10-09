export const useExpenseBadges = () => {
  const getStatusVariant = (status) => {
    const statusMap = {
      SUBMITTED: 'primary',
      APPROVED: 'success',
      REJECTED: 'danger',
      PENDING: 'warning'
    };
    return statusMap[status] || 'default';
  };

  const getTypeVariant = (type) => {
    return type === 'EXPENSE' ? 'purple' : 'indigo';
  };

  return { getStatusVariant, getTypeVariant };
};
