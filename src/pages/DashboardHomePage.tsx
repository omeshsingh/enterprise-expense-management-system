import React, { useEffect, useState, useCallback } from 'react';
import { Typography, Paper, Box, Grid, Card, CardContent, CircularProgress, Alert } from '@mui/material';
import { useAuth } from '../contexts/AuthContext';
import { getMyExpenses } from '../services/expenseService'; // Assuming this fetches all for summary
import type { ExpenseResponseDto } from '../types/expenseTypes';
import SummarizeIcon from '@mui/icons-material/Summarize';
import HourglassEmptyIcon from '@mui/icons-material/HourglassEmpty';
import CheckCircleOutlineIcon from '@mui/icons-material/CheckCircleOutline';
import CancelOutlinedIcon from '@mui/icons-material/CancelOutlined';

const DashboardHomePage: React.FC = () => {
  const { user } = useAuth();
  const [summary, setSummary] = useState<{ submitted: number; approved: number; rejected: number; total: number } | null>(null);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState<string | null>(null);

  const fetchSummaryData = useCallback(async () => {
    setLoading(true);
    setError(null);
    try {
      // Fetch all expenses for the user to calculate summary.
      // getMyExpenses fetches paginated, so we might need a different endpoint
      // or fetch all pages (not ideal for large datasets).
      // For simplicity, let's assume we fetch a large enough size for now.
      // A dedicated backend summary endpoint would be better for performance.
      const data = await getMyExpenses(0, 100); // Fetch up to 100 of their most recent expenses
      
      let submitted = 0;
      let approved = 0;
      let rejected = 0;
      data.content.forEach((exp: ExpenseResponseDto) => {
        if (exp.status === 'SUBMITTED' || exp.status === 'PENDING_FINANCE_APPROVAL') submitted++;
        else if (exp.status === 'APPROVED') approved++;
        else if (exp.status === 'REJECTED') rejected++;
      });
      setSummary({ submitted, approved, rejected, total: data.totalElements });
    } catch (err: any) {
      setError(err.response?.data?.message || err.message || "Failed to load summary data.");
      console.error("Summary fetch error:", err);
    } finally {
      setLoading(false);
    }
  }, []);

  useEffect(() => {
    if (user) { // Only fetch if user is available
      fetchSummaryData();
    } else {
      setLoading(false); // No user, no data to fetch
    }
  }, [user, fetchSummaryData]);

  return (
    <Paper elevation={0} sx={{ p: {xs: 1, md: 2} }}>
      <Typography variant="h4" gutterBottom>
        Welcome, {user?.username || 'User'}!
      </Typography>
      <Typography variant="body1" sx={{mb: 3}}>
        This is your expense management dashboard.
      </Typography>

      {loading && <Box sx={{display: 'flex', justifyContent: 'center', my:3}}><CircularProgress /></Box>}
      {error && <Alert severity="error" sx={{my:2}}>{error}</Alert>}
      
      {summary && !loading && (
        <Grid container spacing={3}>
          <Grid item xs={12} sm={6} md={3}>
            <Card sx={{bgcolor: 'primary.light', color: 'primary.contrastText'}}>
              <CardContent>
                <HourglassEmptyIcon sx={{ fontSize: 40, float: 'right', opacity: 0.7 }}/>
                <Typography variant="h6">{summary.submitted}</Typography>
                <Typography>Pending Approval</Typography>
              </CardContent>
            </Card>
          </Grid>
          <Grid item xs={12} sm={6} md={3}>
            <Card sx={{bgcolor: 'success.light', color: 'success.contrastText'}}>
              <CardContent>
                <CheckCircleOutlineIcon sx={{ fontSize: 40, float: 'right', opacity: 0.7 }}/>
                <Typography variant="h6">{summary.approved}</Typography>
                <Typography>Approved</Typography>
              </CardContent>
            </Card>
          </Grid>
          <Grid item xs={12} sm={6} md={3}>
             <Card sx={{bgcolor: 'error.light', color: 'error.contrastText'}}>
              <CardContent>
                <CancelOutlinedIcon sx={{ fontSize: 40, float: 'right', opacity: 0.7 }}/>
                <Typography variant="h6">{summary.rejected}</Typography>
                <Typography>Rejected</Typography>
              </CardContent>
            </Card>
          </Grid>
          <Grid item xs={12} sm={6} md={3}>
            <Card>
              <CardContent>
                <SummarizeIcon sx={{ fontSize: 40, float: 'right', opacity: 0.7, color: 'action.active' }}/>
                <Typography variant="h6">{summary.total}</Typography>
                <Typography>Total Expenses</Typography>
              </CardContent>
            </Card>
          </Grid>
        </Grid>
      )}
    </Paper>
  );
};
export default DashboardHomePage;