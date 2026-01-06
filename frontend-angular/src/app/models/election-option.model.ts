/**
 * Opción de votación en una elección
 * Coincide con ElectionOption.java (record) del backend
 */
export interface ElectionOption {
  optionId: string;
  title: string;
  description: string | null;
  displayOrder: number;
}

/**
 * Opción con resultados (solo para ADMIN/AUDITOR)
 */
export interface ElectionOptionWithResults extends ElectionOption {
  voteCount: number;
  percentage: number;
}
