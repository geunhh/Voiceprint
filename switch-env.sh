#!/bin/bash
# switch-env.sh

ENV=$1

if [ "$ENV" = "dev" ]; then
    echo "🔧 개발 환경으로 전환..."
    
    # 환경변수 교체
    cp .env.dev .env
    cp services/backend-msa/.env.dev services/backend-msa/.env
    
    # Docker Compose 재시작
    docker-compose -f docker-compose.yml -f docker-compose.prod.yml down
    docker-compose -f docker-compose.yml -f docker-compose.dev.yml up -d
    
    echo "✅ 개발 환경 시작 (http://localhost:81)"
    
elif [ "$ENV" = "prod" ]; then
    echo "🚀 프로덕션 환경으로 전환..."
    
    # 환경변수 교체
    cp .env.prod .env
    cp services/backend-msa/.env.prod services/backend-msa/.env
    
    # Frontend 재빌드
    docker-compose -f docker-compose.yml -f docker-compose.prod.yml build frontend
    
    # Docker Compose 재시작
    docker-compose -f docker-compose.yml -f docker-compose.dev.yml down
    docker-compose -f docker-compose.yml -f docker-compose.prod.yml up -d
    
    echo "✅ 프로덕션 환경 시작 (https://myvoiceprint.duckdns.org)"
    
else
    echo "Usage: ./switch-env.sh [dev|prod]"
    exit 1
fi