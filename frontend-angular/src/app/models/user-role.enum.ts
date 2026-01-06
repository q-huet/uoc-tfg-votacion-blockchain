/**
 * Roles de usuario en el sistema de votación
 * Debe coincidir con UserRole.java del backend
 */
export enum UserRole {
  VOTER = 'VOTER',
  ADMIN = 'ADMIN',
  AUDITOR = 'AUDITOR'
}

/**
 * Helper para obtener el nombre en español del rol
 */
export function getRoleName(role: UserRole): string {
  const roleNames: Record<UserRole, string> = {
    [UserRole.VOTER]: 'Votante',
    [UserRole.ADMIN]: 'Administrador',
    [UserRole.AUDITOR]: 'Auditor'
  };
  return roleNames[role];
}
