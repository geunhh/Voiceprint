// AudioRecorderPage.jsx
import { Container, Typography, Box, Button, CssBaseline, ThemeProvider, createTheme } from '@mui/material';
import { useNavigate } from 'react-router-dom';
import AudioRecorder from './AudioRecorder';

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

function AudioRecorderPage() {
  const navigate = useNavigate();

  return (
    <ThemeProvider theme={theme}>
      <CssBaseline />
      <Container maxWidth="md">
        <Box sx={{ my: 4, display: 'flex', flexDirection: 'column', alignItems: 'center' }}>
          <Typography variant="h4" gutterBottom>음성 녹음 페이지</Typography>
          <AudioRecorder />
          <Button
            variant="contained"
            color="secondary"
            onClick={() => navigate('/')}
            sx={{ mt: 2 }}
          >
            메인으로 돌아가기
          </Button>
        </Box>
      </Container>
    </ThemeProvider>
  );
}

export default AudioRecorderPage;
