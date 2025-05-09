# --- 멀티스테이지 빌드: Builder Stage ---
    FROM python:3.12-slim AS builder

    # 빌드에 필요한 툴 설치
    RUN apt-get update && \
        apt-get install -y --no-install-recommends \
            gcc \
            build-essential \
            libasound2-dev \
            portaudio19-dev \
            ffmpeg \
            python3-dev \
            git && \
        rm -rf /var/lib/apt/lists/*
    
    WORKDIR /app
    
    # 의존성만 먼저 복사하여 캐시 활용
    COPY requirements.txt .
    
    # pip 및 패키지 설치
    RUN pip install --upgrade pip setuptools wheel \
     && pip install --no-cache-dir -r requirements.txt
    
    # 애플리케이션 코드 복사
    COPY . .
    
    
    # --- Runtime Stage ---
    FROM python:3.12-slim
    
    WORKDIR /app
    
    # Builder 에서 설치된 패키지(파이썬 라이브러리) 및 실행 스크립트 복사
    COPY --from=builder /usr/local/lib/python3.12/site-packages /usr/local/lib/python3.12/site-packages
    COPY --from=builder /usr/local/bin           /usr/local/bin
    COPY --from=builder /app                     /app
    
    # 불필요한 빌드 툴 제거 (런타임 이미지를 가볍게 유지)
    RUN apt-get purge -y --auto-remove && rm -rf /var/lib/apt/lists/*
    
    # 파이썬 출력 실시간 처리
    ENV PYTHONUNBUFFERED=1
    
    # FastAPI 기본 포트
    EXPOSE 8000
    
    # 컨테이너 시작 명령
    CMD ["uvicorn", "main:app", "--host", "0.0.0.0", "--port", "8000"]