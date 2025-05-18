// App.jsx
import { useState } from 'react';
import {
  Container,
  Typography,
  Box,
  CssBaseline,
  ThemeProvider,
  createTheme
} from '@mui/material';
import { BrowserRouter as Router, Routes, Route } from 'react-router-dom';
import AudioRecorder from './AudioRecorder';
import LoginSuccess from './LoginSuccess'; // 추가

// 테마 설정
const theme = createTheme({
  palette: {
    primary: {
      main: '#3f51b5',
    },
    secondary: {
      main: '#f50057',
    },
  },
});

const token = localStorage.getItem('accessToken');
if (!token) {
  window.location.href = 'http://localhost:8080/api/v1/user/google';
}

function App() {
  return (
    <ThemeProvider theme={theme}>
      <CssBaseline />
      <Router>
        <Container maxWidth="md">
          <Box sx={{ my: 4, display: 'flex', flexDirection: 'column', alignItems: 'center' }}>
            <Typography variant="h4" component="h1" gutterBottom align="center">
              음성 인식 및 응답 시스템
            </Typography>

            <Routes>
              <Route path="/" element={<AudioRecorder />} />
              <Route path="/login-success" element={<LoginSuccess />} />
            </Routes>
          </Box>
        </Container>
      </Router>
    </ThemeProvider>
  );
}

export default App;
