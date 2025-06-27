import React, { useEffect, useState, useCallback } from 'react';
import {
  Table, TableBody, TableCell, TableContainer, TableHead, TableRow, Paper,
  Typography, Box, CircularProgress, Alert, TablePagination, IconButton, Tooltip, Button,
  Dialog, DialogActions, DialogContent, DialogContentText, DialogTitle, TextField
} from '@mui/material';
import CheckCircleIcon from '@mui/icons-material/CheckCircle';
import CancelIcon from '@mui/icons-material/Cancel';
import VisibilityIcon from '@mui/icons-material/Visibility';
import { getPendingApprovals, approveExpenseApi, rejectExpenseApi } from '../../services/expenseService';
import type { ExpenseResponseDto } from '../../types/expenseTypes';
import type { Page } from '../../types/page';
import { useNavigate } from 'react-router-dom';
import { format, parseISO } from 'date-fns';
import { useSnackbar } from '../../contexts/SnackbarContext';
import type { ApprovalRequestDto } from '../../types/approvalTypes';

const PendingApprovalsPage: React.FC = () => {
  const [pendingExpensesPage, setPendingExpensesPage] = useState<Page<ExpenseResponseDto> | null>(null);
  const [loading, setLoading] = useState<boolean>(true);
  const [actionLoading, setActionLoading] = useState<boolean>(false);
  const [error, setError] = useState<string | null>(null);
  const [page, setPage] = useState<number>(0);
  const [rowsPerPage, setRowsPerPage] = useState<number>(10);
  
  const [openActionDialog, setOpenActionDialog] = useState(false);
  const [currentExpense, setCurrentExpense] = useState<ExpenseResponseDto | null>(null);
  const [actionType, setActionType] = useState<'approve' | 'reject' | null>(null);
  const [comments, setComments] = useState('');

  const navigate = useNavigate();
  const { showSnackbar } = useSnackbar();

  const fetchPendingExpenses = useCallback(async (currentPage: number, currentRowsPerPage: number) => {
    setLoading(true);
    setError(null);
    try {
      const data = await getPendingApprovals(currentPage, currentRowsPerPage, 'createdAt,asc'); // Sort by oldest first
      setPendingExpensesPage(data);
    } catch (err: any) {
      setError(err.response?.data?.message || err.message || 'Failed to fetch pending approvals.');
      console.error("Fetch pending approvals error:", err);
    } finally {
      setLoading(false);
    }
  }, []);

  useEffect(() => {
    fetchPendingExpenses(page, rowsPerPage);
  }, [fetchPendingExpenses, page, rowsPerPage]);

  const handleChangePage = (event: unknown, newPage: number) => {
    setPage(newPage);
  };

  const handleChangeRowsPerPage = (event: React.ChangeEvent<HTMLInputElement>) => {
    setRowsPerPage(parseInt(event.target.value, 10));
    setPage(0);
  };

  const handleViewDetails = (expenseId: number) => {
    navigate(`/dashboard/expenses/${expenseId}`); // Navigate to the common detail page
  };

  const handleOpenActionDialog = (expense: ExpenseResponseDto, type: 'approve' | 'reject') => {
    setCurrentExpense(expense);
    setActionType(type);
    setComments(''); // Reset comments
    setOpenActionDialog(true);
  };

  const handleCloseActionDialog = () => {
    setOpenActionDialog(false);
    setCurrentExpense(null);
    setActionType(null);
  };

  const handleConfirmAction = async () => {
    if (!currentExpense || !actionType) return;

    if (actionType === 'reject' && (!comments || comments.trim() === '')) {
      showSnackbar('Comments are mandatory for rejection.', 'warning');
      return;
    }

    setActionLoading(true);
    const actionData: ApprovalRequestDto = { comments };

    try {
      if (actionType === 'approve') {
        await approveExpenseApi(currentExpense.id, actionData);
        showSnackbar(`Expense ID ${currentExpense.id} approved successfully.`, 'success');
      } else {
        await rejectExpenseApi(currentExpense.id, actionData);
        showSnackbar(`Expense ID ${currentExpense.id} rejected successfully.`, 'success');
      }
      fetchPendingExpenses(page, rowsPerPage); // Refresh list
    } catch (err: any) {
      showSnackbar(err.response?.data?.message || err.message || `Failed to ${actionType} expense.`, 'error');
      console.error(`${actionType} expense error:`, err);
    } finally {
      setActionLoading(false);
      handleCloseActionDialog();
    }
  };


  if (loading && !pendingExpensesPage) {
    return <Box display="flex" justifyContent="center" alignItems="center" minHeight="200px"><CircularProgress /></Box>;
  }

  if (error) {
    return <Alert severity="error" sx={{ m: 2 }}>{error}</Alert>;
  }

  return (
    <Paper sx={{ width: '100%', overflow: 'hidden', p: 2, mt: 2 }}>
      <Typography variant="h5" component="div" sx={{ mb: 2 }}>
        Pending Expense Approvals
      </Typography>

      {pendingExpensesPage && pendingExpensesPage.content && pendingExpensesPage.content.length > 0 ? (
        <>
          <TableContainer>
            <Table stickyHeader aria-label="pending approvals table">
              <TableHead>
                <TableRow>
                  <TableCell sx={{ fontWeight: 'bold' }}>Submitted Date</TableCell>
                  <TableCell sx={{ fontWeight: 'bold' }}>Submitted By</TableCell>
                  <TableCell sx={{ fontWeight: 'bold' }}>Description</TableCell>
                  <TableCell sx={{ fontWeight: 'bold' }}>Category</TableCell>
                  <TableCell align="right" sx={{ fontWeight: 'bold' }}>Amount</TableCell>
                  <TableCell align="center" sx={{ fontWeight: 'bold' }}>Actions</TableCell>
                </TableRow>
              </TableHead>
              <TableBody>
                {pendingExpensesPage.content.map((expense) => (
                  <TableRow hover key={expense.id}>
                    <TableCell>{format(parseISO(expense.createdAt), 'MMM dd, yyyy')}</TableCell>
                    <TableCell>{expense.username}</TableCell> {/* Assuming username is of the submitter */}
                    <TableCell>{expense.description}</TableCell>
                    <TableCell>{expense.categoryName}</TableCell>
                    <TableCell align="right">${expense.amount.toFixed(2)}</TableCell>
                    <TableCell align="center">
                      <Tooltip title="View Details">
                        <IconButton onClick={() => handleViewDetails(expense.id)} size="small" color="primary" disabled={actionLoading}>
                          <VisibilityIcon />
                        </IconButton>
                      </Tooltip>
                      <Tooltip title="Approve Expense">
                        <span>
                          <IconButton onClick={() => handleOpenActionDialog(expense, 'approve')} size="small" color="success" disabled={actionLoading}>
                            <CheckCircleIcon />
                          </IconButton>
                        </span>
                      </Tooltip>
                      <Tooltip title="Reject Expense">
                        <span>
                          <IconButton onClick={() => handleOpenActionDialog(expense, 'reject')} size="small" color="error" disabled={actionLoading}>
                            <CancelIcon />
                          </IconButton>
                        </span>
                      </Tooltip>
                    </TableCell>
                  </TableRow>
                ))}
              </TableBody>
            </Table>
          </TableContainer>
          <TablePagination
            rowsPerPageOptions={[5, 10, 25]}
            component="div"
            count={pendingExpensesPage.totalElements}
            rowsPerPage={rowsPerPage}
            page={page}
            onPageChange={handleChangePage}
            onRowsPerPageChange={handleChangeRowsPerPage}
          />
        </>
      ) : (
        !loading && <Typography sx={{ textAlign: 'center', mt: 3 }}>No expenses pending your approval.</Typography>
      )}

      {/* Action Confirmation Dialog */}
      <Dialog open={openActionDialog} onClose={handleCloseActionDialog}>
        <DialogTitle>{actionType === 'approve' ? 'Approve Expense' : 'Reject Expense'} ID: {currentExpense?.id}</DialogTitle>
        <DialogContent>
          <DialogContentText sx={{mb:1}}>
            {actionType === 'approve' ? 'Do you want to approve this expense?' : 'Please provide a reason for rejecting this expense.'}
          </DialogContentText>
          <TextField
            autoFocus
            margin="dense"
            id="comments"
            label="Comments"
            type="text"
            fullWidth
            variant="outlined"
            multiline
            rows={3}
            value={comments}
            onChange={(e) => setComments(e.target.value)}
            required={actionType === 'reject'}
          />
        </DialogContent>
        <DialogActions>
          <Button onClick={handleCloseActionDialog} color="inherit" disabled={actionLoading}>Cancel</Button>
          <Button onClick={handleConfirmAction} color={actionType === 'approve' ? "success" : "error"} variant="contained" disabled={actionLoading}>
            {actionLoading ? <CircularProgress size={20} color="inherit"/> : (actionType === 'approve' ? 'Approve' : 'Reject')}
          </Button>
        </DialogActions>
      </Dialog>
    </Paper>
  );
};

export default PendingApprovalsPage;