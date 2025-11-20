import React, { useState, useEffect } from 'react';
import {
  Typography,
  Button,
  Box,
  Card,
  CardContent,
  Chip,
  Alert,
} from '@mui/material';
import { useLocation, useNavigate } from 'react-router-dom';
import AutorenewIcon from '@mui/icons-material/Autorenew';
import StopCircleIcon from '@mui/icons-material/StopCircle';
import LogoutIcon from '@mui/icons-material/Logout';
import BaseLayout from '../Base/Base';
import API_BASE_URL from '../../config/api';
import axios from 'axios';

const SwapStatus = () => {
  const location = useLocation();
  const navigate = useNavigate();
  
  // Get data passed from InputIndex or use placeholder data
  const passedData = location.state || {};
  const sessionId = passedData.session_id;
  
  // Initial state - will be replaced by real data from the backend
  const [swapData, setSwapData] = useState({
    status: 'Loading', // Processing, Completed, Error, Timed Out, Stopped
    message: 'Loading swap status...',
    details: []
  });
  const [error, setError] = useState('');
  const [loading, setLoading] = useState(false);

  // Set up polling for status updates
  useEffect(() => {
    if (!sessionId) {
      setError('No session ID provided');
      return;
    }

    const fetchStatus = async () => {
      if (!sessionId) return;

      try {
        const response = await axios.get(`${API_BASE_URL}/api/swap-status/${sessionId}`, {
          withCredentials: true,
        });

        setSwapData(response.data); // Updates UI with latest status fetched from Redis through the backend API call
        setError('');
      } catch (error) {
        console.error('Error fetching status:', error);

        if (error.response?.status === 401) {
          setError('Session expired. Redirecting to login...');
          setTimeout(() => navigate('/'), 2000);
          return;
        }

        setError('Failed to fetch swap status');
      }
    };

    // Fetch swap_status immediately
    fetchStatus();

    // Set up polling every 5 seconds
    const interval = setInterval(fetchStatus, 5000);
    
    // Cleanup interval on component unmount
    return () => clearInterval(interval);
  }, [sessionId, navigate]);

  const handleStopSwap = async () => {
    if (!sessionId) return;

    setLoading(true);
    try {
      const response = await axios.post(`${API_BASE_URL}/api/stop-swap/${sessionId}`, {}, {
        withCredentials: true,
      });

      if (response.data.success) {
        navigate('/'); // Go back to homepage if swap is successfully stopped
      }
    } catch (error) {
      console.error('Error stopping swap:', error);
      setError('Failed to stop swap');
    } finally {
      setLoading(false);
    }
  };

  // Log out is for after successful swap
  const handleLogout = async () => {
    if (!sessionId) {
      navigate('/');
      return;
    }
    
    setLoading(true);
    try {
      const response = await axios.post(`${API_BASE_URL}/api/logout/${sessionId}`, {}, {
        withCredentials: true,
      });

      if (response.data.success) {
        navigate('/');
      }
    } catch (error) {
      console.error('Error during logout:', error);
      navigate('/') // Navigate back to home anyway
    } finally {
      setLoading(false);
    }
  };

  const getStatusColor = (status) => {
    switch (status.toLowerCase()) {
      case 'completed': return 'success';
      case 'processing': return 'primary';
      case 'error':
      case 'timed out':
      case 'stopped': return 'error';
      default: return 'default';
    }
  };

  const getStatusIndicatorColor = (status) => {
    switch (status.toLowerCase()) {
      case 'completed': return '#4caf50';
      case 'processing': return '#2196f3';
      case 'error':
      case 'timed out':
      case 'stopped': return '#f44336';
      default: return '#9e9e9e';
    }
  };

  if (!sessionId) {
    return (
      <BaseLayout>
        <Alert severity="error">
          No swap session found. Please start a new swap from the homepage.
        </Alert>
      </BaseLayout>
    )
  }

  return (
    <BaseLayout>
      {error && (
        <Alert severity="error" sx={{ mb: 3}}>
          {error}
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
          {/* Status Header */}
          <Box sx={{ mb: 4 }}>
            <Box sx={{ display: 'flex', alignItems: 'center', mb: 2 }}>
              <AutorenewIcon sx={{ mr: 1 }} />
              <Typography variant="h5" component="h2" fontWeight="bold">
                Swap Status
              </Typography>
            </Box>
            
            <Box sx={{ display: 'flex', alignItems: 'center', mt: 2 }}>
              <Box
                sx={{
                  width: 12,
                  height: 12,
                  borderRadius: '50%',
                  backgroundColor: getStatusIndicatorColor(swapData.status),
                  mr: 1
                }}
              />
              <Chip 
                label={swapData.status}
                color={getStatusColor(swapData.status)}
                sx={{ fontWeight: 'bold' }}
              />
            </Box>
            
            <Typography variant="body2" color="text.secondary" sx={{ mt: 1 }}>
              {swapData.message}
            </Typography>
          </Box>

          {/* Scrollable Status Container */}
          <Box sx={{ 
            flexGrow: 1, 
            overflowY: 'auto', 
            mb: 3,
            pr: 1
          }}>
            {swapData.details && swapData.details.map((detail, index) => (
              <Card key={index} sx={{ mb: 3, border: '1px solid #e0e0e0' }}>
                <CardContent>
                  <Box sx={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', mb: 2 }}>
                    <Typography variant="h6" fontWeight="bold">
                      Module {index + 1}
                    </Typography>
                    <Chip 
                      label={detail.swapped ? 'Completed' : 'Pending'}
                      color={detail.swapped ? 'success' : 'default'}
                      size="small"
                    />
                  </Box>
                  
                  <Box sx={{ mb: 2 }}>
                    <Typography variant="caption" color="text.secondary">
                      Old Index
                    </Typography>
                    <Typography variant="body1" fontWeight="bold">
                      {detail.old_index}
                    </Typography>
                  </Box>
                  
                  <Box sx={{ mb: 2 }}>
                    <Typography variant="caption" color="text.secondary">
                      New Index
                    </Typography>
                    <Typography variant="body1" fontWeight="bold">
                      {detail.new_indexes}
                    </Typography>
                  </Box>
                  
                  <Box>
                    <Typography variant="caption" color="text.secondary">
                      Status
                    </Typography>
                    <Typography 
                      variant="body2" 
                      color={detail.swapped ? 'success.main' : 'text.primary'}
                    >
                      {detail.message}
                    </Typography>
                  </Box>
                </CardContent>
              </Card>
            ))}
          </Box>

          {/* Action Buttons */}
          <Box sx={{ mt: 'auto' }}>
            {(swapData.status === 'Processing') && (
              <Button
                variant="contained"
                color="error"
                fullWidth
                size="large"
                startIcon={<StopCircleIcon />}
                onClick={handleStopSwap}
                disabled={loading}
                sx={{ mb: 2 }}
              >
                Stop and Log Out
              </Button>
            )}
            
            {(swapData.status === 'Completed' || 
              swapData.status === 'Error' || 
              swapData.status === 'Timed Out' || 
              swapData.status === 'Stopped') && (
              <Button
                variant="contained"
                fullWidth
                size="large"
                startIcon={<LogoutIcon />}
                onClick={handleLogout}
                disabled={loading}
              >
                Log Out
              </Button>
            )}
          </Box>
        </CardContent>
      </Card>
    </BaseLayout>
  );
};

export default SwapStatus;