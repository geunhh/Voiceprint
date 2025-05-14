DROP DATABASE IF EXISTS voiceprint_db;
CREATE DATABASE voiceprint_db
  CHARACTER SET = utf8mb4
  COLLATE = utf8mb4_unicode_ci;
USE voiceprint_db;

-- 1) profile_images
CREATE TABLE `profile_images` (
	`id` TINYINT NOT NULL AUTO_INCREMENT COMMENT "프로필 이미지 ID",
  `title` VARCHAR(10) NOT NULL COMMENT "이미지 제목",
  `image_url` VARCHAR(512) NOT NULL COMMENT '이미지 URL',
  `created_at` TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT "생성일시",
  PRIMARY KEY(id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- 2) emotion
CREATE TABLE `emotion` (
	`id` TINYINT NOT NULL AUTO_INCREMENT COMMENT "감정 ID",
  `name` VARCHAR(10) NOT NULL COMMENT "감정 내용",
  `color` VARCHAR(7) NULL COMMENT "감정 색상",
  PRIMARY KEY(id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- 3) prompt_questions 
CREATE TABLE `prompt_questions` (
	`id` TINYINT NOT NULL AUTO_INCREMENT COMMENT "유도 질문 ID",
  `question` VARCHAR(50) NOT NULL COMMENT '질문',
  `created_at` TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT "생성일시",
	PRIMARY KEY(id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- 4) diary_themas
CREATE TABLE diary_themas (
  `id`            INT          NOT NULL AUTO_INCREMENT COMMENT '테마 ID',
  `user_id`       INT          NULL COMMENT '유저 아이디',
  `title`         VARCHAR(10)  NULL COMMENT '제목',
  `description`   VARCHAR(100) NULL COMMENT '설명',
  `example`       VARCHAR(255) NULL COMMENT '예시',
  `prompt`        TEXT         NOT NULL COMMENT '프롬프트',
  `created_at`    TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '생성일시',
  `updated_at`    TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '수정일시',
  PRIMARY KEY(id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- 8) chatbot
CREATE TABLE chatbot (
  id           TINYINT      NOT NULL AUTO_INCREMENT COMMENT '챗봇 ID',
  name         VARCHAR(30)  NOT NULL COMMENT '이름',
  description  VARCHAR(255) NULL COMMENT '설명',
  image_url    VARCHAR(512) NULL COMMENT '이미지',
  prompt       TEXT         NOT NULL COMMENT '프롬프트',
  is_deleted   BOOLEAN      NOT NULL DEFAULT FALSE COMMENT '삭제여부',
  created_at   TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '생성일시',
  updated_at   TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '수정일시',
  PRIMARY KEY(id),
  KEY
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- 5) users
CREATE TABLE users (
  id               INT           NOT NULL AUTO_INCREMENT COMMENT '사용자 ID',
  profile_image_id TINYINT       NULL COMMENT '프로필 이미지',
  email            VARCHAR(50)   NOT NULL COMMENT '이메일',
  nickname         VARCHAR(30)   NOT NULL COMMENT '닉네임',
  auth_provider    ENUM('google','kakao') NOT NULL COMMENT '소셜 제공자',
  custom_thema_id  INT           NULL COMMENT '사용자 커스텀 테마',
  using_thema_id   INT           NULL COMMENT '선택 테마',
  last_chatbot_id  TINYINT       NULL COMMENT '최근 사용 챗봇 ID',
  is_deleted       BOOLEAN       NOT NULL DEFAULT FALSE COMMENT '삭제여부',
  created_at       TIMESTAMP     NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '생성일시',
  updated_at       TIMESTAMP     NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '수정일시',
  PRIMARY KEY(id),
  CONSTRAINT fk_users_profile_image FOREIGN KEY(profile_image_id) REFERENCES profile_images(id)
    ON DELETE SET NULL ON UPDATE CASCADE,
  CONSTRAINT fk_users_custom_thema FOREIGN KEY(custom_thema_id) REFERENCES diary_themas(id)
    ON DELETE SET NULL ON UPDATE CASCADE,
  CONSTRAINT fk_users_using_thema FOREIGN KEY(using_thema_id) REFERENCES diary_themas(id)
    ON DELETE SET NULL ON UPDATE CASCADE,
  CONSTRAINT fk_users_last_chatbot FOREIGN KEY(last_chatbot_id) REFERENCES chatbot(id)
    ON DELETE SET NULL ON UPDATE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- 6) groups
CREATE TABLE `groups` (
  id               INT           NOT NULL AUTO_INCREMENT COMMENT '그룹 ID',
  name             VARCHAR(30)   NOT NULL COMMENT '이름',
  description      VARCHAR(255)  NULL COMMENT '설명',
  invitation_code  CHAR(10)      NULL COMMENT '초대코드',
  group_image      VARCHAR(512)  NULL COMMENT '이미지',
	enable_alarm     BOOLEAN       NOT NULL DEFAULT FALSE COMMENT '알람여부', 
	alarm_day        VARCHAR(50)   NULL  COMMENT '알림요일',
  alarm_time       TIME          NULL  COMMENT '알림시간',
  is_deleted       BOOLEAN       NOT NULL DEFAULT FALSE COMMENT '삭제여부',
  created_at       TIMESTAMP     NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '생성일시',
  updated_at       TIMESTAMP     NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '수정일시',
  PRIMARY KEY(id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- 7) groups_users (중계 테이블)
CREATE TABLE groups_users (
  user_id    INT         NOT NULL COMMENT '사용자',
  group_id   INT         NOT NULL COMMENT '그룹',
  role       VARCHAR(10) NULL COMMENT '역할',
  joined_at  TIMESTAMP   NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '참여일시',
  PRIMARY KEY(user_id, group_id),
  CONSTRAINT fk_gu_user FOREIGN KEY(user_id) REFERENCES users(id)
    ON DELETE RESTRICT ON UPDATE CASCADE,
  CONSTRAINT fk_gu_group FOREIGN KEY(group_id) REFERENCES `groups`(id)
    ON DELETE RESTRICT ON UPDATE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;




-- 10) diaries
CREATE TABLE diaries (
  id            INT         NOT NULL AUTO_INCREMENT COMMENT '일기 ID',
  user_id       INT         NOT NULL COMMENT '사용자',
  emotion_id    TINYINT     NOT NULL COMMENT '감정',
  title         VARCHAR(30) NULL COMMENT '제목',
  content       TEXT        NOT NULL COMMENT '내용',
  thumbnail     VARCHAR(512) NULL COMMENT '썸네일',
  prompt        TEXT        NULL COMMENT '프롬프트',
  messages      JSON        null COMMENT '대화내역',
  is_deleted    BOOLEAN     NOT NULL DEFAULT FALSE COMMENT '삭제',
  created_at    TIMESTAMP   NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '생성일시',
  updated_at    TIMESTAMP   NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '수정일시',
  PRIMARY KEY(id),
  CONSTRAINT fk_di_user    FOREIGN KEY(user_id) REFERENCES users(id)
    ON DELETE RESTRICT ON UPDATE CASCADE,
  CONSTRAINT fk_di_emotion FOREIGN KEY(emotion_id) REFERENCES emotion(id)
    ON DELETE RESTRICT ON UPDATE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- 11) group_diaries (중계 테이블)
CREATE TABLE group_diaries (
  id         INT       NOT NULL AUTO_INCREMENT COMMENT '그룹일기 ID',
  diary_id   INT       NOT NULL COMMENT '일기',
  group_id   INT       NOT NULL COMMENT '그룹',
  shared_at  TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '공유일시',
  PRIMARY KEY(id),
  CONSTRAINT fk_gd_diary FOREIGN KEY(diary_id) REFERENCES diaries(id)
    ON DELETE RESTRICT ON UPDATE CASCADE,
  CONSTRAINT fk_gd_group FOREIGN KEY(group_id) REFERENCES `groups`(id)
    ON DELETE RESTRICT ON UPDATE CASCADE,
  UNIQUE KEY `unique_group_diaries` (`diary_id`, `group_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- 12) comments
CREATE TABLE comments (
  id             INT           NOT NULL AUTO_INCREMENT COMMENT '댓글 ID',
  user_id        INT           NOT NULL COMMENT '사용자',
  group_diary_id INT           NOT NULL COMMENT '그룹일기',
  content        VARCHAR(255)  NOT NULL COMMENT '내용',
  is_deleted     BOOLEAN       NOT NULL DEFAULT FALSE COMMENT '삭제여부',
  created_at     TIMESTAMP     NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '작성일시',
  updated_at     TIMESTAMP     NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '수정일시',
  PRIMARY KEY(id),
  CONSTRAINT fk_cm_user FOREIGN KEY(user_id) REFERENCES users(id)
    ON DELETE RESTRICT ON UPDATE CASCADE,
  CONSTRAINT fk_cm_gd   FOREIGN KEY(group_diary_id) REFERENCES group_diaries(id)
    ON DELETE RESTRICT ON UPDATE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

