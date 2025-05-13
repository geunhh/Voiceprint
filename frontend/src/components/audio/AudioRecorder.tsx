// AudioRecorder.tsx
// 기존 AudioRecorder.jsx를 TypeScript로 변환 (주석 유지)

import React, { useEffect, useRef, useState } from "react";
import { FaMicrophone, FaStop } from "react-icons/fa";
import { ImSpinner2 } from "react-icons/im";
import { useDispatch } from "react-redux";
import axiosInstance from "../../api/axiosInstance";
import { setCharacter } from "../../store/characterSlice";

import { useNavigate } from "react-router-dom";
import Button from "../common/Button";

import ProgressBar from "../common/ProgressBar";
import AlertModal from "../modal/AlertModal";
import DiaryCreateFailModal from "../modal/DiaryCreateFailModal";
import DiaryCreatingModal from "../modal/DiaryCreatingModal";

// 로컬 기본 아이콘
import chatBlack from "../../assets/icons/chatBlack.png";
import chatBlue from "../../assets/icons/chatBlue.png";
import chatPink from "../../assets/icons/chatPink.png";
import chatRed from "../../assets/icons/chatRed.png";
import chatYellow from "../../assets/icons/chatYellow.png";

// MediaRecorder 확장 모델에 autoSend 속성 추가
interface ExtendedMediaRecorder extends MediaRecorder {
  autoSend?: boolean;
}

// 처리 통계 타입 정의
interface ProcessingStats {
  recordCount: number;
  sentCount: number;
  totalAudioTime: number; // ms 단위
}

export interface AudioRecorderHandle {
  stopRecording: () => void;
}

// 상태 값 타입
type RecorderStatus =
  | "idle"
  | "recording"
  | "전송 중..."
  | "처리 중..."
  | "error";

const AudioRecorder: React.FC = () => {
  const navigate = useNavigate();
  const dispatch = useDispatch();

  // 캐릭터(챗봇) 정보 ------------------------------------------------
  const [character, setCharacterState] = useState({
    id: 0,
    img: chatYellow, // 기본 햇살이
    name: "햇살이",
    tag: "기본 캐릭터",
  });
  const localIcons: Record<string, string> = {
    따분이: chatBlack,
    맑음이: chatBlue,
    설렘이: chatPink,
    열정이: chatRed,
    햇살이: chatYellow,
  };

  const [isRecording, setIsRecording] = useState<boolean>(false);
  const [status, setStatus] = useState<RecorderStatus>("idle");
  const [transcription, setTranscription] = useState<string>("");
  const [isConnected, setIsConnected] = useState<boolean>(false);
  const [isSpeaking, setIsSpeaking] = useState<boolean>(false);
  const [processingStats, setProcessingStats] = useState<ProcessingStats>({
    recordCount: 0,
    sentCount: 0,
    totalAudioTime: 0,
  });

  // 고정된 VAD 설정값
  const silenceTimeout = 1500; // 말소리가 없을 때 타임아웃(ms)
  const volumeThreshold = 15; // 볼륨 임계값(dB)

  const mediaRecorderRef = useRef<ExtendedMediaRecorder | null>(null);
  const websocketRef = useRef<WebSocket | null>(null);
  const audioChunksRef = useRef<Blob[]>([]);
  const audioElementRef = useRef<HTMLAudioElement | null>(null); // 숨겨진 오디오 요소 참조
  const isRecordingRef = useRef<boolean>(false);
  const lastSpeechTimeRef = useRef<number | null>(null);
  const vadTimeoutRef = useRef<number | null>(null);
  const recordStartTimeRef = useRef<number | null>(null);
  const isSpeakingRef = useRef<boolean>(false); // isSpeaking을 ref로도 관리

  // VAD 관련 참조
  const audioContextRef = useRef<AudioContext | null>(null);
  const analyserRef = useRef<AnalyserNode | null>(null);
  const microphoneStreamRef = useRef<MediaStreamAudioSourceNode | null>(null);
  const dataArrayRef = useRef<Uint8Array | null>(null);

  // ────────────────────────────────────────────────────────────────
  // 1. 최근 챗봇 정보 로드 (+fallback)
  // ────────────────────────────────────────────────────────────────
  useEffect(() => {
    const fetchRecent = async () => {
      try {
        const res = await axiosInstance.get("/api/chatbot");

        const { recentChatbotId, chatbots } = res.data.data;
        const bot =
          chatbots.find((b: any) => b.id === recentChatbotId) || chatbots[0];

        const img = bot.imageUrl || localIcons[bot.name] || chatYellow;
        const tag = bot.description.split(",").join(" ");

        // slice 구조(img / name / tag)에 맞게
        const characterObj = { img, name: bot.name, tag };

        setCharacterState(characterObj); // 로컬 상태
        dispatch(setCharacter(characterObj)); // 전역 상태
      } catch (err) {
        console.error("챗봇 정보 실패, 기본 캐릭터 사용", err);
        const fallback = {
          img: chatYellow,
          name: "햇살이",
          tag: "기본 캐릭터",
        };
        setCharacterState(fallback);
        dispatch(setCharacter(fallback));
      }
    };
    fetchRecent();
  }, [dispatch]);

  // 오디오 요소 생성
  useEffect(() => {
    // 숨겨진 오디오 요소 생성
    const audioElement = new Audio();
    audioElement.addEventListener("ended", () => {
      console.log("응답 오디오 재생 완료, 자동 녹음 재시작");
      setTimeout(() => {
        if (!isRecordingRef.current) {
          startRecording();
        }
      }, 500);
    });
    audioElementRef.current = audioElement;

    return () => {
      // 이벤트 리스너 정리
      if (audioElementRef.current) {
        audioElementRef.current.pause();
        audioElementRef.current.src = "";
        audioElementRef.current.removeEventListener("ended", () => {});
      }
    };
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, []);

  // WebSocket 연결 설정
  useEffect(() => {
    // 웹소켓 서버 URL - 실제 서버 URL로 변경 필요
    const wsUrl = "wss://mdia4kmn4s6kmw-8000.proxy.runpod.net/ws";

    const connectWebSocket = () => {
      const ws = new WebSocket(wsUrl);

      ws.onopen = () => {
        console.log("WebSocket 연결 성공");
        setIsConnected(true);
      };

      ws.onmessage = (event: MessageEvent) => {
        if (typeof event.data === "string") {
          try {
            const data = JSON.parse(event.data);
            if (data.transcription) {
              setTranscription(data.transcription as string);
            }
          } catch (error) {
            console.error("메시지 파싱 에러:", error);
          }
        } else {
          handleAudioResponse(event.data as Blob);
        }
      };

      ws.onclose = () => {
        console.log("WebSocket 연결 종료");
        setIsConnected(false);
        // 재연결 시도
        setTimeout(connectWebSocket, 3000);
      };

      ws.onerror = (error) => {
        console.error("WebSocket 에러:", error);
        setIsConnected(false);
      };

      websocketRef.current = ws;
    };

    connectWebSocket();

    // 컴포넌트 언마운트 시 웹소켓 연결 종료
    return () => {
      websocketRef.current?.close();
    };
  }, []);

  // WebSocket이 열리면 자동으로 녹음 시작
  useEffect(() => {
    if (isConnected && !isRecordingRef.current) startRecording();
  }, [isConnected]);

  // 오디오 응답 처리 - 즉시 재생
  const handleAudioResponse = async (audioBlob: Blob) => {
    try {
      // 기존 URL 정리
      if (audioElementRef.current && audioElementRef.current.src) {
        URL.revokeObjectURL(audioElementRef.current.src);
      }

      // 오디오 블롭으로 URL 생성
      const url = URL.createObjectURL(
        new Blob([audioBlob], { type: "audio/mpeg" })
      );

      // 오디오 요소에 설정하고 즉시 재생
      if (audioElementRef.current) {
        audioElementRef.current.src = url;
        await audioElementRef.current.play();
        setIsSpeaking(false); // 응답 재생 시작 시 말하기 상태 초기화
      }
    } catch (error) {
      console.error("오디오 응답 처리 에러:", error);
    }
  };

  // 볼륨 레벨 계산 (dB 단위)
  const calculateVolume = (dataArray: Uint8Array) => {
    // 모든 값이 0인지 먼저 확인
    let allZero = true;
    for (let i = 0; i < dataArray.length; i++) {
      if (dataArray[i] > 0) {
        allZero = false;
        break;
      }
    }

    // 모든 값이 0이면 매우 낮은 값 반환
    if (allZero) {
      return -100; // 매우 낮은 dB 값
    }

    let sum = 0;
    for (let i = 0; i < dataArray.length; i++) {
      sum += dataArray[i] * dataArray[i];
    }

    const rms = Math.sqrt(sum / dataArray.length);
    // 0이 아닌 값으로 나누기 위한 안전장치 (최소값을 더 높게 설정)
    const db = 20 * Math.log10(Math.max(rms, 1) / 128);
    return db;
  };

  // 침묵 감지 및 녹음 중지 함수
  const checkSilence = () => {
    console.log("침묵 감지 함수 호출됨");

    // 이전 타이머가 있으면 취소
    if (vadTimeoutRef.current) {
      clearTimeout(vadTimeoutRef.current);
      vadTimeoutRef.current = null;
    }

    // 중요: 여기서 ref 값을 사용하여 상태 확인
    if (isRecordingRef.current && !isSpeakingRef.current) {
      console.log(`침묵 감지, ${silenceTimeout}ms 후 녹음 중지 예정`);
      vadTimeoutRef.current = window.setTimeout(() => {
        console.log(`침묵이 ${silenceTimeout}ms 동안 지속됨, 녹음 중지`);
        stopRecording();
      }, silenceTimeout);
    }
  };

  // 음성 활동 감지 설정
  const setupVoiceActivityDetection = (stream: MediaStream) => {
    // 기존 오디오 컨텍스트 정리
    if (audioContextRef.current) {
      try {
        if (microphoneStreamRef.current && analyserRef.current) {
          microphoneStreamRef.current.disconnect(analyserRef.current);
        }
        audioContextRef.current
          .close()
          .catch((err) => console.error("AudioContext 종료 에러:", err));
      } catch (err) {
        console.error("AudioContext 정리 중 오류:", err);
      }
      audioContextRef.current = null;
    }

    const audioContext = new (window.AudioContext ||
      (window as any).webkitAudioContext)();
    audioContextRef.current = audioContext;

    const microphone = audioContext.createMediaStreamSource(stream);
    microphoneStreamRef.current = microphone;

    const analyser = audioContext.createAnalyser();
    analyser.fftSize = 256;
    analyser.smoothingTimeConstant = 0.5;
    analyserRef.current = analyser;

    microphone.connect(analyser);

    const dataArray = new Uint8Array(analyser.frequencyBinCount);
    dataArrayRef.current = dataArray;

    const checkAudioLevel = () => {
      // 녹음 중이 아니면 분석 중지
      if (!isRecordingRef.current) {
        console.log("녹음 중이 아님, VAD 중지");
        return;
      }

      analyser.getByteFrequencyData(dataArray);
      const volume = calculateVolume(dataArray);
      const speaking = volume > -volumeThreshold;

      // 말하기 상태가 변경됐을 때만 상태 업데이트 및 로깅
      if (speaking !== isSpeakingRef.current) {
        console.log(
          `음성 상태 변경: ${speaking ? "말하는 중" : "침묵 중"}, 볼륨: ${volume.toFixed(2)}dB`
        );

        // 중요: React 상태와 ref 모두 업데이트
        const prevSpeaking = isSpeakingRef.current;
        isSpeakingRef.current = speaking;
        setIsSpeaking(speaking);

        if (speaking) {
          console.log("말하기 시작 감지됨");
          lastSpeechTimeRef.current = Date.now();
          // 말하기 시작하면 침묵 타이머 취소
          if (vadTimeoutRef.current) {
            console.log("말하기 시작으로 타이머 취소");
            clearTimeout(vadTimeoutRef.current);
            vadTimeoutRef.current = null;
          }
        } else if (prevSpeaking) {
          // 이전에 말하고 있었다면
          console.log("말하기 종료 감지됨");
          // 말하기 끝나면 침묵 감지 시작
          checkSilence();
        }
      }

      // 녹음 중이면 계속 체크
      if (isRecordingRef.current) {
        requestAnimationFrame(checkAudioLevel);
      }
    };

    requestAnimationFrame(checkAudioLevel);
  };

  // 녹음 시작
  const startRecording = async () => {
    try {
      // 이미 녹음 중이면 중복 실행 방지
      if (isRecordingRef.current) {
        console.log("이미 녹음 중입니다.");
        return;
      }

      setStatus("recording");
      audioChunksRef.current = [];
      recordStartTimeRef.current = Date.now(); // 녹음 시작 시간 기록

      // 이전 녹음 정리
      if (mediaRecorderRef.current && mediaRecorderRef.current.stream) {
        mediaRecorderRef.current.stream
          .getTracks()
          .forEach((track) => track.stop());
        mediaRecorderRef.current = null;
      }

      // VAD 타이머 정리
      if (vadTimeoutRef.current) {
        clearTimeout(vadTimeoutRef.current);
        vadTimeoutRef.current = null;
      }

      const stream = await navigator.mediaDevices.getUserMedia({ audio: true });
      const options: MediaRecorderOptions = {
        mimeType: "audio/webm",
        audioBitsPerSecond: 16000,
      };

      const mediaRecorder: ExtendedMediaRecorder = new MediaRecorder(
        stream,
        options
      );
      mediaRecorderRef.current = mediaRecorder;

      // 중요: ref 상태를 먼저 업데이트하여 VAD에서 즉시 참조할 수 있도록 함
      isRecordingRef.current = true;
      isSpeakingRef.current = false; // 초기 말하기 상태는 false로 설정
      // 그 다음 React 상태 업데이트
      setIsRecording(true);
      setIsSpeaking(false);

      setupVoiceActivityDetection(stream);

      mediaRecorder.ondataavailable = (event: BlobEvent) => {
        if (event.data.size > 0) {
          audioChunksRef.current.push(event.data);
        }
      };

      mediaRecorder.onstop = () => {
        // 녹음 시간 계산
        const recordDuration = recordStartTimeRef.current
          ? Date.now() - recordStartTimeRef.current
          : 0;

        // 통계 업데이트
        setProcessingStats((prev) => ({
          ...prev,
          recordCount: prev.recordCount + 1,
          totalAudioTime: prev.totalAudioTime + recordDuration,
        }));

        const audioBlob = new Blob(audioChunksRef.current, {
          type: "audio/webm",
        });

        // 오디오 블롭 크기가 너무 작으면(무의미한 소음만 있는 경우) 서버 전송 스킵
        const minMeaningfulSize = 1000; // 바이트 단위
        const hasMeaningfulAudio = audioBlob.size > minMeaningfulSize;

        const audioContextToClose = audioContextRef.current;

        // 의미 있는 오디오가 있고, 자동 전송 모드일 때만 서버로 전송
        if (
          hasMeaningfulAudio &&
          audioChunksRef.current.length > 0 &&
          websocketRef.current &&
          websocketRef.current.readyState === WebSocket.OPEN &&
          (!("autoSend" in mediaRecorder) ||
            (mediaRecorder as ExtendedMediaRecorder).autoSend)
        ) {
          console.log(
            "전체 오디오 데이터를 서버로 전송합니다. 크기:",
            audioBlob.size,
            "녹음 시간:",
            recordDuration,
            "ms"
          );
          setStatus("전송 중...");

          try {
            websocketRef.current.send(audioBlob);
            console.log("오디오 블롭 전송 완료");

            // 통계 업데이트 - 전송 횟수 증가
            setProcessingStats((prev) => ({
              ...prev,
              sentCount: prev.sentCount + 1,
            }));

            // 오디오 전송 완료 신호 보내기
            setTimeout(() => {
              if (
                websocketRef.current &&
                websocketRef.current.readyState === WebSocket.OPEN
              ) {
                websocketRef.current.send(
                  JSON.stringify({
                    action: "audio_complete",
                    duration: recordDuration,
                    has_speech: hasMeaningfulAudio,
                  })
                );
                console.log("audio_complete 메시지 전송 완료");
                setStatus("idle");
              } else {
                console.error("완료 메시지 전송 실패: 웹소켓 연결 상태 변경됨");
                setStatus("error");
              }
            }, 100);
          } catch (err) {
            console.error("오디오 데이터 전송 중 오류:", err);
            setStatus("error");
          }
        } else {
          if (!hasMeaningfulAudio) {
            console.log(
              "의미 있는 오디오가 감지되지 않아 서버 전송을 건너뜁니다."
            );
          } else if (
            "autoSend" in mediaRecorder &&
            !(mediaRecorder as ExtendedMediaRecorder).autoSend
          ) {
            console.log(
              "자동 전송 모드가 비활성화되어 서버 전송을 건너뜁니다."
            );
          }
          setStatus("idle");
        }

        // 오디오 컨텍스트 정리
        if (audioContextToClose) {
          try {
            if (microphoneStreamRef.current && analyserRef.current) {
              microphoneStreamRef.current.disconnect(analyserRef.current);
            }
            audioContextToClose
              .close()
              .catch((err) => console.error("AudioContext 종료 에러:", err));
          } catch (err) {
            console.error("AudioContext 정리 중 오류:", err);
          }
          audioContextRef.current = null;
          microphoneStreamRef.current = null;
          analyserRef.current = null;
        }

        // 미디어 스트림 정리
        if (mediaRecorderRef.current && mediaRecorderRef.current.stream) {
          mediaRecorderRef.current.stream
            .getTracks()
            .forEach((track) => track.stop());
        }
        mediaRecorderRef.current = null;

        // VAD 타이머 정리
        if (vadTimeoutRef.current) {
          clearTimeout(vadTimeoutRef.current);
          vadTimeoutRef.current = null;
        }

        audioChunksRef.current = [];
        recordStartTimeRef.current = null;

        // 중요: ref 상태 먼저 업데이트
        isRecordingRef.current = false;
        isSpeakingRef.current = false;
        lastSpeechTimeRef.current = null;
        // 그 다음 React 상태 업데이트
        setIsRecording(false);
        setIsSpeaking(false);
      };

      mediaRecorder.start();
      console.log("녹음이 시작되었습니다.");
    } catch (error) {
      console.error("녹음 시작 에러:", error);
      setStatus("error");

      // 에러 발생 시 상태 초기화
      isRecordingRef.current = false;
      isSpeakingRef.current = false;
      recordStartTimeRef.current = null;
      setIsRecording(false);
      setIsSpeaking(false);
    }
  };

  // 녹음 중지
  const stopRecording = (autoSend: boolean = true) => {
    console.log(`녹음 중지 호출됨 (자동 전송: ${autoSend})`);

    // 이미 녹음 중이 아니면 중복 실행 방지
    if (!isRecordingRef.current || !mediaRecorderRef.current) {
      console.log("녹음 중이 아니거나 MediaRecorder가 없습니다.");
      setStatus("idle");
      return;
    }

    // VAD 타이머 정리
    if (vadTimeoutRef.current) {
      clearTimeout(vadTimeoutRef.current);
      vadTimeoutRef.current = null;
    }

    // 사용자 인터페이스 상태 업데이트
    setStatus("처리 중...");

    if (
      mediaRecorderRef.current &&
      mediaRecorderRef.current.state === "recording"
    ) {
      try {
        // autoSend 플래그를 저장하여 ondataavailable 이벤트에서 사용
        mediaRecorderRef.current.autoSend = autoSend;
        mediaRecorderRef.current.stop();
        console.log("MediaRecorder stop 호출됨");
      } catch (error) {
        console.error("녹음 중지 중 오류:", error);

        // 오류 발생 시 강제 정리
        if (mediaRecorderRef.current && mediaRecorderRef.current.stream) {
          mediaRecorderRef.current.stream
            .getTracks()
            .forEach((track) => track.stop());
        }
        mediaRecorderRef.current = null;

        if (audioContextRef.current) {
          try {
            if (microphoneStreamRef.current && analyserRef.current) {
              microphoneStreamRef.current.disconnect(analyserRef.current);
            }
            audioContextRef.current
              .close()
              .catch((err) => console.error("AudioContext 종료 에러:", err));
          } catch (err) {
            console.error("AudioContext 정리 중 오류:", err);
          }
          audioContextRef.current = null;
          microphoneStreamRef.current = null;
          analyserRef.current = null;
        }

        isRecordingRef.current = false;
        isSpeakingRef.current = false;
        lastSpeechTimeRef.current = null;
        setIsRecording(false);
        setIsSpeaking(false);
        setStatus("idle");
      }
    } else {
      console.log("MediaRecorder가 없거나 이미 녹음 중지 상태");
      setStatus("idle");
    }
  };

  // 컴포넌트 언마운트 시 정리
  useEffect(() => {
    return () => {
      // 오디오 URL 정리
      if (audioElementRef.current && audioElementRef.current.src) {
        URL.revokeObjectURL(audioElementRef.current.src);
      }

      // 미디어 스트림 정리
      if (mediaRecorderRef.current && mediaRecorderRef.current.stream) {
        mediaRecorderRef.current.stream
          .getTracks()
          .forEach((track) => track.stop());
      }

      // 오디오 컨텍스트 정리
      if (audioContextRef.current) {
        try {
          if (microphoneStreamRef.current && analyserRef.current) {
            microphoneStreamRef.current.disconnect(analyserRef.current);
          }
          audioContextRef.current
            .close()
            .catch((err) => console.error("AudioContext 종료 에러:", err));
        } catch (err) {
          console.error("AudioContext 정리 중 오류:", err);
        }
      }

      // VAD 타이머 정리
      if (vadTimeoutRef.current) {
        clearTimeout(vadTimeoutRef.current);
        vadTimeoutRef.current = null;
      }
    };
  }, []);

  // 모달 상태
  const [creatingModalOpen, setCreatingModalOpen] = useState(false);
  const [failModalOpen, setFailModalOpen] = useState(false);
  const [showConfirm, setShowConfirm] = useState(false);
  const [alert, setAlert] = useState<{
    message: string;
    type: "success" | "fail";
  } | null>(null);

  // 일기 생성 요청
  const handleCreate = async () => {
    setCreatingModalOpen(true);
    setShowConfirm(false);

    try {
      await axiosInstance.post("/api/chat/end");

      // 1.5초 후 확인 버튼 생성 + 4초 후 임시 저장으로 이동하기
      setTimeout(() => setShowConfirm(true), 0);
      setTimeout(() => {
        setCreatingModalOpen(false);
        navigate("/diary/temp");
      }, 0);
    } catch (err) {
      console.error("일기 생성 실패:", err);
      setCreatingModalOpen(false);
      setFailModalOpen(true);
    }
  };

  // 확인 버튼 클릭 시
  const handleConfirm = () => {
    setCreatingModalOpen(false);
    navigate("/diary/temp");
  };

  return (
    <div className="flex flex-col items-center justify-start min-h-screen px-4 pt-36 pb-36">
      <div className="flex flex-col items-center gap-8">
        {/* 진행바 예시 */}
        <div className="w-full max-w-[320px]">
          <ProgressBar label="" progress={30} />
        </div>

        {/* 캐릭터 애니메이션 */}
        <div
          className={`
    w-60 h-60 rounded-full flex items-center justify-center
    transition-transform duration-300
    ${isSpeaking ? "animate-shake" : "animate-float"}
  `}
        >
          <img
            src={character.img}
            alt={character.name}
            className="w-64 h-64 object-contain"
          />
        </div>

        {/* 녹음/중지 버튼 */}
        <div className="flex gap-4 mb-4">
          {!isRecording ? (
            <button
              onClick={startRecording}
              disabled={
                !isConnected || status === "loading" || status === "전송 중..."
              }
              className="flex items-center gap-2 px-4 py-2 bg-blue-600 text-white rounded disabled:opacity-50"
            >
              <FaMicrophone /> 녹음 시작
            </button>
          ) : (
            <button
              onClick={() => stopRecording(true)}
              className="flex items-center gap-2 px-4 py-2 bg-red-600 text-white rounded"
            >
              <FaStop /> 녹음 중지
            </button>
          )}
        </div>
      </div>
      {/* 상태 표시 (스피너 + 텍스트) */}
      {status !== "idle" && (
        <div className="flex items-center gap-2">
          <ImSpinner2
            className={`animate-spin text-xl ${
              status === "recording" ? "text-red-500" : "text-blue-500"
            }`}
          />
          <span className="text-sm">
            {status === "recording"
              ? isSpeaking
                ? "말하는 중..."
                : "침묵 감지 중..."
              : status === "전송 중..."
                ? "서버로 전송 중..."
                : status === "처리 중..."
                  ? "오디오 처리 중..."
                  : status}
          </span>
        </div>
      )}

      {/* 음성 인식 결과 */}
      {transcription && (
        <div className="w-full max-w-xl p-4 border rounded bg-white shadow">
          <h2 className="font-semibold mb-2">음성 인식 결과:</h2>
          <p>{transcription}</p>
        </div>
      )}

      {/* 종료 버튼 */}
      <div className="fixed bottom-[20vh] left-1/2 -translate-x-1/2 w-[90vw] max-w-[320px] flex justify-center">
        <Button
          text="일기 생성하기"
          type="fill"
          size="L"
          onClick={handleCreate}
        />
      </div>
      {/* 모달들 */}
      {creatingModalOpen && (
        <DiaryCreatingModal
          showConfirm={showConfirm}
          onConfirm={handleConfirm}
        />
      )}
      {failModalOpen && (
        <DiaryCreateFailModal
          onClose={() => setFailModalOpen(false)}
          onRetry={() => {
            setFailModalOpen(false);
            handleCreate();
          }}
        />
      )}

      {alert && (
        <AlertModal
          message={alert.message}
          type={alert.type}
          onClose={() => setAlert(null)}
        />
      )}
    </div>
  );
};

export default AudioRecorder;
