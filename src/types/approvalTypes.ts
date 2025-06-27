export interface ApprovalHistoryDto {
    id: number;
    expenseId: number;
    approverUserId: number;
    approverUsername: string;
    statusBefore: string | null; // Or map to a frontend ExpenseStatus enum
    statusAfter: string;    // Or map to a frontend ExpenseStatus enum
    comments: string | null;
    actionDate: string; // ISO Date string, e.g., "2023-10-27T10:15:30Z"
}

export interface ApprovalRequestDto {
    comments: string;
}