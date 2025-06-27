import React, { useEffect, useState, useCallback } from 'react';
import {
  Table, TableBody, TableCell, TableContainer, TableHead, TableRow, Paper,
  Typography, Box, CircularProgress, Alert, TablePagination, IconButton, Tooltip, Button, Chip
} from '@mui/material';
import VisibilityIcon from '@mui/icons-material/Visibility';
import AddIcon from '@mui/icons-material/Add';
import EditIcon from '@mui/icons-material/Edit';
import DeleteIcon from '@mui/icons-material/Delete';
import { getMyExpenses, deleteExpense } from '../../services/expenseService'; // Added deleteExpense
import type { ExpenseResponseDto } from '../../types/expenseTypes'; // Ensure this path is correct
import type { Page } from '../../types/page'; // Ensure this path is correct
import { useNavigate } from 'react-router-dom';
import { format, parseISO } from 'date-fns'; // parseISO for robust date parsing

const MyExpensesPage: React.FC = () => {
  const [expensesPage, setExpensesPage] = useState<Page<ExpenseResponseDto> | null>(null);
  const [loading, setLoading] = useState<boolean>(true);
  const [actionLoading, setActionLoading] = useState<boolean>(false); // For delete/edit actions
  const [error, setError] = useState<string | null>(null);
  const [successMessage, setSuccessMessage] = useState<string | null>(null);
  const [page, setPage] = useState<number>(0);
  const [rowsPerPage, setRowsPerPage] = useState<number>(10);
  const navigate = useNavigate();

  const fetchExpenses = useCallback(async (currentPage: number, currentRowsPerPage: number) => {
    setLoading(true);
    setError(null);
    // setSuccessMessage(null); // Clear success message on new fetch
    try {
      const data = await getMyExpenses(currentPage, currentRowsPerPage, 'expenseDate,desc');
      setExpensesPage(data);
    } catch (err: any) {
      setError(err.response?.data?.message || err.message || 'Failed to fetch expenses.');
      console.error("Fetch expenses error:", err);
    } finally {
      setLoading(false);
    }
  }, []);

  useEffect(() => {
    fetchExpenses(page, rowsPerPage);
  }, [fetchExpenses, page, rowsPerPage]);

  const handleChangePage = (event: unknown, newPage: number) => {
    setPage(newPage);
  };

  const handleChangeRowsPerPage = (event: React.ChangeEvent<HTMLInputElement>) => {
    setRowsPerPage(parseInt(event.target.value, 10));
    setPage(0);
  };

  const handleViewDetails = (expenseId: number) => {
    navigate(`/dashboard/expenses/${expenseId}`); // Navigate to actual detail page
  };

  const handleEditExpense = (expenseId: number) => {
    navigate(`/dashboard/expenses/${expenseId}/edit`); // Navigate to actual edit page
  };

  const handleDeleteExpense = async (expenseId: number) => {
    if (window.confirm('Are you sure you want to delete this expense? This action cannot be undone.')) {
      setActionLoading(true);
      setError(null);
      setSuccessMessage(null);
      try {
        await deleteExpense(expenseId);
        setSuccessMessage('Expense deleted successfully!');
        // Refresh the expenses list after deletion
        // To ensure pagination is correct, fetch the current page again
        // If the current page becomes empty after deletion, you might want to go to the previous page
        fetchExpenses(page, rowsPerPage);
      } catch (err: any) {
        setError(err.response?.data?.message || err.message || 'Failed to delete expense.');
        console.error("Delete expense error:", err);
      } finally {
        setActionLoading(false);
      }
    }
  };

  const handleNavigateToSubmit = () => {
    navigate('/dashboard/submit-expense');
  };

  // Clear messages after a few seconds
  useEffect(() => {
    if (successMessage) {
      const timer = setTimeout(() => setSuccessMessage(null), 3000);
      return () => clearTimeout(timer);
    }
    if (error) { // Also clear error if user starts typing or navigates, but this is a simple timed clear
        const timer = setTimeout(() => setError(null), 5000);
        return () => clearTimeout(timer);
    }
  }, [successMessage, error]);


  if (loading && !expensesPage) {
    return (
      <Box display="flex" justifyContent="center" alignItems="center" minHeight="200px">
        <CircularProgress />
      </Box>
    );
  }

  return (
    <Paper sx={{ width: '100%', overflow: 'hidden', p: 2, mt: 2 }}>
      <Box sx={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', mb: 2 }}>
        <Typography variant="h5" component="div">
          My Submitted Expenses
        </Typography>
        <Button
          variant="contained"
          color="primary"
          startIcon={<AddIcon />}
          onClick={handleNavigateToSubmit}
        >
          New Expense
        </Button>
      </Box>

      {error && <Alert severity="error" sx={{ mb: 2 }} onClose={() => setError(null)}>{error}</Alert>}
      {successMessage && <Alert severity="success" sx={{ mb: 2 }} onClose={() => setSuccessMessage(null)}>{successMessage}</Alert>}

      {actionLoading && (
        <Box display="flex" justifyContent="center" my={2}>
          <CircularProgress size={24} /> <Typography sx={{ml:1}}>Processing...</Typography>
        </Box>
      )}

      {expensesPage && expensesPage.content && expensesPage.content.length > 0 ? (
        <>
          <TableContainer>
            <Table stickyHeader aria-label="my expenses table">
              <TableHead>
                <TableRow>
                  <TableCell sx={{ fontWeight: 'bold' }}>Date</TableCell>
                  <TableCell sx={{ fontWeight: 'bold' }}>Description</TableCell>
                  <TableCell sx={{ fontWeight: 'bold' }}>Category</TableCell>
                  <TableCell align="right" sx={{ fontWeight: 'bold' }}>Amount</TableCell>
                  <TableCell sx={{ fontWeight: 'bold' }}>Status</TableCell>
                  <TableCell align="center" sx={{ fontWeight: 'bold' }}>Actions</TableCell>
                </TableRow>
              </TableHead>
              <TableBody>
                {expensesPage.content.map((expense) => {
                  const canEditOrDelete = expense.status === 'SUBMITTED' || expense.status === 'REJECTED';
                  return (
                    <TableRow hover role="checkbox" tabIndex={-1} key={expense.id}>
                      <TableCell>
                        {/* Use parseISO because backend sends ISO string, then format it */}
                        {expense.expenseDate ? format(parseISO(expense.expenseDate), 'MMM dd, yyyy') : 'N/A'}
                      </TableCell>
                      <TableCell>{expense.description}</TableCell>
                      <TableCell>{expense.categoryName}</TableCell>
                      <TableCell align="right">${expense.amount.toFixed(2)}</TableCell>
                      <TableCell>
                        <Chip 
                            label={expense.status} 
                            size="small"
                            color={
                                expense.status === 'APPROVED' ? 'success' :
                                expense.status === 'REJECTED' ? 'error' :
                                expense.status === 'PENDING_FINANCE_APPROVAL' ? 'warning' :
                                'default' // For SUBMITTED etc.
                            }
                        />
                      </TableCell>
                      <TableCell align="center">
                        <Tooltip title="View Details">
                          <IconButton onClick={() => handleViewDetails(expense.id)} size="small" color="primary" disabled={actionLoading}>
                            <VisibilityIcon />
                          </IconButton>
                        </Tooltip>
                        {canEditOrDelete && (
                          <>
                            <Tooltip title="Edit Expense">
                              <span> {/* Span for Tooltip when IconButton is disabled */}
                                <IconButton onClick={() => handleEditExpense(expense.id)} size="small" color="secondary" disabled={actionLoading}>
                                  <EditIcon />
                                </IconButton>
                              </span>
                            </Tooltip>
                            <Tooltip title="Delete Expense">
                              <span> {/* Span for Tooltip when IconButton is disabled */}
                                <IconButton onClick={() => handleDeleteExpense(expense.id)} size="small" sx={{ color: 'error.main' }} disabled={actionLoading}>
                                  <DeleteIcon />
                                </IconButton>
                              </span>
                            </Tooltip>
                          </>
                        )}
                      </TableCell>
                    </TableRow>
                  );
                })}
              </TableBody>
            </Table>
          </TableContainer>
          <TablePagination
            rowsPerPageOptions={[5, 10, 25, 50]}
            component="div"
            count={expensesPage.totalElements}
            rowsPerPage={rowsPerPage}
            page={page}
            onPageChange={handleChangePage}
            onRowsPerPageChange={handleChangeRowsPerPage}
          />
        </>
      ) : (
        !loading && <Typography sx={{ textAlign: 'center', mt: 3 }}>No expenses found. Click "New Expense" to add one.</Typography>
      )}
    </Paper>
  );
};

export default MyExpensesPage;