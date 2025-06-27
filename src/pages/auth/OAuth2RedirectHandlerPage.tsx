import React, { useEffect } from 'react';
import { useSearchParams, useNavigate } from 'react-router-dom';
import { useAuth } from '../../contexts/AuthContext';
import { Box, CircularProgress, Typography } from '@mui/material';

const OAuth2RedirectHandlerPage: React.FC = () => {
  const [searchParams] = useSearchParams();
  const navigate = useNavigate();
  const { decodeAndSetUser } = useAuth(); // Assuming decodeAndSetUser is exposed or similar login function

  useEffect(() => {
    const token = searchParams.get('token');
    const error = searchParams.get('error');

    if (token) {
      // You might have a specific loginWithToken function in AuthContext
      // or reuse parts of your existing login logic to store this token.
      // For now, let's assume decodeAndSetUser can handle it.
      decodeAndSetUser(token);
      navigate('/dashboard', { replace: true });
    } else if (error) {
      console.error("OAuth2 Error:", error);
      // Navigate to login page with an error message
      navigate('/login?error=oauth_failed', { replace: true });
    } else {
      // No token or error, something went wrong or direct access
      navigate('/login', { replace: true });
    }
  }, [searchParams, navigate, decodeAndSetUser]);

  return (
    <Box display="flex" flexDirection="column" justifyContent="center" alignItems="center" height="100vh">
      <CircularProgress />
      <Typography sx={{mt: 2}}>Processing authentication...</Typography>
    </Box>
  );
};

export default OAuth2RedirectHandlerPage;