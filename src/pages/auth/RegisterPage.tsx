import React, { useState } from 'react';
import { useNavigate, Link as RouterLink } from 'react-router-dom';
import { Avatar, Button, TextField, Typography, Box, Alert, Grid, Link } from '@mui/material';
import AppRegistrationIcon from '@mui/icons-material/AppRegistration';
import { useAuth } from '../../contexts/AuthContext';

const RegisterPage: React.FC = () => {
  const navigate = useNavigate();
  const { registerUser, isLoading } = useAuth();
  const [username, setUsername] = useState('');
  const [email, setEmail] = useState('');
  const [password, setPassword] = useState('');
  const [firstName, setFirstName] = useState('');
  const [lastName, setLastName] = useState('');
  const [error, setError] = useState<string | null>(null);
  const [success, setSuccess] = useState<string | null>(null);

  const handleSubmit = async (event: React.FormEvent<HTMLFormElement>) => {
    event.preventDefault();
    setError(null);
    setSuccess(null);
    try {
      const message = await registerUser({ username, email, password, firstName, lastName });
      setSuccess(message + " Redirecting to login...");
      setTimeout(() => navigate('/login'), 2000);
    } catch (err: any) {
      setError(err.response?.data?.message || err.message || 'Registration failed.');
    }
  };

  return (
    <>
      <Avatar sx={{ m: 1, bgcolor: 'primary.main' }}>
        <AppRegistrationIcon />
      </Avatar>
      <Typography component="h1" variant="h5">
        Sign Up
      </Typography>
      {error && <Alert severity="error" sx={{ width: '100%', mt: 2 }}>{error}</Alert>}
      {success && <Alert severity="success" sx={{ width: '100%', mt: 2 }}>{success}</Alert>}
      <Box component="form" onSubmit={handleSubmit} noValidate sx={{ mt: 1 }}>
        <TextField margin="normal" required fullWidth label="Username" value={username} onChange={(e) => setUsername(e.target.value)} disabled={isLoading}/>
        <TextField margin="normal" required fullWidth label="Email Address" type="email" value={email} onChange={(e) => setEmail(e.target.value)} disabled={isLoading}/>
        <TextField margin="normal" required fullWidth label="Password" type="password" value={password} onChange={(e) => setPassword(e.target.value)} disabled={isLoading}/>
        <TextField margin="normal" fullWidth label="First Name" value={firstName} onChange={(e) => setFirstName(e.target.value)} disabled={isLoading}/>
        <TextField margin="normal" fullWidth label="Last Name" value={lastName} onChange={(e) => setLastName(e.target.value)} disabled={isLoading}/>
        <Button type="submit" fullWidth variant="contained" sx={{ mt: 3, mb: 2 }} disabled={isLoading}>
          {isLoading ? 'Signing Up...' : 'Sign Up'}
        </Button>
        <Grid container justifyContent="flex-end">
          <Grid item>
            <Link component={RouterLink} to="/login" variant="body2">
              Already have an account? Sign in
            </Link>
          </Grid>
        </Grid>
      </Box>
    </>
  );
};
export default RegisterPage;