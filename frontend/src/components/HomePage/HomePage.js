import React, { useState } from 'react';
import {
  Typography,
  Button,
  Box,
  TextField,
  InputAdornment,
  Card,
  CardContent,
  Alert,
  CircularProgress,
} from '@mui/material';
import { useNavigate } from 'react-router-dom';
import PersonIcon from '@mui/icons-material/Person';
import LockIcon from '@mui/icons-material/Lock';
import MenuBookIcon from '@mui/icons-material/MenuBook';
import ArrowForwardIcon from '@mui/icons-material/ArrowForward';
import BaseLayout from '../Base/Base';
import API_BASE_URL from '../../config/api';
import axios from 'axios';

const HomePage = () => {
  const navigate = useNavigate();
  const [formData, setFormData] = useState({
    username: '',
    password: '',
    num_modules: ''
  });
  const [message, setMessage] = useState('');
  const [loading, setLoading] = useState(false);

  const handleChange = (e) => {
    setFormData({
      ...formData,
      [e.target.name]: e.target.value
    });
  };

  const handleSubmit = async (event) => {
    event.preventDefault();
    setLoading(true);
    setMessage('');

    try {
      // Validate that num_modules is a positive number
      const numModules = parseInt(formData.num_modules);
      if (numModules < 1 || numModules > 6) { // Add reasonable limits
        setMessage('Please enter a valid number of modules (1-6)');
        setLoading(false);
        return;
      }
      
      // Send data as JSON for API call
      const loginData = {
        username: formData.username,
        password: formData.password,
        num_modules: numModules
      };

      // Call the login backend API
      const response = await axios.post(`${API_BASE_URL}/api/login`, loginData, {
        headers: {
          'Content-Type': 'application/json',
        },
        withCredentials: true, // Important for session cookies
      });

      if (response.data.success) {
        // Navigate to the input-index page on the frontend
        navigate('/input-index', {
          state: {
            num_modules: numModules
          }
        });
      }
    } catch (error) {
      console.error('Login error:', error);
      const errorMessage = error.response?.data?.detail || 'Login failed. Please try again.'; 
      setMessage(errorMessage);
    } finally {
      setLoading(false);
    }
  };

  return (
    <BaseLayout>
      {message && (
        <Alert severity="error" sx={{ mb: 3 }}>
          {message}
        </Alert>
      )}

      <Card 
        sx={{ 
          border: 0, 
          boxShadow: 2, 
          flexGrow: 1,
          display: 'flex',
          flexDirection: 'column'
        }}
      >
        <CardContent sx={{ p: 4, display: 'flex', flexDirection: 'column', height: '100%' }}>
          <Typography variant="h5" component="h2" gutterBottom>
            Login Details
          </Typography>
          
          <Box 
            component="form" 
            onSubmit={handleSubmit}
            sx={{ display: 'flex', flexDirection: 'column', height: '100%' }}
          >
            <TextField
              fullWidth
              required
              margin="normal"
              label="Username"
              name="username"
              value={formData.username}
              onChange={handleChange}
              placeholder="Enter your username"
              autoFocus
              InputProps={{
                startAdornment: (
                  <InputAdornment position="start">
                    <PersonIcon />
                  </InputAdornment>
                ),
              }}
            />
            
            <TextField
              fullWidth
              required
              margin="normal"
              type="password"
              label="Password"
              name="password"
              value={formData.password}
              onChange={handleChange}
              placeholder="Enter your password"
              InputProps={{
                startAdornment: (
                  <InputAdornment position="start">
                    <LockIcon />
                  </InputAdornment>
                ),
              }}
            />
            
            <TextField
              fullWidth
              required
              margin="normal"
              type="number"
              label="Number of Modules"
              name="num_modules"
              value={formData.num_modules}
              onChange={handleChange}
              placeholder="Enter number of modules"
              InputProps={{
                startAdornment: (
                  <InputAdornment position="start">
                    <MenuBookIcon />
                  </InputAdornment>
                ),
                inputProps: { min: 1, max: 6 }
              }}
            />
            
            <Box sx={{ mt: 'auto', pt: 3 }}>
              <Button
                type="submit"
                variant="contained"
                fullWidth
                size="large"
                disabled={loading}
                endIcon={loading ? <CircularProgress size={20} /> : <ArrowForwardIcon />}
              >
                {loading ? 'Logging in...' : 'Input Indexes'}
              </Button>
            </Box>
          </Box>
        </CardContent>
      </Card>
    </BaseLayout>
  );
};

export default HomePage;