// App.jsx
import { Container, Typography, Box, CssBaseline, Button, ThemeProvider, createTheme } from '@mui/material';
import { useEffect } from 'react';
import { BrowserRouter as Router, Routes, Route, useLocation, useNavigate } from 'react-router-dom';
import AudioRecorderPage from './AudioRecorderPage';

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

// 로그인 성공 페이지 컴포넌트
function LoginSuccess() {
  const location = useLocation();

  useEffect(() => {
    const searchParams = new URLSearchParams(location.search);
    const accessToken = searchParams.get('access');

    if (accessToken) {
      // 로컬 스토리지에 토큰 저장
      localStorage.setItem('accessToken', accessToken);
      console.log('로그인 성공! 토큰 저장:', accessToken);
    } else {
      console.error('로그인 토큰이 없습니다.');
    }
  }, [location]);

  return (
    <Box sx={{ my: 4, textAlign: 'center' }}>
      <Typography variant="h6">로그인 성공!</Typography>
      <Typography variant="body1">이제 음성 인식 서비스를 이용할 수 있습니다.</Typography>
    </Box>
  );
}

// 메인 페이지 컴포넌트
function Home() {
  const navigate = useNavigate();

  return (
    <Box sx={{ my: 4, textAlign: 'center' }}>
      <Typography variant="h4" gutterBottom>음성 인식 및 응답 시스템</Typography>
      {/* 로그인 버튼 */}
      <Button
        variant="contained"
        color="primary"
        href="http://localhost:8080/api/v1/user/google"
        sx={{ mb: 2 }}
      >
        Google 로그인
      </Button>
      <br />
      {/* 음성 인식 페이지로 이동 버튼 */}
      <Button
        variant="contained"
        color="secondary"
        onClick={() => navigate('/audio-recorder')}
      >
        음성 인식 시작
      </Button>
    </Box>
  );
}

function App() {
  return (
    <ThemeProvider theme={theme}>
      <CssBaseline />
      <Router>
        <Container maxWidth="md">
          <Box sx={{ my: 4, display: 'flex', flexDirection: 'column', alignItems: 'center' }}>
            <Routes>
              <Route path="/" element={<Home />} />
              <Route path="/login-success" element={<LoginSuccess />} />
              <Route path="/audio-recorder" element={<AudioRecorderPage />} />
            </Routes>
          </Box>
        </Container>
      </Router>
    </ThemeProvider>
  );
}

export default App;
