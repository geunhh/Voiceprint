#!/bin/bash
# switch-env.sh

ENV=$1

if [ "$ENV" = "dev" ]; then
    echo "🔧 개발 환경으로 전환..."
    
    # 환경변수 교체
    cp .env.dev .env
    cp services/backend-msa/.env.dev services/backend-msa/.env
    
    # 2. 기존 컨테이너 중지
    docker-compose -f docker-compose.yml -f docker-compose.prod.yml -f docker-compose.resources.yml down 
    
    # 3. Frontend 재빌드 (dev 환경변수 반영)
    echo "📦 Frontend 빌드 중..."
    docker-compose -f docker-compose.yml -f docker-compose.dev.yml -f docker-compose.resources.yml build frontend
    
    # 4. 개발 환경 시작
    docker-compose -f docker-compose.yml -f docker-compose.dev.yml -f docker-compose.resources.yml up -d
    
    echo "✅ 개발 환경 시작 (http://localhost:81)"
    
elif [ "$ENV" = "prod" ]; then
    echo "🚀 프로덕션 환경으로 전환..."
    
    # 1. 환경변수 교체
    cp .env.prod .env
    cp services/backend-msa/.env.prod services/backend-msa/.env
    
    # 2. 기존 컨테이너 중지
    docker-compose -f docker-compose.yml -f docker-compose.dev.yml -f docker-compose.resources.yml down 
    
    # 3. Frontend 재빌드 (prod 환경변수 반영)
    echo "📦 Frontend 빌드 중..."
    docker-compose -f docker-compose.yml -f docker-compose.prod.yml -f docker-compose.resources.yml build frontend
    
    # 4. 프로덕션 환경 시작
    docker-compose -f docker-compose.yml -f docker-compose.prod.yml -f docker-compose.resources.yml up -d
    
    echo "✅ 프로덕션 환경 시작 (https://myvoiceprint.duckdns.org)"
    
else
    echo "Usage: ./switch-env.sh [dev|prod]"
    exit 1
fi