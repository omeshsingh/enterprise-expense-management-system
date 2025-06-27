import React, { useEffect } from 'react';
import { Outlet, useNavigate, Link as RouterLink, useLocation } from 'react-router-dom';
import {
  AppBar, Toolbar, Typography, Box, Drawer, List, ListItem,
  ListItemButton, ListItemIcon, ListItemText, CssBaseline, Divider, IconButton, Tooltip, Avatar
} from '@mui/material';

// Icons for Employee Menu
import DashboardIcon from '@mui/icons-material/Dashboard';
import AddCardIcon from '@mui/icons-material/AddCard';         // For Submit Expense
import ReceiptLongIcon from '@mui/icons-material/ReceiptLong'; // For My Expenses

// Icons for Account/General Menu
import AccountCircleIcon from '@mui/icons-material/AccountCircle'; // For My Profile
import LogoutIcon from '@mui/icons-material/Logout';

// Icons for Manager Menu
import SupervisorAccountIcon from '@mui/icons-material/SupervisorAccount'; // For Pending Approvals
// import AssessmentIcon from '@mui/icons-material/Assessment'; // Example for future Reports

import { useAuth } from '../../contexts/AuthContext'; // Assuming your AuthContext path

const drawerWidth = 240;

interface MenuItemDef {
  text: string;
  icon: React.ReactElement;
  path: string;
  roles?: string[]; // Optional: Roles that can see this item
}

const PrivateLayout: React.FC = () => {
  const navigate = useNavigate();
  const location = useLocation();
  const { logoutUser, user } = useAuth();
   // Get user from auth context

   useEffect(() => {
    if (user) {
      console.log("Current user in PrivateLayout:", JSON.stringify(user, null, 2));
      console.log("User roles:", user.roles);
    } else {
      console.log("No user in PrivateLayout context yet.");
    }
  }, [user]);

  const handleLogout = () => {
    logoutUser();
    navigate('/login'); // Redirect to login after logout
  };

  // Define menu items with optional roles for visibility control
  const employeeMenuItems: MenuItemDef[] = [
    { text: 'Dashboard', icon: <DashboardIcon />, path: '/dashboard' },
    { text: 'My Expenses', icon: <ReceiptLongIcon />, path: '/dashboard/my-expenses' },
    { text: 'Submit Expense', icon: <AddCardIcon />, path: '/dashboard/submit-expense' },
  ];

  const managerMenuItems: MenuItemDef[] = [
    { text: 'Pending Approvals', icon: <SupervisorAccountIcon />, path: '/dashboard/pending-approvals', roles: ['ROLE_MANAGER', 'ROLE_ADMIN'] },
    // { text: 'Team Reports', icon: <AssessmentIcon />, path: '/dashboard/team-reports', roles: ['ROLE_MANAGER', 'ROLE_ADMIN'] }, // Example
  ];

  const accountMenuItems: MenuItemDef[] = [
    { text: 'My Profile', icon: <AccountCircleIcon />, path: '/dashboard/profile' },
  ];

  // Helper function to check if user has any of the required roles for a menu item
  const userHasAccess = (itemRoles?: string[]): boolean => {
    if (!itemRoles || itemRoles.length === 0) {
      return true; // No specific roles required, accessible to all authenticated
    }
    if (!user || !user.roles || user.roles.length === 0) {
      return false; // User has no roles defined
    }
    return itemRoles.some(role => (user.roles ?? []).includes(role));
  };

  return (
    <Box sx={{ display: 'flex' }}>
      <CssBaseline />
      <AppBar position="fixed" sx={{ zIndex: (theme) => theme.zIndex.drawer + 1 }}>
        <Toolbar>
          <Typography variant="h6" component="div" sx={{ flexGrow: 1 }}>
            Expense Management
          </Typography>
          {user && (
            <Box sx={{ display: 'flex', alignItems: 'center' }}>
              <Tooltip title="My Profile">
                <IconButton component={RouterLink} to="/dashboard/profile" color="inherit" sx={{ mr: 1 }}>
                  <Avatar sx={{ bgcolor: 'secondary.main', width: 32, height: 32 }}>
                    {user.username ? user.username.charAt(0).toUpperCase() : <AccountCircleIcon fontSize="small" />}
                  </Avatar>
                </IconButton>
              </Tooltip>
              <Typography sx={{ mr: 2, display: { xs: 'none', sm: 'block' } }}>
                {user.username}
              </Typography>
            </Box>
          )}
          <Tooltip title="Logout">
            <IconButton onClick={handleLogout} color="inherit">
              <LogoutIcon />
            </IconButton>
          </Tooltip>
        </Toolbar>
      </AppBar>
      <Drawer
        variant="permanent"
        sx={{
          width: drawerWidth,
          flexShrink: 0,
          [`& .MuiDrawer-paper`]: { width: drawerWidth, boxSizing: 'border-box' },
        }}
      >
        <Toolbar /> {/* Spacer to push content below AppBar */}
        <Box sx={{ overflow: 'auto' }}>
          {/* Employee Menu Items */}
          <List>
            {employeeMenuItems.map((item) => (
              userHasAccess(item.roles) && ( // Check access if roles are defined
                <ListItem
                  key={item.text}
                  disablePadding
                  component={RouterLink}
                  to={item.path}
                  sx={{ textDecoration: 'none', color: 'inherit' }}
                >
                  <ListItemButton selected={location.pathname === item.path}>
                    <ListItemIcon>{item.icon}</ListItemIcon>
                    <ListItemText primary={item.text} />
                  </ListItemButton>
                </ListItem>
              )
            ))}
          </List>

          {/* Manager Menu Items - Render this section only if there are accessible manager items */}
          {managerMenuItems.some(item => userHasAccess(item.roles)) && (
            <>
              <Divider sx={{ my: 1 }} />
              <Typography variant="overline" sx={{ pl: 2, color: 'text.secondary', display: 'block' }}>
                Manager Actions
              </Typography>
              <List>
                {managerMenuItems.map((item) => (
                  userHasAccess(item.roles) && (
                    <ListItem
                      key={item.text}
                      disablePadding
                      component={RouterLink}
                      to={item.path}
                      sx={{ textDecoration: 'none', color: 'inherit' }}
                    >
                      <ListItemButton selected={location.pathname === item.path}>
                        <ListItemIcon>{item.icon}</ListItemIcon>
                        <ListItemText primary={item.text} />
                      </ListItemButton>
                    </ListItem>
                  )
                ))}
              </List>
            </>
          )}

          {/* Account Menu Items */}
          <Divider sx={{ my: 1 }} />
           <Typography variant="overline" sx={{ pl: 2, color: 'text.secondary', display: 'block' }}>
                Account
            </Typography>
          <List>
            {accountMenuItems.map((item) => (
               userHasAccess(item.roles) && (
                <ListItem
                  key={item.text}
                  disablePadding
                  component={RouterLink}
                  to={item.path}
                  sx={{ textDecoration: 'none', color: 'inherit' }}
                >
                  <ListItemButton selected={location.pathname === item.path}>
                    <ListItemIcon>{item.icon}</ListItemIcon>
                    <ListItemText primary={item.text} />
                  </ListItemButton>
                </ListItem>
               )
            ))}
          </List>
        </Box>
      </Drawer>
      <Box
        component="main"
        sx={{
          flexGrow: 1,
          p: 3,
          minHeight: '100vh',
          backgroundColor: (theme) => theme.palette.mode === 'light' ? theme.palette.grey[100] : theme.palette.grey[900], // Light grey background
        }}
      >
        <Toolbar /> {/* Another spacer for the AppBar height */}
        <Outlet /> {/* This is where the routed page component will be rendered */}
      </Box>
    </Box>
  );
};

export default PrivateLayout;