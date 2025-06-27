import React, { useState, useEffect } from 'react';
import type { FormEvent } from 'react';
import { Paper, Typography, Box, Grid, Avatar, Divider, TextField, Button, CircularProgress } from '@mui/material';
import AccountCircleIcon from '@mui/icons-material/AccountCircle';
import EditIcon from '@mui/icons-material/Edit';
import SaveIcon from '@mui/icons-material/Save';
import { useAuth } from '../../contexts/AuthContext';
import { getCurrentUserProfile, updateUserProfile } from '../../services/userService';
import type { UserUpdateRequest } from '../../services/userService';
import type { User as AuthUserType } from '../../types/authTypes';
import { useSnackbar } from '../../contexts/SnackbarContext';

const UserProfilePage: React.FC = () => {
  const { user: authUser, isLoading: authLoading, token } = useAuth();
  const { showSnackbar } = useSnackbar();

  const [profileUser, setProfileUser] = useState<AuthUserType | null>(null);
  const [isEditing, setIsEditing] = useState(false);
  const [firstName, setFirstName] = useState('');
  const [lastName, setLastName] = useState('');
  const [loading, setLoading] = useState(true); // Page specific loading
  const [saving, setSaving] = useState(false);

  useEffect(() => {
    const fetchProfile = async () => {
      if (authUser) { // If user data already in auth context, use it initially
        setProfileUser(authUser);
        setFirstName(authUser.firstName || ''); // Assuming firstName, lastName are in User type from AuthContext
        setLastName(authUser.lastName || '');
        setLoading(false);
        // Optionally, always fetch fresh data from /users/me
        // try {
        //   const freshUser = await getCurrentUserProfile();
        //   setProfileUser(freshUser);
        //   setFirstName(freshUser.firstName || '');
        //   setLastName(freshUser.lastName || '');
        // } catch (error) {
        //   console.error("Failed to fetch fresh profile", error);
        //   showSnackbar('Failed to load latest profile data.', 'error');
        // }
      } else if (!authLoading && !authUser) { // If auth is done loading and no user
        showSnackbar('User not authenticated.', 'error');
        setLoading(false);
      }
      // If authUser is null but authLoading is true, we wait
    };
    fetchProfile();
  }, [authUser, authLoading, showSnackbar]);

  const handleEditToggle = () => {
    if (!isEditing && profileUser) {
      // When starting to edit, populate form with current profileUser data
      setFirstName(profileUser.firstName || '');
      setLastName(profileUser.lastName || '');
    }
    setIsEditing(!isEditing);
  };

  const handleProfileUpdate = async (event: FormEvent<HTMLFormElement>) => {
    event.preventDefault();
    if (!profileUser) return;
    setSaving(true);

    const updateData: UserUpdateRequest = {};
    if (firstName !== profileUser.firstName) updateData.firstName = firstName;
    if (lastName !== profileUser.lastName) updateData.lastName = lastName;

    if (Object.keys(updateData).length === 0) {
      showSnackbar('No changes to save.', 'info');
      setIsEditing(false);
      setSaving(false);
      return;
    }

    try {
      const updatedUser = await updateUserProfile(updateData);
      // Update the AuthContext with the new user details if successful
      // This assumes your backend returns enough info or your token needs re-decoding
      // A simple way is to re-fetch the token or have updateUserProfile return the new token
      // For now, we'll just update local state and context if token has enough info.
      if (token) {
          // If your JWT contains first/last name, re-decoding it after update might be an option
          // Or better, if updateUserProfile returns the full UserResponseDto, use that.
          // For now, let's assume the token itself doesn't need to change for name updates.
          // We will update the user object in AuthContext by re-decoding (if names are in token)
          // OR by creating a new user object.
          // This part depends on how your AuthContext's decodeAndSetUser or a dedicated updateUserInContext works.
          // A pragmatic approach if names are not in token:
          const newUserState: AuthUserType = {
            ...authUser!, // Keep existing authUser data like id, username, email, roles
            firstName: updatedUser.firstName, // Use updated values
            lastName: updatedUser.lastName,
          };
          localStorage.setItem('user', JSON.stringify(newUserState)); // Update local storage
          // Manually update the context's user. This is a bit of a hack if not using decodeAndSetUser.
          // Ideally, AuthContext would have a function like `updateAuthUser(partialUserData)`
          // For now, let's assume we can use decodeAndSetUser if token implies user info.
          // If not, you'd set `authUser` in context directly (if AuthContext exposes a setter).
          // The simplest without modifying AuthContext too much:
          setProfileUser(updatedUser as AuthUserType); // Update local display
          // If your AuthContext's `decodeAndSetUser` updates `localStorage.getItem('user')` then:
          // decodeAndSetUser(token); // This might be an option if token has updated info (unlikely for just name)
          // The most robust way would be for AuthContext to have an explicit `updateUser(newUser)` method.
          // For this example, we update the displayed profileUser. A full refresh or re-login would show it elsewhere.
          showSnackbar('Profile updated successfully!', 'success');

      }
      setIsEditing(false);
    } catch (error: any) {
      showSnackbar(error.response?.data?.message || 'Failed to update profile.', 'error');
    } finally {
      setSaving(false);
    }
  };


  if (loading || authLoading) {
    return <Box display="flex" justifyContent="center" alignItems="center" minHeight="200px"><CircularProgress /></Box>;
  }

  if (!profileUser) {
    return <Alert severity="warning" sx={{m:2}}>User profile data not available. Please try logging in again.</Alert>;
  }

  return (
    <Paper elevation={3} sx={{ p: { xs: 2, md: 4 }, mt: 2, maxWidth: 700, margin: 'auto' }}>
      <Box sx={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', mb: 3 }}>
        <Box sx={{ display: 'flex', alignItems: 'center' }}>
          <Avatar sx={{ width: 60, height: 60, mr: 2, bgcolor: 'primary.main' }}>
            <AccountCircleIcon sx={{ fontSize: 40 }} />
          </Avatar>
          <Typography variant="h4" component="h1">
            My Profile
          </Typography>
        </Box>
        {!isEditing && (
          <Button variant="outlined" startIcon={<EditIcon />} onClick={handleEditToggle} disabled={saving}>
            Edit Profile
          </Button>
        )}
      </Box>
      <Divider sx={{ mb: 3 }} />

      {!isEditing ? (
        <Grid container spacing={2}>
          <Grid item xs={12} sm={6}>
            <Typography variant="subtitle1" color="text.secondary">Username:</Typography>
            <Typography variant="body1" gutterBottom>{profileUser.username}</Typography>
          </Grid>
          <Grid item xs={12} sm={6}>
            <Typography variant="subtitle1" color="text.secondary">Email:</Typography>
            <Typography variant="body1" gutterBottom>{profileUser.email}</Typography>
          </Grid>
          <Grid item xs={12} sm={6}>
            <Typography variant="subtitle1" color="text.secondary">First Name:</Typography>
            <Typography variant="body1" gutterBottom>{profileUser.firstName || 'Not set'}</Typography>
          </Grid>
          <Grid item xs={12} sm={6}>
            <Typography variant="subtitle1" color="text.secondary">Last Name:</Typography>
            <Typography variant="body1" gutterBottom>{profileUser.lastName || 'Not set'}</Typography>
          </Grid>
          <Grid item xs={12}>
            <Typography variant="subtitle1" color="text.secondary">User ID:</Typography>
            <Typography variant="body1" gutterBottom>{profileUser.id}</Typography>
          </Grid>
          {/* Roles display if available */}
        </Grid>
      ) : (
        <Box component="form" onSubmit={handleProfileUpdate}>
          <Grid container spacing={2}>
            <Grid item xs={12} sm={6}>
              <TextField
                label="First Name"
                fullWidth
                variant="outlined"
                value={firstName}
                onChange={(e) => setFirstName(e.target.value)}
                disabled={saving}
              />
            </Grid>
            <Grid item xs={12} sm={6}>
              <TextField
                label="Last Name"
                fullWidth
                variant="outlined"
                value={lastName}
                onChange={(e) => setLastName(e.target.value)}
                disabled={saving}
              />
            </Grid>
             <Grid item xs={12} sm={6}>
              <Typography variant="subtitle1" color="text.secondary">Username:</Typography>
              <Typography variant="body1" gutterBottom>{profileUser.username} (Cannot change)</Typography>
            </Grid>
            <Grid item xs={12} sm={6}>
              <Typography variant="subtitle1" color="text.secondary">Email:</Typography>
              <Typography variant="body1" gutterBottom>{profileUser.email} (Cannot change)</Typography>
            </Grid>
          </Grid>
          <Box sx={{ mt: 3, display: 'flex', justifyContent: 'flex-end', gap: 1 }}>
            <Button onClick={() => setIsEditing(false)} disabled={saving} color="inherit">
              Cancel
            </Button>
            <Button type="submit" variant="contained" startIcon={<SaveIcon />} disabled={saving}>
              {saving ? <CircularProgress size={20} color="inherit"/> : 'Save Changes'}
            </Button>
          </Box>
        </Box>
      )}
    </Paper>
  );
};

export default UserProfilePage;