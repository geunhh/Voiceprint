# React 앱 빌드

FROM node:22-alpine AS builder

WORKDIR /app

# 의존성 파일 복사
COPY package*.json ./

# 의존성 설치
RUN npm ci --omit=dev

# 전체 소스 복사
COPY . .

# react 빌드
RUN npm run build

# Nginx로 정적 파일 서빙

FROM nginx:1.27-alpine

# NGINX 설정 복사
COPY default.conf /etc/nginx/conf.d/default.conf

# React build 산출물 복사 
COPY --from=builder /app/dist /usr/share/nginx/html

# 포트
EXPOSE 80

# NGINX 실행
CMD ["nginx", "-g", "daemon off;"]
