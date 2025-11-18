/**
 * Estados de una elección
 * Debe coincidir con ElectionStatus.java del backend
 */
export enum ElectionStatus {
  DRAFT = 'DRAFT',
  ACTIVE = 'ACTIVE',
  CLOSED = 'CLOSED',
  COMPLETED = 'COMPLETED',
  CANCELLED = 'CANCELLED'
}

/**
 * Helper para obtener el nombre en español del estado
 */
export function getStatusName(status: ElectionStatus): string {
  const statusNames: Record<ElectionStatus, string> = {
    [ElectionStatus.DRAFT]: 'Borrador',
    [ElectionStatus.ACTIVE]: 'Activa',
    [ElectionStatus.CLOSED]: 'Cerrada',
    [ElectionStatus.COMPLETED]: 'Completada',
    [ElectionStatus.CANCELLED]: 'Cancelada'
  };
  return statusNames[status];
}

/**
 * Helper para obtener el color del estado (para badges)
 */
export function getStatusColor(status: ElectionStatus): string {
  const statusColors: Record<ElectionStatus, string> = {
    [ElectionStatus.DRAFT]: 'gray',
    [ElectionStatus.ACTIVE]: 'green',
    [ElectionStatus.CLOSED]: 'orange',
    [ElectionStatus.COMPLETED]: 'blue',
    [ElectionStatus.CANCELLED]: 'red'
  };
  return statusColors[status];
}
