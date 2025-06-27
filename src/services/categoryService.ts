import apiClient from './api';
import type { ExpenseCategoryDto } from '../types/categoryTypes';

export const getAllCategories = async (): Promise<ExpenseCategoryDto[]> => {
  const response = await apiClient.get<ExpenseCategoryDto[]>('/admin/categories'); // Ensure this endpoint is accessible by employees for selection
  return response.data;
};