import React, { useState, useEffect, useCallback } from 'react';
import type { FormEvent, ChangeEvent } from 'react';
import { useNavigate, useParams, Link as RouterLink } from 'react-router-dom';
import {
  TextField, Button, Typography, Paper, Box, Grid, CircularProgress, Alert,
  FormControl, InputLabel, Select, MenuItem, InputAdornment, List, ListItem, ListItemText, IconButton, ListItemIcon
} from '@mui/material';
import type { SelectChangeEvent } from '@mui/material';
import AttachFileIcon from '@mui/icons-material/AttachFile';
import DeleteIcon from '@mui/icons-material/Delete';
import ArrowBackIcon from '@mui/icons-material/ArrowBack';
import { getExpenseById, updateExpense } from '../../services/expenseService';
import { getAllCategories } from '../../services/categoryService';
import type { ExpenseCategoryDto } from '../../types/categoryTypes';
import type { ExpenseRequestData, ExpenseResponseDto, AttachmentResponseDto } from '../../types/expenseTypes';
import { format, parseISO } from 'date-fns';

const EditExpensePage: React.FC = () => {
  const { expenseId } = useParams<{ expenseId: string }>();
  const navigate = useNavigate();

  const [initialLoading, setInitialLoading] = useState<boolean>(true);
  const [description, setDescription] = useState<string>('');
  const [amount, setAmount] = useState<string>('');
  const [expenseDate, setExpenseDate] = useState<string>('');
  const [categoryId, setCategoryId] = useState<string>('');
  const [categories, setCategories] = useState<ExpenseCategoryDto[]>([]);
  const [existingAttachments, setExistingAttachments] = useState<AttachmentResponseDto[]>([]);
  const [newFiles, setNewFiles] = useState<FileList | null>(null);
  // Note: Deleting existing attachments is more complex.
  // For now, we'll only handle adding new ones.
  // To delete, you'd need a list of attachment IDs to remove and backend support.

  const [categoriesLoading, setCategoriesLoading] = useState<boolean>(true);
  const [submitLoading, setSubmitLoading] = useState<boolean>(false);
  const [error, setError] = useState<string | null>(null);
  const [success, setSuccess] = useState<string | null>(null);

  const fetchExpenseAndCategories = useCallback(async () => {
    if (!expenseId) {
      setError("Expense ID is missing.");
      setInitialLoading(false);
      return;
    }
    setInitialLoading(true);
    setError(null);
    try {
      const numericExpenseId = parseInt(expenseId, 10);
      const [fetchedExpense, fetchedCategories] = await Promise.all([
        getExpenseById(numericExpenseId),
        getAllCategories()
      ]);

      if (fetchedExpense.status !== 'SUBMITTED' && fetchedExpense.status !== 'REJECTED') {
        setError('This expense cannot be edited as it is not in a SUBMITTED or REJECTED state.');
        // Optionally navigate away or disable form
        // navigate('/dashboard/my-expenses');
        return;
      }

      setDescription(fetchedExpense.description);
      setAmount(fetchedExpense.amount.toString());
      setExpenseDate(format(parseISO(fetchedExpense.expenseDate), 'yyyy-MM-dd'));
      setCategoryId(fetchedExpense.categoryId.toString());
      setExistingAttachments(fetchedExpense.attachments || []);
      setCategories(fetchedCategories);

    } catch (err: any) {
      setError(err.response?.data?.message || err.message || 'Failed to load expense or categories.');
      console.error("Fetch data error:", err);
    } finally {
      setInitialLoading(false);
      setCategoriesLoading(false); // Categories are part of initial load
    }
  }, [expenseId]);

  useEffect(() => {
    fetchExpenseAndCategories();
  }, [fetchExpenseAndCategories]);


  const handleFileChange = (event: ChangeEvent<HTMLInputElement>) => {
    setNewFiles(event.target.files);
  };

  const handleSubmit = async (event: FormEvent<HTMLFormElement>) => {
    event.preventDefault();
    setError(null);
    setSuccess(null);
    if (!expenseId) return;

    if (!description || !amount || !expenseDate || !categoryId) {
      setError('Please fill in all required fields.');
      return;
    }
    if (parseFloat(amount) <= 0) {
        setError('Amount must be greater than zero.');
        return;
    }
    setSubmitLoading(true);

    const expenseData: ExpenseRequestData = {
      description,
      amount: parseFloat(amount),
      expenseDate,
      categoryId: parseInt(categoryId, 10),
    };

    const filesArray = newFiles ? Array.from(newFiles) : [];

    try {
      await updateExpense(parseInt(expenseId, 10), expenseData, filesArray);
      setSuccess('Expense updated successfully! Redirecting...');
      setTimeout(() => navigate('/dashboard/my-expenses'), 2000);
    } catch (err: any) {
      setError(err.response?.data?.message || err.message || 'Failed to update expense.');
      console.error("Update expense error:", err);
    } finally {
      setSubmitLoading(false);
    }
  };

  if (initialLoading) {
    return <Box display="flex" justifyContent="center" alignItems="center" minHeight="400px"><CircularProgress /></Box>;
  }
  if (error && !categoriesLoading) { // Show error if not related to initial category load
    return <Alert severity="error" sx={{ m: 2 }}>{error}</Alert>;
  }


  return (
    <Paper sx={{ p: 3, maxWidth: 700, margin: 'auto', mt: 2 }}>
       <Button startIcon={<ArrowBackIcon />} component={RouterLink} to="/dashboard/my-expenses" sx={{ mb: 2 }}>
          Back to My Expenses
        </Button>
      <Typography variant="h5" component="h1" gutterBottom sx={{ textAlign: 'center', mb: 3 }}>
        Edit Expense
      </Typography>
      {error && <Alert severity="error" sx={{ mb: 2 }} onClose={() => setError(null)}>{error}</Alert>}
      {success && <Alert severity="success" sx={{ mb: 2 }}>{success}</Alert>}

      <Box component="form" onSubmit={handleSubmit} noValidate>
        <Grid container spacing={2}>
          {/* Form fields similar to SubmitExpensePage, pre-filled */}
          <Grid item xs={12}>
            <TextField label="Description" value={description} onChange={(e) => setDescription(e.target.value)} fullWidth required variant="outlined" disabled={submitLoading} />
          </Grid>
          <Grid item xs={12} sm={6}>
            <TextField label="Amount" type="number" value={amount} onChange={(e) => setAmount(e.target.value)} fullWidth required variant="outlined" InputProps={{ startAdornment: <InputAdornment position="start">$</InputAdornment>, inputProps: { min: 0.01, step: "0.01" } }} disabled={submitLoading} />
          </Grid>
          <Grid item xs={12} sm={6}>
            <TextField label="Expense Date" type="date" value={expenseDate} onChange={(e) => setExpenseDate(e.target.value)} fullWidth required variant="outlined" InputLabelProps={{ shrink: true }} disabled={submitLoading} />
          </Grid>
          <Grid item xs={12}>
            <FormControl fullWidth variant="outlined" required disabled={submitLoading || categoriesLoading || categories.length === 0}>
              <InputLabel id="category-select-label-edit">Category</InputLabel>
              <Select labelId="category-select-label-edit" value={categoryId} label="Category" onChange={(e: SelectChangeEvent<string>) => setCategoryId(e.target.value)}>
                <MenuItem value="" disabled><em>Select a category</em></MenuItem>
                {categories.length > 0 ? (
                  categories.map((cat) => (<MenuItem key={cat.id} value={cat.id.toString()}>{cat.name}</MenuItem>))
                ) : (<MenuItem disabled>Loading categories...</MenuItem>)}
              </Select>
            </FormControl>
          </Grid>

          {/* Existing Attachments - Deletion not implemented in this simple version */}
          {existingAttachments.length > 0 && (
            <Grid item xs={12}>
              <Typography variant="subtitle1" gutterBottom>Existing Attachments:</Typography>
              <List dense>
                {existingAttachments.map(att => (
                  <ListItem key={att.id} secondaryAction={
                    <IconButton edge="end" aria-label="delete-existing" disabled /*onClick={() => handleRemoveExistingAttachment(att.id)}*/>
                      <DeleteIcon />
                    </IconButton>
                  }>
                    <ListItemIcon><AttachFileIcon /></ListItemIcon>
                    <ListItemText primary={att.fileName} secondary={att.fileType} />
                  </ListItem>
                ))}
              </List>
              <Typography variant="caption" color="text.secondary">Note: Removing existing attachments is not supported in this simplified edit form. To change attachments, please submit new ones below; old ones will remain.</Typography>
            </Grid>
          )}

          <Grid item xs={12}>
             <Typography variant="subtitle1" gutterBottom>Add New Attachments (Optional)</Typography>
            <Button variant="outlined" component="label" fullWidth startIcon={<AttachFileIcon />} disabled={submitLoading} sx={{textTransform: 'none', justifyContent: 'flex-start', color: 'text.secondary', borderColor: 'rgba(0, 0, 0, 0.23)'}}>
              {newFiles && newFiles.length > 0 ? `${newFiles.length} new file(s) selected` : 'Upload New Attachments'}
              <input type="file" hidden multiple onChange={handleFileChange} />
            </Button>
            {newFiles && newFiles.length > 0 && (
              <Box mt={1} component="ul" sx={{ listStyleType: 'none', paddingLeft: 0 }}>
                {Array.from(newFiles).map((file, index) => (
                  <Typography component="li" variant="caption" key={index} sx={{display: 'block'}}>{file.name}</Typography>
                ))}
              </Box>
            )}
          </Grid>

          <Grid item xs={12}>
            <Button type="submit" variant="contained" color="primary" fullWidth disabled={submitLoading} sx={{ mt: 2, py: 1.5 }} size="large">
              {submitLoading ? <CircularProgress size={24} color="inherit" /> : 'Update Expense'}
            </Button>
          </Grid>
        </Grid>
      </Box>
    </Paper>
  );
};

export default EditExpensePage;