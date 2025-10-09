export const useRoleBadges = () => {
  const getRoleVariant = (role) => {
    const roleMap = {
      ADMIN: 'purple',
      MANAGER: 'primary',
      FINANCE: 'warning',
      EMPLOYEE: 'default'
    };
    return roleMap[role] || 'default';
  };

  return { getRoleVariant };
};
