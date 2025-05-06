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
        # redis-server \  # Redis 서버 추가
        # supervisor && \  # Supervisor 추가 (여러 프로세스 관리용)
    rm -rf /var/lib/apt/lists/*

WORKDIR /app

COPY requirements.txt .

# pip 업그레이드 및 setuptools 설치
RUN pip install --upgrade pip setuptools wheel

# Python 패키지 설치
RUN pip install --no-cache-dir -r requirements.txt

# 앱 코드 복사
COPY . .

# # Supervisor 설정 파일 생성 시작 (추가된 부분)
# RUN echo "[supervisord]" > /etc/supervisor/conf.d/supervisord.conf && \
#     echo "nodaemon=true" >> /etc/supervisor/conf.d/supervisord.conf && \
#     echo "" >> /etc/supervisor/conf.d/supervisord.conf && \
#     echo "[program:redis]" >> /etc/supervisor/conf.d/supervisord.conf && \
#     echo "command=redis-server" >> /etc/supervisor/conf.d/supervisord.conf && \
#     echo "autostart=true" >> /etc/supervisor/conf.d/supervisord.conf && \
#     echo "autorestart=true" >> /etc/supervisor/conf.d/supervisord.conf && \
#     echo "" >> /etc/supervisor/conf.d/supervisord.conf && \
#     echo "[program:app]" >> /etc/supervisor/conf.d/supervisord.conf && \
#     echo "command=uvicorn main:app --host 0.0.0.0 --port 8000" >> /etc/supervisor/conf.d/supervisord.conf && \
#     echo "directory=/app" >> /etc/supervisor/conf.d/supervisord.conf && \
#     echo "autostart=true" >> /etc/supervisor/conf.d/supervisord.conf && \
#     echo "autorestart=true" >> /etc/supervisor/conf.d/supervisord.conf



# 환경 변수 (로그 버퍼 비우지 않게)
ENV PYTHONUNBUFFERED=1


# Redis 포트 노출 (추가된 부분)
# EXPOSE 6379
# # 앱 포트 노출
# EXPOSE 8000

# 컨테이너 실행 시 uvicorn으로 FastAPI 실행
CMD ["uvicorn", "main:app", "--host", "0.0.0.0", "--port", "8000"]


# Supervisor로 여러 프로세스 실행 (변경된 부분)
# CMD ["supervisord", "-c", "/etc/supervisor/conf.d/supervisord.conf"]
