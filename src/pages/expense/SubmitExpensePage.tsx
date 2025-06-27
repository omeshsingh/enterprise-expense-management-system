import React, { useState, useEffect, FormEvent, ChangeEvent } from 'react';
import { useNavigate } from 'react-router-dom';
import {
  TextField, Button, Typography, Paper, Box, CircularProgress, /* Alert, */ // Alert can be removed
  FormControl, InputLabel, Select, MenuItem, InputAdornment, Grid // Grid was imported twice, removed one
} from '@mui/material';
// import { Grid } from '@mui/material'; // Already imported above
import type { SelectChangeEvent } from '@mui/material';
import AttachFileIcon from '@mui/icons-material/AttachFile';
import { createExpense } from '../../services/expenseService';
import { getAllCategories } from '../../services/categoryService';
import type { ExpenseCategoryDto } from '../../types/categoryTypes';
import type { ExpenseRequestData } from '../../types/expenseTypes';
import { format } from 'date-fns'; // For setting default date
import { useSnackbar } from '../../contexts/SnackbarContext'; // Import useSnackbar

const SubmitExpensePage: React.FC = () => {
  const navigate = useNavigate();
  const { showSnackbar } = useSnackbar(); // Get the showSnackbar function
  const [description, setDescription] = useState<string>('');
  const [amount, setAmount] = useState<string>('');
  const [expenseDate, setExpenseDate] = useState<string>(format(new Date(), 'yyyy-MM-dd'));
  const [categoryId, setCategoryId] = useState<string>('');
  const [categories, setCategories] = useState<ExpenseCategoryDto[]>([]);
  const [files, setFiles] = useState<FileList | null>(null);
  const [categoriesLoading, setCategoriesLoading] = useState<boolean>(true);
  const [submitLoading, setSubmitLoading] = useState<boolean>(false);
  // const [error, setError] = useState<string | null>(null); // Replaced by snackbar
  // const [success, setSuccess] = useState<string | null>(null); // Replaced by snackbar

  useEffect(() => {
    const fetchCategories = async () => {
      setCategoriesLoading(true);
      try {
        const fetchedCategories = await getAllCategories();
        setCategories(fetchedCategories);
      } catch (err: any) {
        const errorMessage = err.response?.data?.message || err.message || 'Failed to load categories.';
        showSnackbar(errorMessage, 'error');
        console.error("Fetch categories error:", err);
      } finally {
        setCategoriesLoading(false);
      }
    };
    fetchCategories();
  }, [showSnackbar]); // Added showSnackbar to dependency array

  const handleFileChange = (event: ChangeEvent<HTMLInputElement>) => {
    setFiles(event.target.files);
  };

  const handleSubmit = async (event: FormEvent<HTMLFormElement>) => {
    event.preventDefault();
    // setError(null); // Not needed
    // setSuccess(null); // Not needed
    
    if (!description || !amount || !expenseDate || !categoryId) {
      showSnackbar('Please fill in all required fields.', 'warning');
      return;
    }
    if (parseFloat(amount) <= 0) {
      showSnackbar('Amount must be greater than zero.', 'warning');
      return;
    }

    setSubmitLoading(true); // Set loading state once here

    const expenseData: ExpenseRequestData = {
      description,
      amount: parseFloat(amount),
      expenseDate,
      categoryId: parseInt(categoryId, 10),
    };

    const filesArray = files ? Array.from(files) : [];

    try {
      await createExpense(expenseData, filesArray);
      showSnackbar('Expense submitted successfully! Redirecting...', 'success');
      // Reset form
      setDescription('');
      setAmount('');
      setExpenseDate(format(new Date(), 'yyyy-MM-dd'));
      setCategoryId('');
      setFiles(null);
      const fileInput = document.getElementById('expense-file-input') as HTMLInputElement;
      if (fileInput) fileInput.value = '';

      setTimeout(() => navigate('/dashboard/my-expenses'), 1500); // Slightly shorter delay
    } catch (err: any) {
      const errorMessage = err.response?.data?.message || err.message || 'Failed to submit expense.';
      showSnackbar(errorMessage, 'error');
      console.error("Submit expense error:", err);
    } finally {
      setSubmitLoading(false);
    }
  };

  if (categoriesLoading) {
    return <Box display="flex" justifyContent="center" alignItems="center" minHeight="200px"><CircularProgress /></Box>;
  }

  return (
    <Paper sx={{ p: 3, maxWidth: 700, margin: 'auto', mt: 2 }}>
      <Typography variant="h5" component="h1" gutterBottom sx={{ textAlign: 'center', mb: 3 }}>
        Submit New Expense
      </Typography>
      {/* Alert components are now replaced by the global Snackbar via showSnackbar */}
      {/* {error && <Alert severity="error" sx={{ mb: 2 }} onClose={() => setError(null)}>{error}</Alert>} */}
      {/* {success && <Alert severity="success" sx={{ mb: 2 }}>{success}</Alert>} */}
      <Box component="form" onSubmit={handleSubmit} noValidate>
        {/* Grid container had component="div" which is default, removed for brevity */}
        <Grid container spacing={2}>
          <Grid item xs={12}>
            <TextField
              label="Description"
              value={description}
              onChange={(e) => setDescription(e.target.value)}
              fullWidth
              required
              variant="outlined"
              disabled={submitLoading}
            />
          </Grid>
          <Grid item xs={12} sm={6}>
            <TextField
              label="Amount"
              type="number"
              value={amount}
              onChange={(e) => setAmount(e.target.value)}
              fullWidth
              required
              variant="outlined"
              InputProps={{
                startAdornment: <InputAdornment position="start">$</InputAdornment>,
                inputProps: { min: 0.01, step: "0.01" }
              }}
              disabled={submitLoading}
            />
          </Grid>
          <Grid item xs={12} sm={6}>
            <TextField
              label="Expense Date"
              type="date"
              value={expenseDate}
              onChange={(e) => setExpenseDate(e.target.value)}
              fullWidth
              required
              variant="outlined"
              InputLabelProps={{ shrink: true }}
              disabled={submitLoading}
            />
          </Grid>
          <Grid item xs={12}>
            <FormControl fullWidth variant="outlined" required disabled={submitLoading || categories.length === 0}>
              <InputLabel id="category-select-label">Category</InputLabel>
              <Select
                labelId="category-select-label"
                value={categoryId}
                label="Category"
                onChange={(e: SelectChangeEvent<string>) => setCategoryId(e.target.value)}
              >
                <MenuItem value="" disabled><em>Select a category</em></MenuItem>
                {categories.length > 0 ? (
                  categories.map((cat) => (
                    <MenuItem key={cat.id} value={cat.id.toString()}>
                      {cat.name}
                    </MenuItem>
                  ))
                ) : (
                  // Show this only if categories are not loading and still no categories
                  !categoriesLoading && <MenuItem disabled>No categories available</MenuItem>
                )}
              </Select>
            </FormControl>
          </Grid>
          <Grid item xs={12}>
            <Button
              variant="outlined"
              component="label"
              fullWidth
              startIcon={<AttachFileIcon />}
              disabled={submitLoading}
              sx={{textTransform: 'none', justifyContent: 'flex-start', color: 'text.secondary', borderColor: 'rgba(0, 0, 0, 0.23)'}}
            >
              {files && files.length > 0 ? `${files.length} file(s) selected` : 'Upload Attachments (Optional)'}
              <input
                id="expense-file-input"
                type="file"
                hidden
                multiple
                onChange={handleFileChange}
              />
            </Button>
            {files && files.length > 0 && (
              <Box mt={1} component="ul" sx={{ listStyleType: 'none', paddingLeft: 0 }}>
                {Array.from(files).map((file, index) => (
                  <Typography component="li" variant="caption" key={index} sx={{display: 'block'}}>{file.name}</Typography>
                ))}
              </Box>
            )}
          </Grid>
          <Grid item xs={12}>
            <Button
              type="submit"
              variant="contained"
              color="primary"
              fullWidth
              disabled={submitLoading}
              sx={{ mt: 2, py: 1.5 }}
              size="large"
            >
              {submitLoading ? <CircularProgress size={24} color="inherit" /> : 'Submit Expense'}
            </Button>
          </Grid>
        </Grid>
      </Box>
    </Paper>
  );
};

export default SubmitExpensePage;