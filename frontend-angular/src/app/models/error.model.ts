/**
 * Response de error estándar del backend
 * Coincide con ErrorResponse DTO del backend
 */
export interface ErrorResponse {
  timestamp: string; // ISO 8601 datetime string
  status: number;
  error: string;
  message: string;
  path: string;
  details?: string[];
}

/**
 * Tipos de errores comunes
 */
export enum ErrorType {
  AUTHENTICATION_FAILED = 'AUTHENTICATION_FAILED',
  UNAUTHORIZED = 'UNAUTHORIZED',
  FORBIDDEN = 'FORBIDDEN',
  NOT_FOUND = 'NOT_FOUND',
  VALIDATION_ERROR = 'VALIDATION_ERROR',
  DUPLICATE_VOTE = 'DUPLICATE_VOTE',
  ELECTION_CLOSED = 'ELECTION_CLOSED',
  SERVER_ERROR = 'SERVER_ERROR',
  NETWORK_ERROR = 'NETWORK_ERROR'
}

/**
 * Helper para obtener mensaje de error amigable
 */
export function getFriendlyErrorMessage(error: any): string {
  if (error.error?.message) {
    return error.error.message;
  }

  if (error.status === 0) {
    return 'No se puede conectar con el servidor. Verifica tu conexión.';
  }

  const statusMessages: Record<number, string> = {
    400: 'Solicitud inválida. Verifica los datos ingresados.',
    401: 'No estás autenticado. Por favor, inicia sesión.',
    403: 'No tienes permisos para realizar esta acción.',
    404: 'El recurso solicitado no existe.',
    409: 'Ya has votado en esta elección.',
    500: 'Error del servidor. Intenta nuevamente más tarde.',
    503: 'Servicio no disponible. Intenta nuevamente más tarde.'
  };

  return statusMessages[error.status] || 'Ha ocurrido un error inesperado.';
}
