// AudioRecorder.jsx

// React 훅과 MUI 컴포넌트 import
import { useState, useRef, useEffect } from 'react';
import { Button, Box, Typography } from '@mui/material';
import MicIcon from '@mui/icons-material/Mic';
import StopIcon from '@mui/icons-material/Stop';

// Blob 데이터를 Base64로 인코딩하는 함수
const blobToArrayBuffer = (blob) => {
  return new Promise((resolve, reject) => {
    const reader = new FileReader();
    // 파일 읽기 완료 시 ArrayBuffer 반환
    reader.onloadend = () => resolve(reader.result);
    // 파일 읽기 중 오류 발생 시 reject 호출
    reader.onerror = reject;
    reader.readAsArrayBuffer(blob);
  });
};

// 음성 녹음 컴포넌트
const AudioRecorder = () => {
  // 상태 관리: 녹음 중 여부, 변환 텍스트, WebSocket 연결 여부
  const [isRecording, setIsRecording] = useState(false);
  const [transcription, setTranscription] = useState('');
  const [isConnected, setIsConnected] = useState(false);

  // 참조 변수 관리: 미디어 레코더, 웹소켓, 오디오 조각, 오디오 엘리먼트
  const mediaRecorderRef = useRef(null);
  const websocketRef = useRef(null);
  const audioChunksRef = useRef([]);
  const audioElementRef = useRef(null);
  const startTimeRef = useRef(null);

  // WebSocket 연결 함수
  const connectWebSocket = async () => {
    try {
      // 서버로부터 WebSocket 세션 URL 가져오기
      const res = await fetch('http://localhost:8080/api/v1/voice/session', {
        headers: {
          'Authorization': `Bearer ${localStorage.getItem('accessToken')}`
        }
      });

      // 응답 확인: 오류 발생 시 예외 처리
      if (!res.ok) {
        throw new Error('웹소켓 세션 정보를 가져오는 데 실패했습니다.');
      }

      // WebSocket URL 파싱
      const { wsUrl } = await res.json();
      console.log('웹소켓 URL:', wsUrl);

      // WebSocket 객체 생성
      const ws = new WebSocket(wsUrl);

      // WebSocket 연결 성공 시 콜백
      ws.onopen = () => {
        console.log('WebSocket 연결 성공');
        setIsConnected(true);
      };

      // WebSocket으로부터 메시지 수신 처리
      ws.onmessage = (event) => {
        if (typeof event.data === 'string') {
          try {
            // 텍스트 메시지 파싱
            const data = JSON.parse(event.data);
            if (data.transcription) {
              setTranscription(data.transcription);
              console.log('받은 텍스트:', data.transcription);
            }
          } catch (error) {
            console.error("메시지 파싱 에러:", error);
          }
        } else {
          // 오디오 응답 처리
          handleAudioResponse(event.data);
        }
      };

      // WebSocket 연결 종료 처리
      ws.onclose = () => {
        console.log('WebSocket 연결 종료');
        setIsConnected(false);
        // 자동 재연결 시도 (3초 후)
        setTimeout(connectWebSocket, 3000);
      };

      // WebSocket 오류 처리
      ws.onerror = (error) => {
        console.error('WebSocket 에러:', error);
        setIsConnected(false);
      };

      // WebSocket 객체 저장
      websocketRef.current = ws;
    } catch (err) {
      console.error('WebSocket 초기 연결 실패:', err);
    }
  };

  // 오디오 응답 처리 함수
  const handleAudioResponse = async (audioBlob) => {
    try {
      // 오디오 엘리먼트에 Blob URL 설정 및 재생
      if (audioElementRef.current) {
        audioElementRef.current.src = URL.createObjectURL(
          new Blob([audioBlob], { type: 'audio/webm' })
        );
        audioElementRef.current.play();
        console.log("오디오 응답 재생");
      }
    } catch (error) {
      console.error("오디오 응답 처리 에러:", error);
    }
  };

  // 오디오 데이터를 WebSocket으로 전송
  const sendAudioToWebSocket = async (audioBlob) => {
    try {
      const arrayBuffer = await blobToArrayBuffer(audioBlob);

      // WebSocket 연결 확인 후 데이터 전송
      if (websocketRef.current && websocketRef.current.readyState === WebSocket.OPEN) {
        websocketRef.current.send(arrayBuffer);
        console.log("✅ 바이너리 오디오 데이터 전송 완료");
      } else {
        console.error("❌ WebSocket이 열려 있지 않습니다.");
      }
    } catch (error) {
      console.error("❌ 오디오 데이터 전송 중 오류:", error);
    }
  };

  // 녹음 시작 함수
  const startRecording = async () => {
    try {
      const stream = await navigator.mediaDevices.getUserMedia({ audio: true });
      const options = { mimeType: 'audio/webm; codecs=opus' };
      const mediaRecorder = new MediaRecorder(stream, options);
      mediaRecorderRef.current = mediaRecorder;

      // 녹음 시작 시간 저장
      startTimeRef.current = Date.now();

      // 녹음 데이터가 수신될 때 처리
      mediaRecorder.ondataavailable = async (event) => {
        if (event.data.size > 0) {
          console.log("📥 오디오 데이터 수신:", event.data.size, "바이트");
          await sendAudioToWebSocket(event.data);
        }
      };
      mediaRecorder.onstop = () => {
        const endTime = Date.now();
        const durationInSeconds = ((endTime - startTimeRef.current) / 1000).toFixed(1);

        const message = {
          type: 'silent',
          content:{
            action: 'audio_complete',
            duration: parseFloat(durationInSeconds),
            has_speech: true
          }
        };

        if (websocketRef.current && websocketRef.current.readyState === WebSocket.OPEN) {
          websocketRef.current.send(JSON.stringify(message));
          console.log("🟡 모든 오디오 전송 후 메시지 전송:", message);
        } else {
          console.error("❌ WebSocket이 열려 있지 않아서 메시지를 보낼 수 없습니다.");
        }
      };

      mediaRecorder.start(100);
      setIsRecording(true);
      console.log("녹음 시작");
    } catch (error) {
      console.error("녹음 시작 에러:", error);
    }
  };

  // 녹음 중지 함수
  const stopRecording = () => {
    if (mediaRecorderRef.current) {
      mediaRecorderRef.current.stop();
      setIsRecording(false);
      console.log("녹음 중지");
    }
  };

  // 컴포넌트가 마운트될 때 WebSocket 연결 시도
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
        <Typography variant="body1">인식된 텍스트: {transcription}</Typography>
      )}
      <audio ref={audioElementRef} hidden />
    </Box>
  );
};

export default AudioRecorder;
