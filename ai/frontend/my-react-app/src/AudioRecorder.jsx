// AudioRecorder.jsx
import { useState, useRef, useEffect } from 'react';
import { Button, Box, Typography, CircularProgress } from '@mui/material';
import MicIcon from '@mui/icons-material/Mic';
import StopIcon from '@mui/icons-material/Stop';

const AudioRecorder = () => {
  const [isRecording, setIsRecording] = useState(false);
  const [status, setStatus] = useState('idle');
  const [transcription, setTranscription] = useState('');
  const [isConnected, setIsConnected] = useState(false);
  const [isSpeaking, setIsSpeaking] = useState(false);
  const [processingStats, setProcessingStats] = useState({
    recordCount: 0,
    sentCount: 0,
    totalAudioTime: 0
  });

  const mediaRecorderRef = useRef(null);
  const websocketRef = useRef(null);
  const audioChunksRef = useRef([]);
  const audioElementRef = useRef(null);
  const isRecordingRef = useRef(false);

  // WebSocket 연결 설정
  const connectWebSocket = async () => {
    try {
      const res = await fetch('/api/v1/voice/session', {
        headers: {
          'Authorization': `Bearer ${localStorage.getItem('accessToken')}`
        }
      });

      if (!res.ok) {
        throw new Error('웹소켓 세션 정보를 가져오는 데 실패했습니다.');
      }

      const { wsUrl } = await res.json();
      console.log('웹소켓 URL:', wsUrl); // 디버깅용

      const ws = new WebSocket(wsUrl);

      ws.onopen = () => {
        console.log('WebSocket 연결 성공');
        setIsConnected(true);
      };

      ws.onmessage = (event) => {
        if (typeof event.data === 'string') {
          try {
            const data = JSON.parse(event.data);
            if (data.transcription) {
              setTranscription(data.transcription);
              console.log('받은 텍스트:', data.transcription);
            }
          } catch (error) {
            console.error("메시지 파싱 에러:", error);
          }
        } else {
          handleAudioResponse(event.data);
        }
      };

      ws.onclose = () => {
        console.log('WebSocket 연결 종료');
        setIsConnected(false);
        setTimeout(connectWebSocket, 3000); // 자동 재연결
      };

      ws.onerror = (error) => {
        console.error('WebSocket 에러:', error);
        setIsConnected(false);
      };

      websocketRef.current = ws;
    } catch (err) {
      console.error('WebSocket 초기 연결 실패:', err);
    }
  };

  // 오디오 응답 처리
  const handleAudioResponse = async (audioBlob) => {
    try {
      if (audioElementRef.current) {
        audioElementRef.current.src = URL.createObjectURL(
          new Blob([audioBlob], { type: 'audio/webm' })
        );
        audioElementRef.current.play();
        setIsSpeaking(false);
      }
    } catch (error) {
      console.error("오디오 응답 처리 에러:", error);
    }
  };

  // 녹음 시작
  const startRecording = async () => {
    try {
      const stream = await navigator.mediaDevices.getUserMedia({ audio: true });
      const options = {
        mimeType: 'audio/webm; codecs=opus'
      };
      const mediaRecorder = new MediaRecorder(stream, options);
      mediaRecorderRef.current = mediaRecorder;

      mediaRecorder.ondataavailable = (event) => {
        if (event.data.size > 0) {
          audioChunksRef.current.push(event.data);
        }
      };

      mediaRecorder.onstop = () => {
        const audioBlob = new Blob(audioChunksRef.current, { type: 'audio/webm' });

        if (websocketRef.current && websocketRef.current.readyState === WebSocket.OPEN) {
          console.log("오디오 블롭 전송 중...");
          websocketRef.current.send(audioBlob);
          console.log("오디오 블롭 전송 완료");
        }

        audioChunksRef.current = [];
      };

      mediaRecorder.start();
      setIsRecording(true);
      console.log("녹음 시작");
    } catch (error) {
      console.error("녹음 시작 에러:", error);
    }
  };

  // 녹음 중지
  const stopRecording = () => {
    if (mediaRecorderRef.current) {
      mediaRecorderRef.current.stop();
      setIsRecording(false);
      console.log("녹음 중지");
    }
  };

  useEffect(() => {
    connectWebSocket();
    return () => {
      if (websocketRef.current) {
        websocketRef.current.close();
      }
    };
  }, []);

  return (
    <Box sx={{ display: 'flex', flexDirection: 'column', alignItems: 'center', gap: 2 }}>
      <Typography variant="h5">음성 대화 인터페이스</Typography>
      <Button
        variant="contained"
        color={isRecording ? "secondary" : "primary"}
        startIcon={isRecording ? <StopIcon /> : <MicIcon />}
        onClick={isRecording ? stopRecording : startRecording}
      >
        {isRecording ? "녹음 중지" : "녹음 시작"}
      </Button>
      {transcription && (
        <Typography variant="body1" style={{ marginTop: '20px' }}>
          인식된 텍스트: {transcription}
        </Typography>
      )}
    </Box>
  );
};

export default AudioRecorder;
