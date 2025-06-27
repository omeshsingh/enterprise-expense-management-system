import React from 'react';
import { BrowserRouter as Router, Routes, Route, Navigate } from 'react-router-dom';
import { useAuth } from './contexts/AuthContext'; // Assuming your AuthContext

// Layout Components
import PublicLayout from './components/layout/PublicLayout';
import PrivateLayout from './components/layout/PrivateLayout';
import ProtectedRoute from './components/common/ProtectedRoute';

// Page Components
import LoginPage from './pages/auth/LoginPage';
import RegisterPage from './pages/auth/RegisterPage';
import OAuth2RedirectHandlerPage from './pages/auth/OAuth2RedirectHandlerPage'; // <<<< IMPORT THIS
import DashboardHomePage from './pages/DashboardHomePage';
import MyExpensesPage from './pages/expense/MyExpensesPage';
import SubmitExpensePage from './pages/expense/SubmitExpensePage';
import UserProfilePage from './pages/user/UserProfilePage';
import ExpenseDetailPage from './pages/expense/ExpenseDetailPage';
import EditExpensePage from './pages/expense/EditExpensePage';
import PendingApprovalsPage from './pages/manager/PendingApprovalsPage'; 

import { Box, CircularProgress } from '@mui/material';

const App: React.FC = () => {
  const { isAuthenticated, isLoading } = useAuth();

  if (isLoading) {
    return (
      <Box display="flex" justifyContent="center" alignItems="center" height="100vh">
        <CircularProgress size={60} />
      </Box>
    );
  }

  return (
    // <Router>
      <Routes>
        {/* Public Routes: Login, Register, and OAuth2 Redirect Handler */}
        <Route element={<PublicLayout />}>
          <Route
            path="/login"
            element={isAuthenticated ? <Navigate to="/dashboard" replace /> : <LoginPage />}
          />
          <Route
            path="/register"
            element={isAuthenticated ? <Navigate to="/dashboard" replace /> : <RegisterPage />}
          />
          {/* 
            The OAuth2RedirectHandlerPage often doesn't need the PublicLayout, 
            as it's just a processing page. You can place it outside or keep it if layout is minimal.
            For simplicity, keeping it here. If it causes layout issues, move it to be a direct child of <Routes>.
          */}
        </Route>
        
        {/* OAuth2 Redirect Handler - Should be a top-level accessible route */}
        {/* It might not even need the PublicLayout if it's purely functional */}
        <Route path="/oauth2/redirect" element={<OAuth2RedirectHandlerPage />} /> {/* <<<< MOVED HERE */}


        {/* Protected Routes: Dashboard and its children */}
        <Route element={<ProtectedRoute />}>
          <Route path="/dashboard" element={<PrivateLayout />}>
            <Route index element={<DashboardHomePage />} />
            <Route path="my-expenses" element={<MyExpensesPage />} />
            <Route path="submit-expense" element={<SubmitExpensePage />} />
            <Route path="profile" element={<UserProfilePage />} />
            <Route path="expenses/:expenseId" element={<ExpenseDetailPage />} />
            <Route path="expenses/:expenseId/edit" element={<EditExpensePage />} />
            <Route path="pending-approvals" element={<PendingApprovalsPage />} />
          </Route>
        </Route>

        {/* Fallback Routes */}
        <Route
          path="*"
          element={<Navigate to={isAuthenticated ? "/dashboard" : "/login"} replace />}
        />
        <Route
          path="/"
          element={<Navigate to={isAuthenticated ? "/dashboard" : "/login"} replace />}
        />
      </Routes>
    //* </Router> */
  );
};

export default App;