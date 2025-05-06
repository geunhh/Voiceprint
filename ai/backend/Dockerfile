FROM python:3.12-slim

# 필요한 시스템 패키지 설치
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

COPY requirements.txt .

# pip 업그레이드 및 setuptools 설치
RUN pip install --upgrade pip setuptools wheel

# Python 패키지 설치
RUN pip install --no-cache-dir -r requirements.txt

# 앱 코드 복사
COPY . .

# 환경 변수 (로그 버퍼 비우지 않게)
ENV PYTHONUNBUFFERED=1

# 컨테이너 실행 시 uvicorn으로 FastAPI 실행
CMD ["uvicorn", "main:app", "--host", "0.0.0.0", "--port", "8000"]
