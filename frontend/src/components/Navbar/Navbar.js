import React from 'react';
import {
  AppBar,
  Toolbar,
  Typography,
  Button,
  Box,
} from '@mui/material';
import { Link, useLocation } from 'react-router-dom';

const Navbar = () => {
  const location = useLocation();

  return (
    <AppBar position="static" elevation={2}>
      <Toolbar>
        <Typography variant="h6" component="div" sx={{ flexGrow: 1 }}>
          NTU Add-Drop Automator
        </Typography>
        <Box sx={{ display: 'flex', gap: 2 }}>
          <Button 
            color="inherit" 
            component={Link} 
            to="/"
            variant={location.pathname === '/' ? 'outlined' : 'text'}
          >
            Home
          </Button>
          <Button 
            color="inherit" 
            component={Link} 
            to="/input"
            variant={location.pathname === '/input' ? 'outlined' : 'text'}
          >
            Add/Drop
          </Button>
          <Button 
            color="inherit" 
            component={Link} 
            to="/swap-status"
            variant={location.pathname === '/swap-status' ? 'outlined' : 'text'}
          >
            Status
          </Button>
        </Box>
      </Toolbar>
    </AppBar>
  );
};

export default Navbar;