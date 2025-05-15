from fastapi import FastAPI, WebSocket, WebSocketDisconnect, Depends, HTTPException
from fastapi.middleware.cors import CORSMiddleware
import webrtcvad
import tempfile
from openai import OpenAI
import os
from dotenv import load_dotenv
import wave
# import io
# import subprocess
import json
import openai
from starlette.websockets import WebSocketState
import redis
import asyncio
from schema import Chat, MyChat, PromtTest, ChatResponse, ChatSaveTest
from typing import Annotated
import datetime
# import base64
import wave
import tempfile
import os
import io
from google.cloud import texttospeech

import logging

# 로깅 설정
logging.basicConfig(level=logging.INFO)
logger = logging.getLogger(__name__)

app = FastAPI()
r = redis.Redis(host=os.getenv("REDIS_HOST"), port=6379,  password=os.getenv("REDIS_PASSWORD"), decode_responses=True)

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

#google tts
os.environ["GOOGLE_APPLICATION_CREDENTIALS"] = "rd-pjt-459808-184401881707.json"
client_gtts = texttospeech.TextToSpeechClient()


# 오디오 설정
CHANNELS = 1
RATE = 16000
SAMPLE_WIDTH = 2  # 16비트 = 2바이트

# 채팅 길이
MAX_CHAT_LENGTH = 2000

async def verify_API(token : Annotated) :
    if token != os.getenv("BACKEND_API") : 
        raise HTTPException(status_code=400, detail="TOKEN INVALID. USE CORRECT TOKEN TO ACCESS")





def g_tts(text) :
    # 요청할 텍스트 설정
    synthesis_input = texttospeech.SynthesisInput(text=text)

    # 음성 설정
    voice = texttospeech.VoiceSelectionParams(
        language_code="ko-KR",
        ssml_gender=texttospeech.SsmlVoiceGender.NEUTRAL  # 남/여 중 선택 가능
    )

    # 오디오 설정
    audio_config = texttospeech.AudioConfig(
        audio_encoding=texttospeech.AudioEncoding.MP3
    )

    # 요청 보내기
    response = client_gtts.synthesize_speech(
        input=synthesis_input,
        voice=voice,
        audio_config=audio_config
    )
    print("google response 완료.")
    return response.audio_content


def stt(audio_data):
    if not audio_data:
        print("오디오 데이터가 없습니다.")
        return None

    try:
        # 직접 WAV 헤더 생성 (16kHz, 16비트, 모노)
        with tempfile.NamedTemporaryFile(suffix='.wav', delete=False) as temp_file:
            temp_wav_path = temp_file.name
        
            # WAV 파일로 저장
            wf = wave.open(temp_wav_path, "wb")
            wf.setnchannels(1)
            wf.setsampwidth(2)
            wf.setframerate(16000)
            wf.writeframes(audio_data)
            wf.close()
        with open(temp_wav_path, 'rb') as audio_file:
            # OpenAI API로 전송
            # print(temp_wav_path)
            transcript = client.audio.transcriptions.create(
                file=audio_file,
                model="whisper-1",
                language="ko",
                temperature=0.7
            )
            
            print(f"인식된 텍스트: {transcript.text}")
            
            return transcript.text
        
    except Exception as e:
        print(f"STT 처리 중 오류 발생: {e}")
        import traceback
        traceback.print_exc()
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
    print("웹소켓 연결 대기중")
    await websocket.accept()
    print("WebSocket 연결 수락됨")
    user_id = websocket.query_params.get("userId")
    if not user_id:
        await websocket.close(code=1008)
        return

    chatbot_info = r.hgetall(f"chat_session:{user_id}")
    chat_history  =r.lrange(f"chat_session_messages:{user_id}", 0, -1)

    print(chatbot_info, chat_history)

    if not chatbot_info:
        await websocket.close(code=1003)
        print("❌ user id not correct or 세션 없음")
        return

    if int(chatbot_info["total_token"]) > MAX_CHAT_LENGTH :
        return {"chatting_response": "챗봇 토큰 수를 초과하였습니다.", "token" : chatbot_info["total_token"]}

    
    #기존 채팅 히스토리 가져오기
    chat_history = list(map(json.loads,chat_history))
    chat_history = [{"role" : "system", "content" : chatbot_info["chatPrompt"]}] + chat_history 

    # 오디오 데이터 버퍼
    audio_buffer = b''
    
    try:
        while True:
            # 메시지 수신 (바이너리 또는 텍스트)
            message = await websocket.receive()
            
            # 바이너리 데이터 (오디오) 처리
            if "bytes" in message:
                audio_data = message["bytes"]
                print(f"수신된 오디오 데이터 크기: {len(audio_data)} 바이트")
                audio_buffer += audio_data
            
            # 텍스트 메시지 (JSON) 처리
            elif "text" in message:
                print("text in message:")
                try:
                    data = json.loads(message["text"])
                    print(f"수신된 JSON 메시지: {data}")
                    
                    if "Done" in data and audio_buffer:
                        print("오디오 처리 시작")
                        
                        # STT 처리
                        transcription = stt(audio_buffer)
                        print(transcription)
                        if not transcription.strip() : 
                            await websocket.send_json({
                                "error": "음성 인식 실패"
                            }) 
                        # 오디오 버퍼 초기화
                        audio_buffer = b''
                        if transcription:
                            # 처리 결과를 클라이언트에 전송
                            await websocket.send_json({
                                "transcription": transcription
                            })
                            # transcriotion 에 있는 내용 백엔드에 보내기보단 redis 에 저장
                            chat_history.append({"role" : "user", "content" : transcription})    

                            # LLM 에서 대댑을 해줌
                            response = await llm(transcription)
                            if response :
                                
                                chat_history.append({"role" : "assistant", "content" : response})
                                await websocket.send_json({
                                    "chatting" : [
                                        {"role" : "user", "content" : transcription},
                                        {"role" : "assistant", "content" : response}
                                    ],
                                    "token" : len(transcription)+len(response)

                                })
                                # openai TTS 
                                # return_voice = await tts(response)
                                return_voice = g_tts(response)
                                # response를 redis에 저장하는 기능을 여기 넣자.

                                print("여기까지됨 2222")
                                await websocket.send_bytes(return_voice)
                            else : 
                                await websocket.send_json({"error" : "LLM response fail"})
                        else : 
                            await websocket.send_json({
                                "error": "음성 인식 실패"
                            }) 

                        audio_buffer = b''
                except json.JSONDecodeError:
                    print("잘못된 JSON 형식")
            
            else:
                print(f"알 수 없는 메시지 형식: {message}")
    
    except WebSocketDisconnect:
        print("클라이언트 연결 종료")
        pass
        # if websocket.application_state != WebSocketState.DISCONNECTED:
        #     await websocket.close()
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
async def redis_save(user_key : str, chatbot_id : str, chat_prompt : str, status : str, total_token :int, tempDiary : str, temptitle : str, themeDescription : str,themeTitle : str, themeDiary : str, emotion : str) :
    current_time = datetime.datetime.now().strftime('%Y-%m-%d %H:%M:%S')
    r.hset(
        f'chat_session:{user_key}',
        mapping={  # mapping을 사용하여 키-값 쌍을 전달
            "chatbotId": chatbot_id,
            "chatPrompt": chat_prompt,
            "status": status,
            "total_token": total_token,
            "tempDiary": tempDiary, 
            "tempTitle": temptitle,
            "createdAt": current_time,
            "themeDescription" : themeDescription, 
            "themeTitle": themeTitle,
            "themeDiary" : themeDiary,
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
@app.post("/test/save_full_chat_history")
def save_full_chat_history(test : ChatSaveTest):
    """
    채팅 내역 전체를 Redis에 한 번에 저장하는 함수.

    Args:
        user_id: 사용자 ID (정수).
        chat_history: 채팅 내역 리스트 ( [{"role": "user", "content": ...}, ...] 형태).
    """
    try:
        key = f"chat_session_messages:{test.user_id}"

        # 채팅 내역을 JSON 문자열로 변환하여 Redis에 저장
        for message in test.chat_history:
            r.rpush(key, json.dumps(message))

    except Exception as e:
        print(f"Error saving chat history: {e}")
        return False  # 실패 시 False 반환

    return True  # 성공 시 True 반환


@app.post("/api/v1/chat")
async def chat_text(chat_response : ChatResponse) :
    # 이전 채팅 기록 확인
    chatbot_info = r.hgetall(f"chat_session:{chat_response.user_id}") #챗봇 프롬프트
    #생각해보니까 이거 어차피 챗봇 프롬프트에서 Ai 성격 프롬프트가 있잖아? 매번 확인해야 하긴 하네
    chat_history  =r.lrange(f"chat_session_messages:{chat_response.user_id}", 0, -1)
    print(chatbot_info)

    if int(chatbot_info["total_token"]) > MAX_CHAT_LENGTH :
        return {"chatting_response": "챗봇 토큰 수를 초과하였습니다.", "token" : chatbot_info["total_token"]}
    
    #기존 채팅 히스토리 가져오기
    chat_history = list(map(json.loads,chat_history))

    chat_history = [{"role" : "system", "content" : chatbot_info["chatPrompt"]}] + chat_history + [{"role" : "user", "content" : chat_response.chatting}]

    # 데이터 없으면 user id가 잘못된 상황으로 판단, not correct 예외 처리
    if not chatbot_info:
        raise HTTPException(status_code=404, detail="user id not correct")
    
    # 채팅 답변을 받아옴
    response  = await chat(chat_history)
    
    # 프롬프트 길이 수정해서 저장.
    chatbot_info["total_token"] = int(chatbot_info["total_token"]) + len(response) + len(chat_response.chatting)

    if not response :
        raise HTTPException(status_code=500, detail="no response from server")
    else : 
        
        # 채팅 답변과 글자수를 보냄.
        return {"chatting_response": response, "token" : chatbot_info["total_token"]}


@app.post("/api/v1/to_diary")
async def diary(request: MyChat):
    # Redis에서 프롬프트랑 채팅 기록 받아오기
    chatbot_info = r.hgetall(f"chat_session:{request.user_id}")
    chat_history = r.lrange(f"chat_session_messages:{request.user_id}", 0, -1)
    chat_history = list(map(json.loads, chat_history))
    user_chat = ''
    print(chatbot_info)
    # 여기서 유저가 한 채팅 내용만 긁어오기
    for i in range(len(chat_history)): 
        if chat_history[i]["role"] == "user": 
            user_chat += chat_history[i]["content"] 

    system_prompt = f"""당신은 유저의 채팅 기록을 바탕으로 일기를 작성하는 전문가입니다. {chatbot_info["themeDescription"]} 글쓰기 스타일로 해주세요.
    
    다음 형식을 엄격하게 지켜서 일기를 만들어주세요:
    
    [일기의 제목]
    
    [행복/설렘/피로/짜증/우울] (다섯 가지 감정 중 하나만 선택)
    
    [채팅의 핵심 내용을 일기로 작성. 700자 내외로 작성]
    
    **출력 예시:**
    
    {chatbot_info["themeTitle"]}

    기쁨

    {chatbot_info["themeDiary"]}


    주의사항:
    1. 반드시 위 형식을 정확히 따라주세요
    2. 채팅 기록을 그대로 복사하지 말고, 일기 형태로 재구성해주세요
    3. 발랄하고 활기찬 분위기로 작성해주세요.
    4. 각 항목 사이에는 빈 줄을 넣어주세요
    5. 응답은 오직 일기 형식으로만 구성해주세요 (다른 설명이나 메타 정보 불필요)
    6. 글자 수는 700자 정도입니다.
    """
    
    user_prompt = f"""다음은 유저의 채팅 기록입니다. 이를 바탕으로 일기를 작성해주세요:
    
    {user_chat}
    """

    messages = [
        {"role": "system", "content": system_prompt},
        {"role": "user", "content": user_prompt}
    ]
    
    response = await chat(messages)
    
    lines = response.split('\n\n')
    if len(lines) < 3:
        raise HTTPException(status_code=500, detail="응답 형식이 올바르지 않습니다")
        
    return {"title": lines[0], "emotion": lines[1], "diary": lines[2]}

pompt_example = "1. 어투 * 일기체: 나에게 쓰는 개인적인 기록 스타일 (예: 오늘 나는...) * 독자 의식형: 블로그나 공유를 위한 스타일 (예: 여러분들도 이런 경험...) * 대화형: 타인과 대화하듯 쓰는 스타일 (예: 너는 어땠어?) * 격식체: 존댓말 위주의 정중한 표현 (예: 오늘은 ~했습니다) * 비격식체: 반말 위주의 편안한 표현 (예: 오늘은 ~했어) 2. 분위기 * 밝고 유쾌: 긍정적이고 활기찬 표현, 웃음 요소가 많음 (이모티콘, ㅋㅋㅋ 등) * 담담하고 평온: 중립적인 감정, 사실 위주 서술 * 슬프고 우울: 부정적 감정, 아쉬움, 후회 등이 드러남 * 화나고 짜증남: 분노, 짜증, 불만 등이 표현됨 * 설레고 기대됨: 기대감, 희망, 설렘 등의 감정이 담김 * 감사하고 만족: 고마움, 충족감, 행복감 등을 표현 * 불안하고 걱정됨: 걱정, 염려, 불안 등의 감정이 포함 3. 주제 * 일상생활: 평범한 일상, 소소한 일들 * 운동/건강: 운동, 다이어트, 건강관리 * 취미활동: 독서, 영화, 게임, 음악 등 취미 * 직장/업무: 일과 관련된 경험, 성과, 고민 * 학업/공부: 학교, 시험, 공부 관련 * 인간관계: 가족, 친구, 연인, 동료와의 관계 * 여행/나들이: 여행, 외출, 나들이 경험 * 음식/요리: 식사, 요리, 맛집 경험 * 패션/뷰티: 옷, 화장품, 외모 관리 * 육아/가족: 아이 키우기, 가족 생활 * 쇼핑/소비: 구매, 쇼핑 경험 * 문화/예술: 공연, 전시회, 예술 활동 * 명상/성찰: 자기 성찰, 깨달음, 철학적 사고 * 사회/시사: 사회 이슈, 시사 관련 생각 * 디지털/IT: 기기, 앱, 디지털 경험 4. 길이  * 초단문: 300자 미만 * 단문: 300자~800자 * 중문: 800자~1500자 * 장문: 1500자~3000자 * 초장문: 3000자 이상 5. 서술 특성 * 시간 흐름: 시간순으로 일어난 일을 서술 * 주제 중심: 특정 주제를 중심으로 생각 전개 * 감정 중심: 감정과 느낌 위주로 서술 * 대화 포함: 대화나 인용문이 많이 포함됨 * 묘사 중심: 상황이나 장면을 생생히 묘사 * 사색 중심: 생각과 성찰 위주의 내용 6. 문체 특징 * 간결체: 짧고 명료한 문장 위주 * 상세체: 자세한 설명과 묘사가 많음 * 구어체: 말하는 듯한 문체, 줄임말 많음 * 문어체: 정제된 표현, 문어적 표현 * 감성체: 감정 표현과 수식어가 풍부 * 이모티콘 사용: 이모티콘, 특수문자 활용 * 관용어 사용: 속담, 관용구 등을 활용"
diary_example = "아침부터 마음이 이상하게 두근거렸다. 별일도 없는데 왜일까 싶었는데, 어쩌면 오늘따라 바람이 조금 더 부드럽게 느껴지고, 햇살이 조금 더 따뜻하게 내려와서 그런 걸까. 출근길에 마주친 이름 모를 꽃들이 나를 보고 웃는 것 같아서 괜히 혼자 웃음이 났다.오후에는 카페에서 잠깐 일을 했는데, 옆 테이블에서 들려오던 웃음소리와 커피향이 섞여 마음을 간질였다. 오늘은 정말 별일 아닌 것들이 전부 특별하게 느껴졌다.저녁에는 친구랑 약속이 있어서 나갔는데, 걷는 내내 심장이 조금 빨리 뛰는 것 같았다. 오랜만에 만난 친구와의 대화, 밝게 빛나는 거리의 불빛들, 그리고 그 모든 순간들이 나를 설레게 했다. 내일도 이런 기분이 이어졌으면 좋겠다.참, 오늘의 나에게 한마디 — 이 설렘을 잊지 말자."

@app.post("/api/v1/prompt_test")
async def prompt_test(request : PromtTest) : 
    prompt_response = await llm([{"role" : "system", "content" : "아래 기반으로 일기를 만들 때 어떤 프롬프트를 써야 유사한 분위기의 일기를 쓸 수 있을지 알려줄래? 다른 말 없이 딱 프롬프트만 줬으면 해."}, 
            {"role" : "system", "content" : pompt_example }, 
            {"role" : "user", "content" : request.prev_diary}])
    new_diary = await llm([{"role" : "system", "content" : "아래 프롬프트를 기반으로 일기를 새로 생성해줘."},{"role" : "user", "content" : prompt_response},  {"role" : "user", "content" : diary_example}])
    return { "prompt": prompt_response,"example": new_diary[:200]}
