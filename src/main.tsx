import React from 'react';
import ReactDOM from 'react-dom/client';
import App from './App.tsx';
import { ThemeProvider } from '@mui/material/styles';
import CssBaseline from '@mui/material/CssBaseline';
import theme from './styles/theme.ts';
import { AuthProvider } from './contexts/AuthContext.tsx';
import { BrowserRouter } from 'react-router-dom'; // <<<< IMPORT BrowserRouter
import { SnackbarProvider } from './contexts/SnackbarContext.tsx';

ReactDOM.createRoot(document.getElementById('root')!).render(
  <React.StrictMode>
    <ThemeProvider theme={theme}>
      <CssBaseline />
      <BrowserRouter> {/* <<<< WRAP AuthProvider (and App) with BrowserRouter */}
        <AuthProvider>
        <SnackbarProvider> {/* <<<< ENSURE THIS WRAPS APP */}
            <App />
          </SnackbarProvider>
        </AuthProvider>
      </BrowserRouter>
    </ThemeProvider>
  </React.StrictMode>,
);