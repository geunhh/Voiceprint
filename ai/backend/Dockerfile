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

# 종속성 설치
RUN pip install -r requirements.txt

# pkg_resources가 올바르게 동작하는지 확인
RUN python -c "import pkg_resources"

# 애플리케이션 코드 복사
COPY . .

# 실행
CMD ["python", "./voice_test_openai.py"]
