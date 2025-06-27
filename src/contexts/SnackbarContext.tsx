import React, { createContext, useContext, useState, useCallback } from 'react';
import type { ReactNode } from 'react';
import { Snackbar, Alert } from '@mui/material';
import type { AlertColor } from '@mui/material';

interface SnackbarMessage {
  message: string;
  severity: AlertColor; // 'success' | 'error' | 'warning' | 'info'
}

interface SnackbarContextType {
  showSnackbar: (message: string, severity: AlertColor) => void;
}

const SnackbarContext = createContext<SnackbarContextType | undefined>(undefined);

export const SnackbarProvider: React.FC<{ children: ReactNode }> = ({ children }) => {
  const [open, setOpen] = useState(false);
  const [snackbarConfig, setSnackbarConfig] = useState<SnackbarMessage | null>(null);

  const showSnackbar = useCallback((message: string, severity: AlertColor) => {
    setSnackbarConfig({ message, severity });
    setOpen(true);
  }, []);

  const handleClose = (event?: React.SyntheticEvent | Event, reason?: string) => {
    if (reason === 'clickaway') {
      return;
    }
    setOpen(false);
  };

  return (
    <SnackbarContext.Provider value={{ showSnackbar }}>
      {children}
      {snackbarConfig && (
        <Snackbar
          open={open}
          autoHideDuration={4000} // Hide after 4 seconds
          onClose={handleClose}
          anchorOrigin={{ vertical: 'bottom', horizontal: 'center' }}
        >
          <Alert onClose={handleClose} severity={snackbarConfig.severity} sx={{ width: '100%' }} variant="filled">
            {snackbarConfig.message}
          </Alert>
        </Snackbar>
      )}
    </SnackbarContext.Provider>
  );
};

export const useSnackbar = () => {
  const context = useContext(SnackbarContext);
  if (context === undefined) {
    throw new Error('useSnackbar must be used within a SnackbarProvider');
  }
  return context;
};