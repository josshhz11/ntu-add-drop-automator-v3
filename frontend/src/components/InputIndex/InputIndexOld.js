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
  Container,
  Paper,
  CircularProgress,
} from '@mui/material';
import axios from 'axios';
import { useNavigate } from 'react-router-dom';
import PersonIcon from '@mui/icons-material/Person';
import LockIcon from '@mui/icons-material/Lock';
import MenuBookIcon from '@mui/icons-material/MenuBook';
import ArrowForwardIcon from '@mui/icons-material/ArrowForward';
import BaseLayout from '../Base/Base';

const InputIndex = () => {
  const [formData, setFormData] = useState({
    username: '',
    password: '',
    courseToAdd: '',
    courseToDrop: '',
  });
  const [loading, setLoading] = useState(false);
  const [message, setMessage] = useState({ type: '', text: '' });

  const handleChange = (e) => {
    setFormData({
      ...formData,
      [e.target.name]: e.target.value,
    });
  };

  const handleSubmit = async (e) => {
    e.preventDefault();
    setLoading(true);
    setMessage({ type: '', text: '' });

    try {
      // Replace with your backend API endpoint
      const response = await axios.post('/api/submit-form', formData);
      setMessage({ type: 'success', text: 'Form submitted successfully!' });
    } catch (error) {
      setMessage({ type: 'error', text: 'Error submitting form. Please try again.' });
    } finally {
      setLoading(false);
    }
  };

  return (
    <Container maxWidth="md" sx={{ mt: 4, mb: 4 }}>
      <Paper elevation={3} sx={{ p: 4 }}>
        <Typography variant="h4" component="h1" gutterBottom textAlign="center">
          Course Add-Drop Form
        </Typography>
        
        {message.text && (
          <Alert severity={message.type} sx={{ mb: 3 }}>
            {message.text}
          </Alert>
        )}

        <Box component="form" onSubmit={handleSubmit}>
          <TextField
            fullWidth
            label="Username"
            name="username"
            value={formData.username}
            onChange={handleChange}
            margin="normal"
            required
          />
          
          <TextField
            fullWidth
            label="Password"
            name="password"
            type="password"
            value={formData.password}
            onChange={handleChange}
            margin="normal"
            required
          />
          
          <TextField
            fullWidth
            label="Course to Add"
            name="courseToAdd"
            value={formData.courseToAdd}
            onChange={handleChange}
            margin="normal"
            helperText="Enter the course code you want to add"
          />
          
          <TextField
            fullWidth
            label="Course to Drop"
            name="courseToDrop"
            value={formData.courseToDrop}
            onChange={handleChange}
            margin="normal"
            helperText="Enter the course code you want to drop"
          />
          
          <Box sx={{ mt: 3, textAlign: 'center' }}>
            <Button
              type="submit"
              variant="contained"
              size="large"
              disabled={loading}
              startIcon={loading && <CircularProgress size={20} />}
            >
              {loading ? 'Processing...' : 'Submit Request'}
            </Button>
          </Box>
        </Box>
      </Paper>
    </Container>
  );
};

export default InputIndex;