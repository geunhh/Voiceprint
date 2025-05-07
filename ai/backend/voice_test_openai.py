from openai import OpenAI

import pyaudio
import collections
from pydub import AudioSegment
import wave
import os
import tempfile
from dotenv import load_dotenv
import time

# TTS test
from pydub import AudioSegment
from pydub.playback import play
from io import BytesIO

# webrtcvad-wheels 패키지 사용 (Python 3.13 호환)
try:
    import webrtcvad_wheels as webrtcvad
except ImportError:
    # 설치되지 않은 경우 안내
    print("webrtcvad_wheels 패키지를 설치해주세요: pip install webrtcvad-wheels")
    # 대체 방법으로 기존 webrtcvad 사용 시도
    try:
        import webrtcvad
    except ImportError:
        print("호환되는 VAD 패키지가 필요합니다.")
        exit(1)

load_dotenv()

client = OpenAI()

current_dir = os.getcwd()
file_path = os.path.join(current_dir, "output.mp3")

# pyaudio 설정
MIC_DEVICE_ID = 11  # 실제 장치 ID로 확인 필요
SAMPLE_RATE = 16000
CHANNELS = 1
CHUNK = 320
CHUNK_DURATION_MS = 20
FORMAT = pyaudio.paInt16
RATE = 16000
SILENCE_THRESHOLD = 15
RING_BUFFER_SIZE = 30

# 오디오 재생 함수 - 도커 환경 고려
def play_audio(file_path):
    """
    여러 방법으로 오디오 파일 재생을 시도합니다.
    도커 환경에서 작동하도록 여러 대안을 제공합니다.
    """
    # 방법 1: 브라우저를 통한 재생 (웹서버가 실행 중인 경우)
    try:
        print(f"오디오 파일이 준비되었습니다: {file_path}")
        print("재생 중...")
        
        # 방법 2: pydub + simpleaudio로 재생 시도
        try:
            from pydub import AudioSegment
            from pydub.playback import play
            
            sound = AudioSegment.from_file(file_path, format="mp3")
            play(sound)
            return True
        except Exception as e:
            print(f"pydub 재생 실패: {e}")
        
        # 방법 3: ffplay 시도 (도커에 ffmpeg가 설치된 경우)
        try:
            os.system(f"ffplay -nodisp -autoexit -hide_banner -loglevel error {file_path}")
            return True
        except Exception as e:
            print(f"ffplay 재생 실패: {e}")
            
        # 방법 4: mpg123 사용 (fallback)
        try:
            os.system(f"mpg123 {file_path}")
            return True
        except Exception as e:
            print(f"mpg123 재생 실패: {e}")
            
        # 파일 생성만 성공한 경우
        print(f"오디오 파일이 {file_path}에 저장되었습니다만, 재생에 실패했습니다.")
        print("도커 환경에서 오디오 장치 접근이 제한되었을 수 있습니다.")
        return False
        
    except Exception as e:
        print(f"오디오 재생 실패: {e}")
        return False

# VAD 초기화
vad = webrtcvad.Vad()
vad.set_mode(3)  # 감도 설정 (0-3)

# 오디오 스트림 설정 (오류 시 대체 코드)
try:
    p = pyaudio.PyAudio()
    
    # 사용 가능한 오디오 장치 리스트
    info = p.get_host_api_info_by_index(0)
    numdevices = info.get('deviceCount')
    
    print("\n사용 가능한 오디오 입력 장치:")
    for i in range(0, numdevices):
        device_info = p.get_device_info_by_index(i)
        if device_info.get('maxInputChannels') > 0:
            print(f"장치 ID {i}: {device_info.get('name')}")
            
    # MIC_DEVICE_ID 장치가 존재하는지 확인
    device_count = p.get_device_count()
    if MIC_DEVICE_ID >= device_count:
        print(f"경고: ID {MIC_DEVICE_ID}인 마이크를 찾을 수 없습니다. 기본 입력 장치를 사용합니다.")
        MIC_DEVICE_ID = p.get_default_input_device_info()['index']
        
    # 스트림 열기 (특정 장치 ID 사용)
    stream = p.open(
        format=FORMAT,
        channels=CHANNELS,
        rate=RATE,
        input=True,
        input_device_index=MIC_DEVICE_ID,
        frames_per_buffer=CHUNK
    )
except Exception as e:
    print(f"오디오 초기화 오류: {e}")
    print("기본 입력 장치로 시도합니다...")
    try:
        p = pyaudio.PyAudio()
        stream = p.open(
            format=FORMAT,
            channels=CHANNELS,
            rate=RATE,
            input=True,
            frames_per_buffer=CHUNK
        )
    except Exception as e2:
        print(f"기본 입력 장치도 실패: {e2}")
        print("도커 환경에서 실행 중인 경우, 호스트의 오디오 장치를 컨테이너와 공유해야 합니다.")
        print("docker run --device /dev/snd:/dev/snd -v /run/user/1000/pulse:/run/user/1000/pulse -e PULSE_SERVER=unix:/run/user/1000/pulse/native ...")
        exit(1)

ring_buffer = collections.deque(maxlen=RING_BUFFER_SIZE)
silence_frames = []
recorded_frames = []
triggered = False
recording = False

print("음성 인식 준비가 완료되었습니다. 말씀해주세요...")

try:
    while True:
        # 데이터 읽기
        try:
            data = stream.read(CHUNK, exception_on_overflow=False)
        except Exception as e:
            print(f"오디오 스트림 읽기 오류: {e}")
            time.sleep(0.1)
            continue

        # VAD로 말하는 중인지 확인
        try:
            is_speech = vad.is_speech(data, RATE)
        except Exception as e:
            print(f"VAD 처리 오류: {e}")
            is_speech = False

        # 음성 감지 상태 관리
        if not triggered:
            ring_buffer.append((data, is_speech))
            num_voiced = len([f for f, speech in ring_buffer if speech])

            # 버퍼의 90%가 음성으로 감지되면 녹음 시작
            if num_voiced > 0.9 * RING_BUFFER_SIZE:
                triggered = True
                recording = True
                print("음성 감지됨 - 녹음 시작")

                # 링 버퍼에 있던 이전 데이터도 녹음에 포함
                recorded_frames = [f[0] for f in ring_buffer]
                ring_buffer.clear()

        else:
            recorded_frames.append(data)

            # 음성이 계속 감지되면 침묵 카운터 초기화
            if is_speech:
                silence_frames = 0
            # 침묵이 감지되면 카운터 증가
            else:
                silence_frames += 1
                
            # 침묵이 일정 시간 지속되면 녹음 중단
            if silence_frames > SILENCE_THRESHOLD:
                triggered = False
                recording = False
                print("침묵 감지됨 - 녹음 중단")

                # 임시 파일 생성 (파일 충돌 방지)
                with tempfile.NamedTemporaryFile(suffix='.wav', delete=False) as temp_file:
                    temp_wav_path = temp_file.name
                
                # WAV 파일로 저장
                wf = wave.open(temp_wav_path, "wb")
                wf.setnchannels(CHANNELS)
                wf.setsampwidth(p.get_sample_size(FORMAT))
                wf.setframerate(RATE)
                wf.writeframes(b''.join(recorded_frames))
                wf.close()

                # dBFS 측정 후 -20dBFS로 정규화
                try:
                    sound = AudioSegment.from_file(temp_wav_path, format="wav")
                    current_dbfs = sound.dBFS
                    target_dbfs = -20.0
                    change_in_dBFS = target_dbfs - current_dbfs
                    sound = sound.apply_gain(change_in_dBFS)
                    sound.export(temp_wav_path, format="wav")
                except Exception as e:
                    print(f"오디오 정규화 오류: {e}")

                # OpenAI STT 처리
                try:
                    audio_file = open(temp_wav_path, "rb")
                    transcript = client.audio.transcriptions.create(
                        file=audio_file,
                        model="whisper-1",
                        language="ko",
                        temperature=0.7
                    )
                    audio_file.close()
                    
                    # 임시 파일 삭제
                    os.unlink(temp_wav_path)
                    
                    # print(f"인식된 텍스트: {transcript.text}")
                    
                    # if not transcript.text.strip():
                    #     print("인식된 텍스트가 없습니다. 다시 시도해주세요.")
                    #     recorded_frames = []
                    #     silence_frames = 0
                    #     continue
                        
                except Exception as e:
                    print(f"음성 인식 오류: {e}")
                    if os.path.exists(temp_wav_path):
                        os.unlink(temp_wav_path)
                    recorded_frames = []
                    silence_frames = 0
                    continue

                # OPENAI LLM 응답 생성
                try:
                    response = client.chat.completions.create(
                        model="gpt-3.5-turbo",
                        messages=[
                            {
                                "role": "system",
                                "content": "당신은 친절한 AI 어시스턴트입니다. 항상 명확하고 간결하게 한국어로 대답해주세요. 이모티콘이나 특수문자 등 발음이 불가능한 말은 하지 않습니다."
                            },
                            {
                                "role": "user",
                                "content": transcript.text,
                            }
                        ],
                        temperature=0.5,
                        max_tokens=500,
                        frequency_penalty=0.5,
                    )
                    
                    answer = response.choices[0].message.content
                    print(f"AI 응답: {answer}")
                    
                except Exception as e:
                    print(f"AI 응답 생성 오류: {e}")
                    answer = "죄송합니다, 응답을 생성하는 중에 오류가 발생했습니다. 다시 시도해주세요."

                # 음성 합성 (TTS)
                try:
                    stt_answer = client.audio.speech.create(
                        model="tts-1",
                        voice="alloy",
                        input=answer,
                        speed=1,
                        response_format='mp3'
                    )
                
                    audio_data = stt_answer.content
                    print(audio_data)

                    audio_segment = AudioSegment.from_file(BytesIO(audio_data), format="mp3")
                    try : 
                        play(audio_segment)
                        print("오디오 직접 재생 완료")
                    except Exception as e:
                        print(f"pydub 직접 재생 실패: {e}")

                    
                except Exception as e:
                    print(f"음성 합성 및 재생 오류: {e}")
            

except KeyboardInterrupt:
    print("\n프로그램 종료...")
finally:
    if 'stream' in locals() and stream:
        stream.stop_stream()

        stream.close()
    if 'p' in locals() and p:
        p.terminate()
    print("오디오 자원이 정리되었습니다.")