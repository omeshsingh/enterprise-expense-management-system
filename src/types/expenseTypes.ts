export interface AttachmentResponseDto {
    id: number;
    fileName: string;
    fileType: string;
    uploadedAt: string;
  }
  
  export interface ExpenseRequestData { // Used for creating an expense
    description: string;
    amount: number;       // Will be sent as number
    expenseDate: string;  // YYYY-MM-DD
    categoryId: number;
  }
  
  export interface ExpenseResponseDto { // Received from backend
    id: number;
    description: string;
    amount: number;
    expenseDate: string;
    status: string;
    userId: number;
    username: string;
    categoryId: number;
    categoryName: string;
    attachments: AttachmentResponseDto[];
    createdAt: string;
    updatedAt: string;
  }