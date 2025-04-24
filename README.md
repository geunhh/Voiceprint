# 깃 컨벤션

## 커밋 메시지 컨벤션

<aside>

#### 1. 커밋 유형 지정

| 커밋 유형          | 의미                                                         |
| ------------------ | ------------------------------------------------------------ |
| `feat`             | 새로운 기능 추가                                             |
| `fix`              | 버그 수정                                                    |
| `docs`             | 문서 수정                                                    |
| `style`            | 코드 formatting, 세미콜론 누락, 코드 자체의 변경이 없는 경우 |
| `refactor`         | 코드 리팩토링                                                |
| `test`             | 테스트 코드, 리팩토링 테스트 코드 추가                       |
| `chore`            | 패키지 매니저 수정, 그 외 기타 수정 ex) .gitignore           |
| `design`           | CSS 등 사용자 UI 디자인 변경                                 |
| `comment`          | 필요한 주석 추가 및 변경                                     |
| `rename`           | 파일 또는 폴더 명을 수정하거나 옮기는 작업만인 경우          |
| `remove`           | 파일을 삭제하는 작업만 수행한 경우                           |
| `!BREAKING CHANGE` | 커다란 API 변경의 경우                                       |
| `!HOTFIX`          | 급하게 치명적인 버그를 고쳐야 하는 경우                      |

#### 2. 제목과 본문을 빈행으로 분리

- 커밋 유형 이후 제목과 본문은 한글로 작성하여 내용이 잘 전달될 수 있도록 할 것
- 본문에는 변경한 내용과 이유 설명 (어떻게보다는 무엇 & 왜를 설명)

#### 3. 형식

- 커밋유형 : 내용
- ex) feat: 게시판 업로드 API 기능 구현
</aside>

# GitLab 브랜치 전략 및 네이밍 컨벤션

## 1. 브랜치 전략 (Git Flow)

Git Flow 기반으로 브랜치 전략을 구성하며, 다음과 같은 브랜치를 사용헸습니다.

### ✅ 기본 브랜치

- **master** : 배포 가능한 안정적인 코드가 위치한 브랜치 (직접 푸시 금지)
- **release** : 테스트 완료 후 배포 준비 브랜치 (직접 푸시 금지지)
- **develop** : 기능 개발이 완료된 코드가 합쳐지는 브랜치 (기능 개발 브랜치들이 머지됨)
- **frontend / backend** : 기능 개발 브렌치에서 테스트 후 frontend/backend 브렌치에서 안정화 확인

### ✅ 브랜치 흐름

1. 새로운 기능 개발 시 `frontend/backend`에서 `feature/JIRA-xxx-설명` 브랜치 생성
2. 개발 완료 후 `frontend/backend`으로 Merge Request (MR)
3. 배포 준비 시 `develop`에서 버그 수정 및 QA 후 안정화된 버전 release로 머지 후 배포
4. 긴급 수정 필요 시 `develop`에서 `hotfix/JIRA-xxx-설명` 생성/테스트 후 `release`에 머지
5. 'master'는 최종 배포, 산출물물
## 2. 브랜치 네이밍 컨벤션

브랜치명에는 **Jira 이슈 번호**를 포함하여 추적을 용이하게 하고, 역할을 명확하게 합니다.

### ✅ 브랜치 네이밍 규칙

```
브랜치유형/JIRA-이슈번호-설명/{front or back}
```

**설명 규칙:**

- **영어 소문자 or 한글** 사용 (띄어쓰기 하이픈 활용)
- **의미가 명확한 단어** 사용 (예: `add-login`, `fix-db-error`)
- **30자 이하**로 간결하게 작성

### ✅ 기능 개발 및 수정 브랜치

- **feature/{JIRA-이슈번호}-기능명/{front or back}** : 새로운 기능 개발을 위한 브랜치
- **hotfix/{JIRA-이슈번호}-설명** : 긴급 수정이 필요한 경우, `master`에서 분기하여 빠르게 반영 후 `develop`에도 머지
- **fix/{JIRA-이슈번호}-설명** : 개발 중 발견된 버그 수정 브랜치

### ✅ 브랜치명 예시

### 기능 개발

- `feature/JIRA-123-add-login/front` (로그인 기능 개발)
- `feature/JIRA-234-implement-image-upload/back` (이미지 업로드 기능 추가)

### 버그 수정

- `fix/JIRA-345-fix-image-upload/back` (이미지 업로드 버그 수정)
- `fix/JIRA-456-resolve-api-timeout/back` (API 타임아웃 문제 해결)

### 긴급 수정

- `hotfix/JIRA-567-fix-server-crash` (긴급 서버 다운 해결)
- `hotfix/JIRA-678-fix-payment-bug` (결제 오류 긴급 수정)
