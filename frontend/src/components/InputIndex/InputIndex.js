import React, { useState, useEffect } from 'react';
import {
  Typography,
  Button,
  Box,
  TextField,
  InputAdornment,
  Card,
  CardContent,
  Alert,
  Divider,
  CircularProgress,
} from '@mui/material';
import { useLocation, useNavigate } from 'react-router-dom';
import BookmarkIcon from '@mui/icons-material/Bookmark';
import SwapHorizIcon from '@mui/icons-material/SwapHoriz';
import AutorenewIcon from '@mui/icons-material/Autorenew';
import BaseLayout from '../Base/Base';
import API_BASE_URL from '../../config/api';
import axios from 'axios';

const InputIndex = () => {
  const location = useLocation();
  const navigate = useNavigate();
  
  // Get number of modules from navigation state or default to 1
  const numModules = location.state?.num_modules || 1;
  
  const [formData, setFormData] = useState({});
  const [message, setMessage] = useState({ type: '', text: '' });
  const [loading, setLoading] = useState(false);

  // Initialize form data based on number of modules
  useEffect(() => {
    const initialData = {};
    for (let i = 0; i < numModules; i++) {
      initialData[`old_index_${i}`] = '';
      initialData[`new_index_${i}`] = '';
    }
    setFormData(initialData);
  }, [numModules]);

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
      // Save all modules data from the form under 'modules'
      const modules = [];
      for (let i = 0; i < numModules; i++) {
        const oldIndex = formData[`old_index_${i}`]
        const newIndexes = formData[`new_index_${i}`]

        if (!oldIndex || !newIndexes) {
          throw new Error(`Please fill in all fields for Module ${i + 1}`);
        }
        
        modules.push({
          old_index: oldIndex,
          new_indexes: newIndexes
        });
      }

      // Call the swap-submission backend API
      const response = await axios.post(`${API_BASE_URL}/api/submit-swap`, {
        num_modules: numModules,
        modules: modules
      }, {
        headers: {
          'Content-Type': 'application/json',
        },
        withCredentials: true,
      });

      if (response.data.success) {
        // Navigate to the swap-status page on the frontend
        navigate('/swap-status', {
          state: {
            session_id: response.data.session_id,
            numModules,
            formData
          }
        });
      }
    } catch (error) {
      console.error('Swap submission error:', error);
      const errorMessage = error.response?.data?.detail || error.message || 'Swap submission failed. Please try again.';
      setMessage({ type: 'error', text: errorMessage });
    } finally {
      setLoading(false);
    }
  };

  // Generate module forms
  const renderModuleForms = () => {
    const forms = [];
    for (let i = 0; i < numModules; i++) {
      forms.push(
        <Box key={i} sx={{ mb: i < numModules - 1 ? 4 : 0 }}>
          <Typography variant="h6" sx={{ mb: 2, fontWeight: 'bold' }}>
            Module {i + 1}
          </Typography>
          
          <TextField
            fullWidth
            required
            margin="normal"
            label="Old Index"
            name={`old_index_${i}`}
            value={formData[`old_index_${i}`] || ''}
            onChange={handleChange}
            placeholder="Enter current index"
            autoFocus={i === 0}
            disabled={loading}
            InputProps={{
              startAdornment: (
                <InputAdornment position="start">
                  <BookmarkIcon />
                </InputAdornment>
              ),
            }}
          />
          
          <TextField
            fullWidth
            required
            margin="normal"
            label="New Index"
            name={`new_index_${i}`}
            value={formData[`new_index_${i}`] || ''}
            onChange={handleChange}
            placeholder="e.g., 80271, 80272, 80273"
            helperText="Separate multiple indexes with commas"
            disabled={loading}
            InputProps={{
              startAdornment: (
                <InputAdornment position="start">
                  <SwapHorizIcon />
                </InputAdornment>
              ),
            }}
          />
          
          {i < numModules - 1 && (
            <Divider sx={{ mt: 3 }} />
          )}
        </Box>
      );
    }
    return forms;
  };

  return (
    <BaseLayout>
      {message.text && (
        <Alert severity={message.type} sx={{ mb: 3 }}>
          {message.text}
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
            Input Indexes
          </Typography>
          
          <Box 
            component="form" 
            onSubmit={handleSubmit}
            sx={{ display: 'flex', flexDirection: 'column', height: '100%' }}
          >
            {/* Scrollable container for module forms */}
            <Box sx={{ 
              flexGrow: 1, 
              overflowY: 'auto', 
              mb: 3,
              pr: 1 // Add padding for scrollbar
            }}>
              {renderModuleForms()}
            </Box>
            
            {/* Submit button at bottom */}
            <Box sx={{ mt: 'auto', pt: 3 }}>
              <Button
                type="submit"
                variant="contained"
                fullWidth
                size="large"
                disabled={loading}
                startIcon={loading ? <CircularProgress size={20} /> : <AutorenewIcon />}
              >
                {loading ? 'Processing...' : 'Automate Swap'}
              </Button>
            </Box>
          </Box>
        </CardContent>
      </Card>
    </BaseLayout>
  );
};

export default InputIndex;