import React from 'react';
import {
  Container,
  Box,
  Paper,
  Card,
  CardContent,
  Typography,
  Button,
} from '@mui/material';
import TaskAltIcon from '@mui/icons-material/TaskAlt';
import ChatIcon from '@mui/icons-material/Chat';
import ShieldIcon from '@mui/icons-material/Shield';

// const API_BASE_URL = 'http://localhost:8000';

const BaseLayout = ({ children, pageTitle = "NTU Add-Drop Automator" }) => {
  return (
    <Box 
      sx={{ 
        minHeight: '100vh',
        background: 'linear-gradient(135deg, #f5f7fa 0%, #c3cfe2 100%)',
        display: 'flex',
        alignItems: 'center',
        justifyContent: 'center',
        py: 5
      }}
    >
      <Container maxWidth="lg">
        <Paper 
          elevation={3} 
          sx={{ 
            overflow: 'hidden',
            borderRadius: 2,
            display: 'flex',
            minHeight: '600px'
          }}
        >
          {/* Left Panel - Static Content */}
          <Box sx={{ 
            bgcolor: 'white', 
            p: 4, 
            display: 'flex', 
            flexDirection: 'column',
            width: '50%',
            minWidth: '400px'
          }}>
            {/* Logo and Title */}
            <Box sx={{ display: 'flex', alignItems: 'center', mb: 4 }}>
              <Box 
                component="img"
                src={`${process.env.PUBLIC_URL}/NTU_Logo.png`}
                alt="NTU Logo"
                sx={{ height: 40, mr: 3 }}
              />
              <Typography variant="h5" component="h1" fontWeight="bold">
                NTU Add-Drop Automator
              </Typography>
            </Box>

            {/* Description Card */}
            <Card sx={{ bgcolor: '#f8f9fa', border: 0, mb: 4, flexGrow: 1 }}>
              <CardContent>
                <Typography variant="body1" sx={{ mb: 2, color: 'text.secondary' }}>
                  This tool helps you to periodically check vacancies for your modules
                  you wish to swap every 5 minutes, up to a maximum of 2 hours. If
                  slots are found, they will be automatically swapped for you. If not, it
                  will keep running till we find a slot, or 2 hours is up. Then you will
                  have to sign in again.
                </Typography>
                <Typography variant="body1" sx={{ mb: 2, color: 'text.secondary' }}>
                  This tool allows for choosing multiple desired slots (if changing due
                  to time slots), and you simply have to write all the indexes separated
                  by commas (i.e. 80271, 80272, 80273).
                </Typography>
                <Typography variant="body1" sx={{ fontWeight: 'bold', color: 'text.secondary' }}>
                  If you're running into errors on the page, try closing this tab and
                  trying again.
                </Typography>
                <Typography variant="body1" sx={{ mt: 2, color: 'text.secondary' }}>
                  This tool is fully secure and no user credentials are stored or accessible.
                </Typography>
              </CardContent>
            </Card>

            {/* Footer Buttons */}
            <Box sx={{ display: 'flex', gap: 2 }}>
              <Button 
                variant="contained" 
                sx={{ flex: 1 }}
                startIcon={<ChatIcon />}
                onClick={() => window.open('https://docs.google.com/forms/d/e/1FAIpQLSdniXT-UR1MLjssAkZLvJunD2lCgfckdjMd7iamOFD-cjCMKg/viewform', '_blank')}
              >
                Provide Feedback
              </Button>
              <Button 
                variant="outlined" 
                sx={{ flex: 1 }}
                startIcon={<ShieldIcon />}
                href="/privacy-policy"
              >
                Privacy Policy
              </Button>
            </Box>
          </Box>

          {/* Right Panel - Dynamic Content */}
          <Box sx={{ 
            bgcolor: '#f8f9fa', 
            p: 4, 
            display: 'flex', 
            flexDirection: 'column',
            width: '50%',
            minWidth: '400px'
          }}>
            {children}
          </Box>
        </Paper>
      </Container>
    </Box>
  );
};

export default BaseLayout;