import React, { useState, useEffect, useCallback } from 'react';
import { useNavigate, Link as RouterLink } from 'react-router-dom'; // Import React Router's Link as RouterLink
import {
  Avatar,
  Button,
  TextField,
  Typography,
  Box,
  Alert,
  Link as MUILink, // Import Material UI's Link as MUILink to avoid name clash if needed, or just Link
  Divider,
  // IconButton // Not used in the provided Google Login example, but keep if needed elsewhere
} from '@mui/material';
import LockOutlinedIcon from '@mui/icons-material/LockOutlined';
import GoogleIcon from '@mui/icons-material/Google';
import { useAuth } from '../../contexts/AuthContext';
import Grid from '@mui/material/Grid';

// Get the backend OAuth2 login URL from environment variable or hardcode
const GOOGLE_AUTH_URL_BACKEND = `${import.meta.env.VITE_API_BASE_URL || 'http://localhost:8080'}/oauth2/authorization/google`;

const LoginPage: React.FC = () => {
  const navigate = useNavigate();
  const { loginUser, isLoading, handleOAuth2Success } = useAuth(); // Use handleOAuth2Success from context
  const [username, setUsername] = useState('');
  const [password, setPassword] = useState('');
  const [error, setError] = useState<string | null>(null);
  let oauthPopup: Window | null = null;

  const handleAuthMessage = useCallback((event: MessageEvent) => {
    // For simplicity, assuming messages are trusted if they have the correct type for now.
    // In production, you'd add stricter origin checks based on how your backend sends the message.
    // const expectedOrigin = new URL(import.meta.env.VITE_API_BASE_URL || 'http://localhost:8080').origin;
    // if (event.origin !== expectedOrigin) {
    //   console.warn("Message received from unexpected origin:", event.origin);
    //   return;
    // }

    if (event.data && event.data.type === 'oauth2_success' && event.data.token) {
      console.log("OAuth2 token received from popup:", event.data.token);
      handleOAuth2Success(event.data.token); // Process token via AuthContext
      navigate('/dashboard', { replace: true });
      if (oauthPopup && !oauthPopup.closed) {
        oauthPopup.close();
      }
    } else if (event.data && event.data.type === 'oauth2_error') {
      setError(event.data.message || "Google login failed.");
      if (oauthPopup && !oauthPopup.closed) {
        oauthPopup.close();
      }
    }
  }, [navigate, handleOAuth2Success, oauthPopup]); // Ensure all dependencies are listed

  useEffect(() => {
    window.addEventListener('message', handleAuthMessage);
    return () => {
      window.removeEventListener('message', handleAuthMessage);
      if (oauthPopup && !oauthPopup.closed) {
        oauthPopup.close();
      }
    };
  }, [handleAuthMessage, oauthPopup]); // Ensure all dependencies are listed

  const handleFormSubmit = async (event: React.FormEvent<HTMLFormElement>) => {
    event.preventDefault();
    setError(null);
    try {
      await loginUser({ username, password });
      navigate('/dashboard');
    } catch (err: any) {
      setError(err.response?.data?.message || err.message || 'Login failed. Please check credentials.');
    }
  };

  const handleGoogleLogin = () => {
    const width = 600, height = 700;
    const left = (window.innerWidth / 2) - (width / 2);
    const top = (window.innerHeight / 2) - (height / 2);

    if (oauthPopup && !oauthPopup.closed) {
      oauthPopup.close();
    }
    oauthPopup = window.open(
      GOOGLE_AUTH_URL_BACKEND,
      'GoogleAuthLogin',
      `width=${width},height=${height},top=${top},left=${left},toolbar=no,location=no,status=no,menubar=no,scrollbars=yes,resizable=yes`
    );
    if (!oauthPopup) {
      setError("Popup blocked. Please allow popups for this site to use Google login.");
    }
  };

  return (
    <>
      <Avatar sx={{ m: 1, bgcolor: 'secondary.main' }}>
        <LockOutlinedIcon />
      </Avatar>
      <Typography component="h1" variant="h5">
        Sign in
      </Typography>
      {error && <Alert severity="error" sx={{ width: '100%', mt: 2 }} onClose={() => setError(null)}>{error}</Alert>}
      
      <Button
        fullWidth
        variant="outlined"
        color="primary"
        startIcon={<GoogleIcon />}
        onClick={handleGoogleLogin}
        sx={{ mt: 3, mb: 2, textTransform: 'none' }}
        disabled={isLoading}
      >
        Sign in with Google
      </Button>

      <Divider sx={{ width: '100%', my: 2 }}>OR</Divider>

      <Box component="form" onSubmit={handleFormSubmit} noValidate sx={{ mt: 1 }}>
        <TextField
          margin="normal"
          required
          fullWidth
          id="username"
          label="Username"
          name="username"
          autoComplete="username"
          autoFocus
          value={username}
          onChange={(e) => setUsername(e.target.value)}
          disabled={isLoading}
        />
        <TextField
          margin="normal"
          required
          fullWidth
          name="password"
          label="Password"
          type="password"
          id="password"
          autoComplete="current-password"
          value={password}
          onChange={(e) => setPassword(e.target.value)}
          disabled={isLoading}
        />
        <Button
          type="submit"
          fullWidth
          variant="contained"
          sx={{ mt: 3, mb: 2 }}
          disabled={isLoading}
        >
          {isLoading ? 'Signing In...' : 'Sign In with Username'}
        </Button>
        <Grid container justifyContent="flex-end"> {/* Adjusted for typical layout */}
          <Grid item>
            {/* Using MUI Link component with react-router-dom's Link for navigation */}
            <MUILink component={RouterLink} to="/register" variant="body2">
              {"Don't have an account? Sign Up"}
            </MUILink>
          </Grid>
        </Grid>
      </Box>
    </>
  );
};

export default LoginPage;