pipeline {
  agent any

  triggers {
    gitlab(
      triggerOnPush: true,
      triggerOnMergeRequest: true,
      branchFilterType: 'NameBasedFilter',
      includeBranchesSpec: 'release'
    )
  }


  environment {
    FRONTEND_IMAGE = "jokiheum/voiceprint-frontend:latest"
    BACKEND_IMAGE = "jokiheum/voiceprint-backend:latest"
    MYSQL_IMAGE = "jokiheum/voiceprint-mysql:latest"
    DEPLOY_HOST = "ubuntu@k12b106.p.ssafy.io"
    DEPLOY_PATH = "/home/ubuntu/voiceprint"
  }

  stages {
    // GitLab에서 현재 브랜치 코드를 Jenkins 워크스페이스로 내려받음
    stage("Checkout") {
      steps {
        checkout scm
      }
    }
    
    // Jenkins credential에 등록한 env 파일 설정
    stage("Prepare Environment") {
      steps {
        withCredentials([
          file(credentialsId: 'env-backend', variable: 'BACKEND_ENV'),
          file(credentialsId: 'env-frontend', variable: 'FRONTEND_ENV'),
          file(credentialsId: 'env-mysql', variable: 'MYSQL_ENV')
        ]) {
          script {
            // 각각의 파일 내용을 읽어서 해당 위치에 저장
            writeFile file: 'backend/.env', text: readFile(BACKEND_ENV)
            writeFile file: 'frontend/.env', text: readFile(FRONTEND_ENV)
            writeFile file: 'mysql/.env', text: readFile(MYSQL_ENV)
          }
        }
      }
    }

    // frontend docker image build
    stage("Build FRONTEND Docker Image") {
      steps {
        dir('frontend') {
          sh "docker build -t ${FRONTEND_IMAGE} ."
        }
      }
    }

    // backend docker image build 
    stage("Build Backend Docker Image") {
      steps {
        dir('backend') {
          sh "docker build -t ${BACKEND_IMAGE} ."
        }
      }
    }

    // Mysql docker image build
    stage("Build Mysql Docker Image") {
      steps {
        dir('mysql') {
          sh "docker build -t ${MYSQL_IMAGE} ."
        }
      }
    }



    // 위에서 build 한 docker image push
    stage("Push Docker Images") {
      steps {
        withDockerRegistry([credentialsId: 'dockerhub-token', url: '']) {
          sh "docker push ${FRONTEND_IMAGE}"
          sh "docker push ${BACKEND_IMAGE}"
          sh "docker push ${MYSQL_IMAGE}"
        }
      }
    }


    // EC2에 배포
    stage("Deploy to EC2") {
      steps {
        sshagent(credentials: ['ec2-ssh-key']) {
          sh """
          # 1. 필요한 파일 전송
          scp -o StrictHostKeyChecking=no backend/.env ${DEPLOY_HOST}:${DEPLOY_PATH}/backend.env
          scp -o StrictHostKeyChecking=no frontend/.env ${DEPLOY_HOST}:${DEPLOY_PATH}/frontend.env
          scp -o StrictHostKeyChecking=no mysql/.env ${DEPLOY_HOST}:${DEPLOY_PATH}/mysql.env
          scp -o StrictHostKeyChecking=no docker-compose.yml ${DEPLOY_HOST}:${DEPLOY_PATH}/docker-compose.yml
          ssh -o StrictHostKeyChecking=no ${DEPLOY_HOST} 'rm -rf ${DEPLOY_PATH}/voiceprint.conf'
          scp -o StrictHostKeyChecking=no voiceprint.conf ${DEPLOY_HOST}:${DEPLOY_PATH}/voiceprint.conf
          # 2. 원격 접속 후 배포
          ssh -o StrictHostKeyChecking=no ${DEPLOY_HOST} '
            cd ${DEPLOY_PATH} &&
            docker compose down --remove-orphans &&
            docker compose up -d --build
            docker compose pull &&
            docker compose up -d &&
            docker image prune -f &&
            rm -f backend.env frontend.env mysql.env
          '
          """
        }
      }
    }
  }
  post {
    always {
        echo '배포 파이프라인 종료'
    }
  }
}