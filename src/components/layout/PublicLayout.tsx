import React from 'react';
import { Outlet } from 'react-router-dom';
import { Container, Box, Paper } from '@mui/material';

const PublicLayout: React.FC = () => {
  return (
    <Container component="main" maxWidth="xs">
      <Paper elevation={3} sx={{ marginTop: 8, padding: 4, display: 'flex', flexDirection: 'column', alignItems: 'center' }}>
        <Outlet />
      </Paper>
    </Container>
  );
};
export default PublicLayout;