export const ROLES = {
  ADMIN: 'ADMIN',
  MANAGER: 'MANAGER',
  FINANCE: 'FINANCE',
  EMPLOYEE: 'EMPLOYEE'
};

export const canAccessUsers = (role) => {
  return [ROLES.ADMIN, ROLES.MANAGER].includes(role);
};

export const canAccessDepartments = (role) => {
  return [ROLES.ADMIN, ROLES.FINANCE].includes(role);
};
