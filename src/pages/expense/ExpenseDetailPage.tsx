import React, { useEffect, useState, useCallback } from 'react';
import { useParams, useNavigate, Link as RouterLink } from 'react-router-dom';
import {
  Typography, Paper, Box, CircularProgress, Alert, Grid, Chip, List,
  ListItem, ListItemText, ListItemIcon, Divider, Button, Link
} from '@mui/material';
import AttachFileIcon from '@mui/icons-material/AttachFile';
import HistoryIcon from '@mui/icons-material/History';
import EditIcon from '@mui/icons-material/Edit';
import ArrowBackIcon from '@mui/icons-material/ArrowBack';
import { getExpenseById, getExpenseApprovalHistory } from '../../services/expenseService';
import type { ExpenseResponseDto } from '../../types/expenseTypes';
import type { ApprovalHistoryDto } from '../../types/approvalTypes';
import { format, parseISO } from 'date-fns';
import apiClient from '../../services/api'; // For constructing download URL

const ExpenseDetailPage: React.FC = () => {
  const { expenseId } = useParams<{ expenseId: string }>();
  const navigate = useNavigate();
  const [expense, setExpense] = useState<ExpenseResponseDto | null>(null);
  const [history, setHistory] = useState<ApprovalHistoryDto[]>([]);
  const [loading, setLoading] = useState<boolean>(true);
  const [error, setError] = useState<string | null>(null);

  const fetchExpenseDetails = useCallback(async () => {
    if (!expenseId) {
      setError("Expense ID is missing.");
      setLoading(false);
      return;
    }
    setLoading(true);
    setError(null);
    try {
      const numericExpenseId = parseInt(expenseId, 10);
      const expenseData = await getExpenseById(numericExpenseId);
      setExpense(expenseData);
      const historyData = await getExpenseApprovalHistory(numericExpenseId);
      setHistory(historyData);
    } catch (err: any) {
      setError(err.response?.data?.message || err.message || 'Failed to fetch expense details.');
      console.error("Fetch detail error:", err);
    } finally {
      setLoading(false);
    }
  }, [expenseId]);

  useEffect(() => {
    fetchExpenseDetails();
  }, [fetchExpenseDetails]);

  const getAttachmentDownloadUrl = (attachmentId: number): string => {
    return `${apiClient.defaults.baseURL}/expenses/attachments/${attachmentId}/download`;
  };

  const canEditOrDelete = expense?.status === 'SUBMITTED' || expense?.status === 'REJECTED';

  if (loading) {
    return <Box display="flex" justifyContent="center" alignItems="center" minHeight="400px"><CircularProgress /></Box>;
  }

  if (error) {
    return <Alert severity="error" sx={{ m: 2 }}>{error}</Alert>;
  }

  if (!expense) {
    return <Alert severity="warning" sx={{ m: 2 }}>Expense not found.</Alert>;
  }

  return (
    <Paper sx={{ p: 3, mt: 2 }}>
      <Box sx={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', mb: 2 }}>
        <Button startIcon={<ArrowBackIcon />} onClick={() => navigate(-1)}>
          Back to My Expenses
        </Button>
        {canEditOrDelete && (
          <Button
            variant="outlined"
            startIcon={<EditIcon />}
            component={RouterLink}
            to={`/dashboard/expenses/${expense.id}/edit`} // Link to Edit Page
          >
            Edit Expense
          </Button>
        )}
      </Box>
      <Typography variant="h4" gutterBottom component="div">
        Expense Details: {expense.description}
      </Typography>
      <Divider sx={{ my: 2 }} />

      <Grid container spacing={2}>
        <Grid item xs={12} md={6}>
          <Typography variant="subtitle1" color="text.secondary">Amount:</Typography>
          <Typography variant="h6">${expense.amount.toFixed(2)}</Typography>
        </Grid>
        <Grid item xs={12} md={6}>
          <Typography variant="subtitle1" color="text.secondary">Status:</Typography>
          <Chip label={expense.status} color={expense.status === 'APPROVED' ? 'success' : expense.status === 'REJECTED' ? 'error' : 'default'} />
        </Grid>
        <Grid item xs={12} md={6}>
          <Typography variant="subtitle1" color="text.secondary">Expense Date:</Typography>
          <Typography variant="body1">{format(parseISO(expense.expenseDate), 'MMMM dd, yyyy')}</Typography>
        </Grid>
        <Grid item xs={12} md={6}>
          <Typography variant="subtitle1" color="text.secondary">Category:</Typography>
          <Typography variant="body1">{expense.categoryName}</Typography>
        </Grid>
        <Grid item xs={12} md={6}>
          <Typography variant="subtitle1" color="text.secondary">Submitted By:</Typography>
          <Typography variant="body1">{expense.username}</Typography>
        </Grid>
        <Grid item xs={12} md={6}>
          <Typography variant="subtitle1" color="text.secondary">Submitted On:</Typography>
          <Typography variant="body1">{format(parseISO(expense.createdAt), 'MMMM dd, yyyy, h:mm a')}</Typography>
        </Grid>
      </Grid>

      {expense.attachments && expense.attachments.length > 0 && (
        <Box sx={{ mt: 3 }}>
          <Typography variant="h6" gutterBottom><AttachFileIcon sx={{verticalAlign: 'bottom', mr:1}}/> Attachments</Typography>
          <List dense>
            {expense.attachments.map(att => (
              <ListItem key={att.id}
                secondaryAction={
                  <Button
                    variant="outlined"
                    size="small"
                    href={getAttachmentDownloadUrl(att.id)}
                    target="_blank" // Open in new tab, browser handles download
                    // download // Suggests download, but target="_blank" is often enough for backend to force it
                  >
                    Download
                  </Button>
                }
              >
                <ListItemText primary={att.fileName} secondary={att.fileType} />
              </ListItem>
            ))}
          </List>
        </Box>
      )}

      <Box sx={{ mt: 4 }}>
        <Typography variant="h6" gutterBottom><HistoryIcon sx={{verticalAlign: 'bottom', mr:1}}/> Approval History</Typography>
        {history.length > 0 ? (
          <List dense>
            {history.map(h => (
              <ListItem key={h.id} sx={{borderBottom: '1px solid #eee', py: 1.5}}>
                <ListItemText
                  primary={
                    <Typography variant="body2">
                      Status changed to <strong>{h.statusAfter}</strong> by <strong>{h.approverUsername}</strong>
                    </Typography>
                  }
                  secondary={
                    <>
                      <Typography component="span" variant="caption" color="text.secondary">
                        {format(parseISO(h.actionDate), 'MMM dd, yyyy, h:mm a')}
                      </Typography>
                      {h.comments && (
                        <Typography component="span" variant="caption" sx={{ display: 'block', fontStyle: 'italic', mt: 0.5 }}>
                          Comment: {h.comments}
                        </Typography>
                      )}
                      {h.statusBefore && (
                         <Typography component="span" variant="caption" color="text.secondary" sx={{display: 'block'}}>
                            (Previous Status: {h.statusBefore})
                        </Typography>
                      )}
                    </>
                  }
                />
              </ListItem>
            ))}
          </List>
        ) : (
          <Typography>No approval history available for this expense.</Typography>
        )}
      </Box>
    </Paper>
  );
};

export default ExpenseDetailPage;