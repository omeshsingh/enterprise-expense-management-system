import apiClient from './api';
import type { Page } from '../types/page';
import type { ExpenseResponseDto, ExpenseRequestData } from '../types/expenseTypes';
import type { ApprovalHistoryDto, ApprovalRequestDto } from '../types/approvalTypes'; 
// import type { ExpenseResponseDto } from '../types/expenseTypes';
// import type { ApprovalRequestDto } from '../types/approvalTypes';

export const createExpense = async (expenseData: ExpenseRequestData, files?: File[]): Promise<ExpenseResponseDto> => {
  const formData = new FormData();
  formData.append('expense', new Blob([JSON.stringify(expenseData)], { type: 'application/json' }));
  if (files && files.length > 0) {
    files.forEach((file) => {
      formData.append('files', file);
    });
  }
  const response = await apiClient.post<ExpenseResponseDto>('/expenses', formData);
  return response.data;
};

export const getMyExpenses = async (page: number, size: number, sort?: string): Promise<Page<ExpenseResponseDto>> => {
  const params = new URLSearchParams();
  params.append('page', page.toString());
  params.append('size', size.toString());
  if (sort) {
    params.append('sort', sort);
  }
  const response = await apiClient.get<Page<ExpenseResponseDto>>(`/expenses/my?${params.toString()}`);
  return response.data;
};

// GET /api/expenses/{id} - 
export const getExpenseById = async (id: number): Promise<ExpenseResponseDto> => {
    const response = await apiClient.get<ExpenseResponseDto>(`/expenses/${id}`);
    return response.data;
  };
  
  // PUT /api/expenses/{id}
  export const updateExpense = async (id: number, expenseData: ExpenseRequestData, files?: File[]): Promise<ExpenseResponseDto> => {
    const formData = new FormData();
    formData.append('expense', new Blob([JSON.stringify(expenseData)], { type: 'application/json' }));
  
    if (files && files.length > 0) {
      files.forEach((file) => {
        formData.append('files', file);
      });
    }
    // To handle removing attachments, you might need another field in formData, e.g.,
    // formData.append('attachmentsToRemove', JSON.stringify([attachmentId1, attachmentId2]));
    // This would require backend changes to process `attachmentsToRemove`.
  
    const response = await apiClient.put<ExpenseResponseDto>(`/expenses/${id}`, formData);
    return response.data;
  };
  
  // DELETE /api/expenses/{id}
  export const deleteExpense = async (id: number): Promise<void> => {
    await apiClient.delete(`/expenses/${id}`);
  };
  
  // GET /api/expenses/{expenseId}/history
  export const getExpenseApprovalHistory = async (expenseId: number): Promise<ApprovalHistoryDto[]> => {
    const response = await apiClient.get<ApprovalHistoryDto[]>(`/expenses/${expenseId}/history`);
    return response.data;
  };
  
  // GET /api/expenses/attachments/{attachmentId}/download - This will be a direct link in UI
  // No specific service function needed if you just construct the URL, but you could have one
  // export const getAttachmentDownloadUrl = (attachmentId: number): string => {
  //   return `${apiClient.defaults.baseURL}/expenses/attachments/${attachmentId}/download`;
  // };

  export const getPendingApprovals = async (page: number, size: number, sort?: string): Promise<Page<ExpenseResponseDto>> => {
    const params = new URLSearchParams();
    params.append('page', page.toString());
    params.append('size', size.toString());
    if (sort) {
      params.append('sort', sort);
    }
    const response = await apiClient.get<Page<ExpenseResponseDto>>(`/approvals/pending?${params.toString()}`);
    return response.data;
  };
  
  export const approveExpenseApi = async (expenseId: number, data: ApprovalRequestDto): Promise<ExpenseResponseDto> => {
    const response = await apiClient.post<ExpenseResponseDto>(`/expenses/${expenseId}/approve`, data);
    return response.data;
  };
  
  export const rejectExpenseApi = async (expenseId: number, data: ApprovalRequestDto): Promise<ExpenseResponseDto> => {
    // Ensure comments are part of the data for rejection
    if (!data.comments || data.comments.trim() === '') {
      throw new Error("Rejection comments are mandatory.");
    }
    const response = await apiClient.post<ExpenseResponseDto>(`/expenses/${expenseId}/reject`, data);
    return response.data;
  };
