import React, { useState, useEffect } from 'react';
import { BrowserRouter as Router, Routes, Route } from 'react-router-dom';
import CssBaseline from '@mui/material/CssBaseline';
import { ThemeProvider, createTheme } from '@mui/material/styles';
import axios from 'axios';
import HomePage from './components/HomePage/HomePage';
import InputIndex from './components/InputIndex/InputIndex';
import SwapStatus from './components/SwapStatus/SwapStatus';
import PrivacyPolicy from './components/PrivacyPolicy/PrivacyPolicy';
import ErrorPage from './components/ErrorPage/ErrorPage';
import OfflinePage from './components/OfflinePage/OfflinePage';
import './App.css';
import API_BASE_URL from './config/api';

const theme = createTheme({
  palette: {
    primary: {
      main: '#1e4976',
    },
    secondary: {
      main: '#dc004e',
    },
    background: {
      default: '#f8f9fa',
    },
  },
  typography: {
    fontFamily: '"Inter", "Roboto", "Helvetica", "Arial", sans-serif',
  },
});

function App() {
  // Function to check if it's add-drop period (January or August) - FRONTEND
  {/*const isAddDropPeriod = () => {
    const currentMonth = new Date().getMonth() + 1; // 1-12
    return currentMonth === 1 || currentMonth === 8;
  };*/}

  const getPeriodData = () => {
    const currentMonth = new Date().getMonth() + 1;
    const currentYear = new Date().getFullYear();

    if (currentMonth === 1 || currentMonth === 8) {
      return null; // Active period, no next period data needed
    }

    const eligibleMonth = currentMonth < 8 ? "August" : "January"
    const eligibleYear = currentMonth < 8 ? currentYear : currentYear + 1;

    return {
      month: eligibleMonth,
      year: eligibleYear
    };
  };
  
  return (
    <ThemeProvider theme={theme}>
      <CssBaseline />
      <Router>
        <div className="App">
          <Routes>
            <Route path="/" element={<HomePage />} />
            {/*<Route 
              path="/" 
              element={isAddDropPeriod() ? <HomePage /> : <OfflinePage periodData={getPeriodData()} />} 
            />*/}
            <Route path="/input-index" element={<InputIndex />} />
            <Route path="/swap-status" element={<SwapStatus />} />
            <Route path="/privacy-policy" element={<PrivacyPolicy />} />
            <Route path="/error-page" element={<ErrorPage />} />
          </Routes>
        </div>
      </Router>
    </ThemeProvider>
  )
}

export default App;