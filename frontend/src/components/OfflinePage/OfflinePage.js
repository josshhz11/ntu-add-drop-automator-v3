import React from 'react';
import {
  Typography,
  Box,
  Card,
  CardContent,
} from '@mui/material';
import TaskAltIcon from '@mui/icons-material/TaskAlt';
import BaseLayout from '../Base/Base';

const OfflinePage = () => {
  // This would typically come from your backend or state management
  const currentMonth = new Date().getMonth() + 1; // 1-12
  const currentYear = new Date().getFullYear();
  
  // Determine next eligible period
  const eligibleMonth = currentMonth < 8 ? "August" : "January";
  const eligibleYear = currentMonth < 8 ? currentYear : currentYear + 1;

  return (
    <BaseLayout>
      <Card 
        sx={{ 
          border: 0, 
          boxShadow: 0, 
          bgcolor: 'transparent',
          textAlign: 'center',
          display: 'flex',
          alignItems: 'center',
          justifyContent: 'center',
          height: '100%'
        }}
      >
        <CardContent sx={{ p: 4 }}>
          <Box sx={{ mb: 4 }}>
            <TaskAltIcon 
              sx={{ 
                fontSize: 80, 
                color: 'primary.main',
                mb: 2 
              }} 
            />
          </Box>

          <Typography 
            variant="h4" 
            component="h2" 
            color="primary"
            gutterBottom
            fontWeight="bold"
          >
            NTU Add-Drop is currently offline
          </Typography>
          
          <Typography 
            variant="h6" 
            sx={{ mb: 3, color: 'text.secondary' }}
          >
            Next Add-Drop period:
          </Typography>
          
          <Box
            sx={{
              bgcolor: 'primary.main',
              color: 'white',
              borderRadius: 2,
              p: 3,
              display: 'inline-block',
              minWidth: 200
            }}
          >
            <Typography variant="h4" component="div" fontWeight="bold">
              {eligibleMonth}
            </Typography>
            <Typography variant="h4" component="div" fontWeight="bold">
              {eligibleYear}
            </Typography>
          </Box>
        </CardContent>
      </Card>
    </BaseLayout>
  );
};

export default OfflinePage;