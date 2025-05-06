from fastapi import FastAPI, WebSocket, WebSocketDisconnect, Depends, HTTPException
from fastapi.middleware.cors import CORSMiddleware
import webrtcvad
import tempfile
from openai import OpenAI
import os
from dotenv import load_dotenv
import wave
import io
import subprocess
import json
import openai
from starlette.websockets import WebSocketState
import redis
import asyncio
from schema import Chat, MyChat, PromtTest
from typing import Annotated
import datetime

app = FastAPI()
r = redis.Redis(host="localhost", port=6379, db=0, decode_responses=True)

# 백엔드 origin 으로 변경 필요 
origins = [
    "http://localhost",
    "http://localhost:8080",
    "http://localhost:5173",
]

app.add_middleware(
    CORSMiddleware,
    allow_origins=origins,
    allow_credentials=True,
    allow_methods=["*"],
    allow_headers=["*"],
)


load_dotenv()

# openai 키 설정
client = OpenAI(api_key=os.getenv("OPENAI_API_KEY"))

# 오디오 설정
CHANNELS = 1
RATE = 16000
SAMPLE_WIDTH = 2  # 16비트 = 2바이트

async def verify_API(token : Annotated) : 
    if token != os.getenv("BACKEND_API") : 
        raise HTTPException(status_code=400, detail="TOKEN INVALID. USE CORRECT TOKEN TO ACCESS")



def convert_webm_to_wav(webm_bytes):
    """WebM 형식의 바이너리 데이터를 WAV로 변환"""
    try:
        process = subprocess.Popen(
            ['ffmpeg', '-i', 'pipe:0', '-f', 'wav', '-ar', '16000', '-ac', '1', 'pipe:1'],
            stdin=subprocess.PIPE, stdout=subprocess.PIPE, stderr=subprocess.PIPE
        )
        wav_data, err = process.communicate(input=webm_bytes)
        if process.returncode != 0:
            print("ffmpeg 변환 오류:", err.decode())
            return None
        return io.BytesIO(wav_data)
    except Exception as e:
        print(f"변환 중 오류 발생: {e}")
        return None

def stt(audio_data):
    if not audio_data:
        print("오디오 데이터가 없습니다.")
        return None
    
    try:
        # WebM에서 WAV로 변환
        wav_io = convert_webm_to_wav(audio_data)
        if wav_io is None:
            print("WAV 변환 실패")
            return None
        
        # 파일명 설정 (OpenAI API 요구사항)
        wav_io.name = "audio.wav"
        
        # OpenAI Whisper API로 음성 인식
        transcript = client.audio.transcriptions.create(
            file=wav_io,
            model="whisper-1",
            language="ko",
            temperature=0.7
        )
        
        print(f"인식된 텍스트: {transcript.text}")
        return transcript.text
    except Exception as e:
        print(f"음성 인식 오류: {e}")
        return None


async def llm(speak) : 
    if speak : 
        response = client.responses.create(
            model="gpt-4.1",
            input=speak
        )
        print(response.output_text)
        return response.output_text
    else :
        return None
    
async def tts(message):
        # OpenAI TTS API 호출
        response = client.audio.speech.create(
            model="tts-1",
            voice="nova",
            input=message
        )
        audio_bytes = response.content  # mp3 bytes
        print("여기까지 됨")
        
        return audio_bytes
    
@app.websocket("/ws")
async def websocket_endpoint(websocket: WebSocket):
    await websocket.accept()
    print("WebSocket 연결 수락됨")
    chat_character = await websocket.receive()  # 일단 성격을 줘야 함.

    # 오디오 데이터 버퍼
    audio_buffer = None
    chat_history = [{"role" :"system", "content": chat_character}]
    
    try:
        while True:
            # 메시지 수신 (바이너리 또는 텍스트)
            message = await websocket.receive()
            # print(message)
            
            # 바이너리 데이터 (오디오) 처리
            if "bytes" in message:
                audio_data = message["bytes"]
                print(f"수신된 오디오 데이터 크기: {len(audio_data)} 바이트")
                audio_buffer = audio_data
            
            # 텍스트 메시지 (JSON) 처리
            elif "text" in message:
                try:
                    data = json.loads(message["text"])
                    print(f"수신된 JSON 메시지: {data}")
                    
                    # audio_complete 메시지 처리
                    if data.get("action") == "audio_complete" and audio_buffer:
                        print("오디오 처리 시작")
                        
                        # STT 처리
                        transcription = stt(audio_buffer)
                        print(transcription)
                        if transcription:
                            # 처리 결과를 클라이언트에 전송
                            await websocket.send_json({
                                "transcription": transcription
                            })
                            # transcriotion 에 있는 내용 백엔드에 보내기보단 redis 에 저장
                            chat_history.append({"role" : "user", "content" : transcription})

                        else:
                            await websocket.send_json({
                                "error": "음성 인식 실패"
                            })
                        
                        # 버퍼 초기화
                        audio_buffer = None

                        # LLM 에서 대댑을 해줌
                        response = await llm(transcription)
                        chat_history.append({"role" : "assistant", "content" : response})
                        
                        # openai TTS 
                        return_voice = await tts(response)
                        # response를 redis에 저장하는 기능을 여기 넣자. 
                        
                        # 처음에는 음성이랑 데이터랑 같이 보낸다고 했는데, 그러면 음성 데이터(byte 데이터)를 인코딩을 해야 함 
                        # 다소 좋지 않은 방법이기 때문에, 먼저 텍스트를 보내고 음성을 보내려고 했으나, 생각해보니 운전하는 중에는 텍스트보단 음성을 먼저 기대할 것 같음. 
                        # 그리고 텍스트를 볼 수 있는 상황이면 굳이 음성 출력을 기다리지 않을 것 같음. 
                        # 무엇보다 텍스트를 보내고 나서 음성을 보내면 너무 느림.. 그래서 그냥 이건 안하는게 나을듯. 

                        print("여기까지됨 2222")
                        await websocket.send_bytes(return_voice)
                        

                except json.JSONDecodeError:
                    print("잘못된 JSON 형식")
            
            else:
                print(f"알 수 없는 메시지 형식: {message}")
    
    except WebSocketDisconnect:
        print("클라이언트 연결 종료")
        if websocket.application_state != WebSocketState.DISCONNECTED:
            await websocket.close()
    except Exception as e:
        print(f"오류 발생: {e}")


async def chat(request) : 
    response = client.chat.completions.create(
        model = 'gpt-3.5-turbo',
        messages=request
    )
    return response.choices[0].message.content

#테스트용 redis 데이터 저장용 함수

@app.post("/test/redis_save")
async def redis_save(user_key : str, chatbot_id : str, chat_prompt : str, status : str, tempDiary : str, temptitle : str, emotion : str) :
    current_time = datetime.datetime.now().strftime('%Y-%m-%d %H:%M:%S')
    r.hset(
        f'chat_session:{user_key}',
        mapping={  # mapping을 사용하여 키-값 쌍을 전달
            "chatbotId": chatbot_id,
            "chatPrompt": chat_prompt,
            "status": status,
            "tempDiary": tempDiary,
            "tempTitle": temptitle,
            "createdAt": current_time,
            "emotion": emotion
        }
    )
    return None 

#대략 3만자 정도면 컷 해야 하네. 인풋 아웃풋 합쳐서. 그럼 대략 10000자 정도면 음...


# chat history 저장 테스트용 함수
@app.post("/test/chat_history")
async def chat_history(user_key : str, role : str,text: str) :
    json_transform = json.dumps({"role" : role, "content" : text})
    print(json_transform)
    r.rpush(f"chat_session_messages:{user_key}",json_transform )
    return 
    
# 백엔드 redis 버전 7.4.3이라는데

# 1. 가장 먼저 구현해야 하는 것은 채팅창
@app.post("/api/v1/chat")
async def chat_text(user_key) :
    #여기서 redis 접속

    # 이전 채팅 기록 확인
    chatbot_info = r.hgetall(f"chat_session:{user_key}") #챗봇 프롬프트
    #생각해보니까 이거 어차피 챗봇 프롬프트에서 Ai 성격 프롬프트가 있잖아? 매번 확인해야 하긴 하네
    chat_history  =r.lrange(f"chat_session_messages:{user_key}", 0, -1)
    print(chat_history)
    
    #기존 채팅 히스토리 가져오기. json형태이므로, 안타깝게도 이건 바이너리 형태.....하 
    chat_history = list(map(json.loads,chat_history))
    print(chat_history)
    chat_history = [{"role" : "system", "content" : chatbot_info["chatPrompt"]}] + chat_history

    # 데이터 없으면 user id가 잘못된거니까 not correct
    if not chatbot_info:
        raise HTTPException(status_code=404, detail="user id not correct")
    # print(chatbot_info)
    
    # Now try to access with the correct key
    response  = await chat(chat_history)
    # 여기서 redis 에 답변을 저장해야 함. 
    
    if not response :
        raise HTTPException(status_code=500, detail="no response from server")
    else : 
        r.rpush(f"chat_session_messages:{user_key}", json.dumps({"role": "assistant", "content": response}))
        raise HTTPException(status_code=200, detail=response)

@app.post("/api/v1/to_diary")
async def diary(request : MyChat):
    # openai api 로 일기 만들기
    content = ""
    messages = [
        {"role": "system", "content": f"이 내용으로 일기를 만들어줘.{request.mychat}"},
        {"role": "system", "content": f"이런 양식으로 만들어줘 : {content}"},
    ]
    response = chat(messages)
    if not response : 
        raise HTTPException(status_code=500, detail="no response from server")
    else : 
        return {"code" : 200, "data" : response}


@app.post("/api/v1/prompt_test")
async def prompt_test(request : PromtTest) : 
    try : 
        response = await llm([{"role" : "system", "content" : "사용자의 요청에 따라 일기를 다시 써줘. 제공된 일기에 있었던 일만 언급해야 해."}, 
                {"role" : "system", "content" : request.user_prompt }, 
                {"role" : "user", "content" : request.prev_diary}])
        print(response)
    except Exception as e : 
        print(e)
    