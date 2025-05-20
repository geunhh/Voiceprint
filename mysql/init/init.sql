CREATE DATABASE IF NOT EXISTS voiceprint_db DEFAULT CHARACTER SET utf8mb4;
USE voiceprint_db;

-- 1) chatbot
CREATE TABLE `chatbot` (
  `id`           TINYINT NOT NULL AUTO_INCREMENT,
  `name`         VARCHAR(30) NOT NULL,
  `description`  VARCHAR(255) DEFAULT NULL,
  `image_url`    VARCHAR(512) DEFAULT NULL,
  `prompt`       TEXT,
  `is_deleted`   BIT(1) DEFAULT NULL,
  `created_at`   DATETIME(0) DEFAULT NULL,
  `updated_at`   DATETIME(0) DEFAULT NULL,
   PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;
 
-- 2) profile_images
CREATE TABLE `profile_images` (
  `id`            TINYINT NOT NULL AUTO_INCREMENT,
  `title`         VARCHAR(255) NOT NULL,
  `image_url`     VARCHAR(255)  NOT NULL,
  `created_at`    DATETIME(0) NOT NULL,
   PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;
 
-- 3) emotion
CREATE TABLE `emotion` (
  `id`                TINYINT NOT NULL AUTO_INCREMENT,
  `name`              VARCHAR(10)  DEFAULT NULL,
  `color`             VARCHAR(10) DEFAULT NULL,
   PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;
 
-- 4) prompt_questions
CREATE TABLE `prompt_questions` (
  `id`                TINYINT NOT NULL AUTO_INCREMENT,
  `question`          VARCHAR(50) DEFAULT NULL,
  `created_at`        DATETIME(0) DEFAULT NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;
 
 -- 5) today_question
CREATE TABLE `today_question` (
  `date`              DATE NOT NULL,
  `question_id`       TINYINT NOT NULL,
  PRIMARY KEY (`date`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- 6) users
CREATE TABLE `users` (
  `id`                INT NOT NULL AUTO_INCREMENT,
  `profile_image_id`  TINYINT DEFAULT NULL,
  `using_thema_id`    INT DEFAULT NULL,
  `last_chatbot_id`   TINYINT DEFAULT NULL,
  `provider_id`       VARCHAR(50) NOT NULL,
  `nickname`          VARCHAR(30) NOT NULL,
  `auth_provider`     ENUM('google','kakao') NOT NULL,
  `alarm_time`        TIME DEFAULT '21:00:00',
  `enable_alarm`      TINYINT(1) DEFAULT NULL,
  `is_deleted`        BIT(1) NOT NULL,
  `created_at`        DATETIME(0) NOT NULL,
  `updated_at`        DATETIME(0) NOT NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;
 
 -- 7) notification
CREATE TABLE `notifications` (
  `id`                BIGINT NOT NULL AUTO_INCREMENT,
  `user_id`           INT NOT NULL,
  `type`              VARCHAR(20)  NOT NULL,
  `message`           VARCHAR(50)  NOT NULL,
  `metadata`          JSON DEFAULT NULL,
  `is_read`           BIT(1) NOT NULL,
  `created_at`        DATETIME(0) NOT NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;
 
 -- 8) diary
CREATE TABLE `diary` (
  `id`                INT NOT NULL AUTO_INCREMENT,
  `user_id`           INT DEFAULT NULL,
  `emotion_id`        TINYINT DEFAULT NULL,
  `title`             VARCHAR(30) NOT NULL,
  `content`           TEXT,
  `thumbnail`         VARCHAR(512) DEFAULT NULL,
  `prompt`            TEXT,
  `messages`          JSON DEFAULT NULL,
  `is_deleted`        BIT(1) DEFAULT NULL,
  `created_at`        DATETIME(0) DEFAULT NULL,
  PRIMARY KEY (`id`)
 ) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;
 
-- 9) groups
CREATE TABLE `groups` (
  `id`                INT NOT NULL AUTO_INCREMENT,
  `name`              VARCHAR(30) NOT NULL,
  `description`       VARCHAR(255) DEFAULT NULL,
  `group_image`       VARCHAR(255) NOT NULL,
  `is_deleted`        TINYINT(1) NOT NULL DEFAULT '0',
  `enable_alarm`      TINYINT(1) NOT NULL DEFAULT '0',
  `alarm_day`         VARCHAR(255) DEFAULT NULL,
  `alarm_time`        TIME DEFAULT NULL,
  `created_at`        DATETIME(0) NOT NULL,
  `updated_at`        DATETIME(0) NOT NULL, 
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;
 
-- 10) groups_users
CREATE TABLE `groups_users` (
  `user_id`           INT NOT NULL,
  `group_id`          INT NOT NULL,
  `role`              ENUM('ADMIN','MEMBER') NOT NULL,
  `joined_at`         DATETIME(0) NOT NULL,
  PRIMARY KEY (`group_id`,`user_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;
 
-- 11) group_diaries
CREATE TABLE `group_diaries` (
  `id`                INT NOT NULL AUTO_INCREMENT,
  `diary_id`          INT NOT NULL,
  `group_id`          INT NOT NULL,
  `shared_at`         DATETIME(0) NOT NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;
 
-- 12) group_invite
CREATE TABLE `group_invite` (
  `id`                INT NOT NULL AUTO_INCREMENT,
  `inviter_id`        INT DEFAULT NULL,
  `group_id`          INT DEFAULT NULL,
  `invite_code`       VARCHAR(16) NOT NULL,
  `created_at`        DATETIME(0) NOT NULL,
  `expired_at`        DATETIME(0) NOT NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;
 
-- 13) comment
CREATE TABLE `comment` (
  `id`                INT NOT NULL AUTO_INCREMENT,
  `user_id`           INT DEFAULT NULL,
  `group_diary_id`    INT DEFAULT NULL,
  `content`           VARCHAR(255) NOT NULL,
  `is_deleted`        BIT(1) NOT NULL,
  `created_at`        DATETIME(0) DEFAULT NULL,
  `updated_at`        DATETIME(0) DEFAULT NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;
 
-- 14) diary_themas
CREATE TABLE `diary_themas` (
  `id`                INT NOT NULL AUTO_INCREMENT,
  `user_id`           INT DEFAULT NULL,
  `title`             VARCHAR(10) DEFAULT NULL,
  `description`       VARCHAR(100) DEFAULT NULL,
  `example`           VARCHAR(255) DEFAULT NULL,
  `prompt`            TEXT NOT NULL,
  `created_at`        DATETIME(0) DEFAULT NULL,
  `updated_at`        DATETIME(0) DEFAULT NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;
 
-- ================================================================================
-- 제약 조건
-- ================================================================================
ALTER TABLE `diary_themas`
ADD CONSTRAINT `uk_diary_themas_user_id` UNIQUE (`user_id`),
ADD CONSTRAINT `fk_diary_themas_user_id` FOREIGN KEY (`user_id`) REFERENCES `users` (`id`);

ALTER TABLE `group_invite`
ADD CONSTRAINT `uk_group_invite_code` UNIQUE (`invite_code`),
ADD CONSTRAINT `fk_group_invite_inviter_id` FOREIGN KEY (`inviter_id`) REFERENCES `users` (`id`),
ADD CONSTRAINT `fk_group_invite_group_id` FOREIGN KEY (`group_id`) REFERENCES `groups` (`id`);

ALTER TABLE `comment`
ADD CONSTRAINT `fk_comment_diary_id` FOREIGN KEY (`user_id`) REFERENCES `users` (`id`),
ADD CONSTRAINT `fk_comment_group_diary_id` FOREIGN KEY (`group_diary_id`) REFERENCES `group_diaries` (`id`);

ALTER TABLE `group_diaries`
ADD CONSTRAINT `fk_group_diaries_diary_id` FOREIGN KEY (`diary_id`) REFERENCES `diary` (`id`),
ADD CONSTRAINT `fk_group_diaries_group_id` FOREIGN KEY (`group_id`) REFERENCES `groups` (`id`);

ALTER TABLE `groups_users`
ADD CONSTRAINT `fk_gu_user` FOREIGN KEY (`user_id`) REFERENCES `users` (`id`),
ADD CONSTRAINT `fk_gu_group` FOREIGN KEY (`group_id`) REFERENCES `groups` (`id`);

ALTER TABLE `diary`
ADD CONSTRAINT `fk_diary_user_id` FOREIGN KEY (`user_id`) REFERENCES `users` (`id`),
ADD CONSTRAINT `fk_diary_emotion_id` FOREIGN KEY (`emotion_id`) REFERENCES `emotion` (`id`);

ALTER TABLE `notifications`
ADD CONSTRAINT `fk_notifications_user_id` FOREIGN KEY (`user_id`) REFERENCES `users` (`id`);

ALTER TABLE `users`
ADD CONSTRAINT `fk_users_profile_image_id` FOREIGN KEY (`profile_image_id`) REFERENCES `profile_images` (`id`),
ADD CONSTRAINT `fk_users_using_thema_id` FOREIGN KEY (`using_thema_id`) REFERENCES `diary_themas` (`id`),
ADD CONSTRAINT `fk_users_last_chatbot_id` FOREIGN KEY (`last_chatbot_id`) REFERENCES `chatbot` (`id`);

-- ===========================================================================
-- 데이터 삽입
-- ===========================================================================
-- 1) chatbot 
INSERT INTO `chatbot` (`name`, `description`, `image_url`, `prompt`, `created_at`, `updated_at`, `is_deleted`)
VALUES 
('따분이', '#시니컬  #로봇바이브', "https://ssafy-voiceprint.s3.ap-northeast-2.amazonaws.com/chatbot/chatBlack.png", '시니컬하고 로봇같은 톤으로 응답해주세요.', now(), now(), 0),
('맑음이', '#명랑  #쿨톤', "https://ssafy-voiceprint.s3.ap-northeast-2.amazonaws.com/chatbot/chatBlue.png", '명랑하고 쿨톤 느낌으로 상쾌하게 말해주세요.', now(), now(), 0),
('설렘이', '#러블리  #핑크러버', "https://ssafy-voiceprint.s3.ap-northeast-2.amazonaws.com/chatbot/chatPink.png", '러블리하고 설레는 말투로 응답해주세요.', now(), now(), 0),
('열정이', '#에너지  #리더', "https://ssafy-voiceprint.s3.ap-northeast-2.amazonaws.com/chatbot/chatRed.png", '에너지 넘치고 리더십 있는 톤으로 답변해주세요.', now(), now(), 0),
('햇살이', '#긍정  #따뜻함', "https://ssafy-voiceprint.s3.ap-northeast-2.amazonaws.com/chatbot/chatYellow.png", '따뜻하고 긍정적인 말투로 공감해주세요.', now(), now(), 0);

-- 2) profile_images
INSERT INTO `profile_images` (`image_url`, `title`, `created_at`)
VALUES 
('https://ssafy-voiceprint.s3.ap-northeast-2.amazonaws.com/profile/profile1.png', '프로필1', now()),
('https://ssafy-voiceprint.s3.ap-northeast-2.amazonaws.com/profile/profile2.png', '프로필2', now()),
('https://ssafy-voiceprint.s3.ap-northeast-2.amazonaws.com/profile/profile3.png', '프로필3', now()),
('https://ssafy-voiceprint.s3.ap-northeast-2.amazonaws.com/profile/profile4.png', '프로필4', now()),
('https://ssafy-voiceprint.s3.ap-northeast-2.amazonaws.com/profile/profile5.png', '프로필5', now()),
('https://ssafy-voiceprint.s3.ap-northeast-2.amazonaws.com/profile/profile6.png', '프로필6', now()),
('https://ssafy-voiceprint.s3.ap-northeast-2.amazonaws.com/profile/profile7.png', '프로필7', now()),
('https://ssafy-voiceprint.s3.ap-northeast-2.amazonaws.com/profile/profile8.png', '프로필8', now()),
('https://ssafy-voiceprint.s3.ap-northeast-2.amazonaws.com/profile/profile9.png', '프로필9', now());

-- 3) emotion
INSERT INTO `emotion` (`name`, `color`)
VALUES 
('행복', '분홍'),
('설렘', '빨강'),
('피로', '주황'),
('짜증', '파랑'),
('우울', '보라');

-- 4) prompt_questions
INSERT INTO `prompt_questions` (`id`, `created_at`, `question`)
VALUES
  ( 1, now(), '오늘 아침 기분은 어떤 느낌이었나요?'),
  ( 2, now(), '오늘 첫 번째로 떠오른 생각은 무엇이었나요?'),
  ( 3, now(), '오늘 가장 많이 웃게 만든 순간은 언제였나요?'),
  ( 4, now(), '오늘 하루 중 가장 작은 성취는 무엇이었나요?'),
  ( 5, now(), '오늘 만난 사람 중 기억에 남는 대화는 무엇이었나요?'),
  ( 6, now(), '오늘 비슷한 루틴 중 새롭게 시도한 행동이 있나요?'),
  ( 7, now(), '오늘 나를 가장 당황하게 한 일이 있었다면 무엇인가요?'),
  ( 8, now(), '오늘 나를 가장 편안하게 만든 장소나 순간은 언제였나요?'),
  ( 9, now(), '오늘의 날씨가 내 기분에 어떤 영향을 주었나요?'),
  (10, now(), '오늘 하루 세 끼 중 가장 맛있었던 식사는 무엇이었나요?'),
  (11, now(), '오늘 내 감정을 한 단어로 표현한다면 무엇일까요?'),
  (12, now(), '오늘 하루 중 가장 집중이 잘 됐던 시간은 언제였나요?'),
  (13, now(), '오늘 날씨를 동물에 비유한다면 어떤 동물일까요?'),
  (14, now(), '오늘 새로 알게 된 사실이나 배운 점은 무엇인가요?'),
  (15, now(), '오늘 나를 가장 자극한 소리나 노래가 있나요?'),
  (16, now(), '오늘 하루 중 가장 감사했던 순간은 언제인가요?'),
  (17, now(), '오늘 내가 꼭 지키고 싶은 작은 약속이 있었나요?'),
  (18, now(), '오늘 느낀 스트레스를 한 문장으로 풀어보세요.'),
  (19, now(), '오늘의 나에게 격려 한 마디를 건넨다면?'),
  (20, now(), '오늘 가장 인상 깊게 본 풍경이나 사물은 무엇이었나요?'),
  (21, now(), '오늘 내가 스스로 칭찬해주고 싶은 일은 무엇인가요?'),
  (22, now(), '오늘 무언가를 포기하게 된 순간이 있었나요?'),
  (23, now(), '오늘 나를 설레게 만든 계획이나 기대는 무엇이었나요?'),
  (24, now(), '오늘 우연히 떠오른 추억이 있다면 어떤 기억인가요?'),
  (25, now(), '오늘 나의 선택 중 가장 즉흥적이었던 것은 무엇인가요?'),
  (26, now(), '오늘의 나에게 필요한 한 가지는 무엇일까요?'),
  (27, now(), '오늘 하루 동안 가장 많이 사용한 단어나 표현은 무엇인가요?'),
  (28, now(), '오늘 한 가지 버리고 싶은 생각이나 감정이 있다면?'),
  (29, now(), '오늘 하루가 끝나면 하고 싶은 일은 무엇인가요?'),
  (30, now(), '오늘의 나를 내일의 나에게 추천하고 싶은 점은 무엇인가요?');

-- 5) today_question
INSERT INTO `today_question` (`date`, `question_id`)
VALUES ('2025-05-20', 1);

-- 6) diary_themas
INSERT INTO diary_themas (`user_id`,`title`,`description`,`example`,`prompt`,`created_at`,`updated_at`) VALUES
(
    NULL,
    '발랄한 일기',
    '소소한 행복과 감성을 담아내는 테마',
    '이불 속에서 뒹굴거리다 겨우 일어나 커피 한 잔 마셨는데, 어쩐지 오늘 커피맛이 정말 특별하게 느껴졌다. 기분 좋은 하루가 시작되는 느낌이랄까. 🥰☕집 앞 꽃집에 들렀다가 노란 미니 해바라기를 두 송이 샀다.',
    '프롬프트 A',
    NOW(),
    NOW()
),
(
    NULL,
    '유쾌한 대화형 일기',
    '대화하는 듯한 어투의 밝고 유쾌한 테마',
    '오늘은 제가 커피 쿠폰 10개를 어떻게 한 번에 다 써버렸는지 말씀드릴게요! 여러분도 이런 적 있으시죠? 아이 키우다 보면 자신만의 시간이 절~~~대 없잖아요. 맞죠? 오늘 드디어! 남편이 "여보, 오늘은 내가 애 볼 테니 나가서 좀 쉬다 와요"라고 하더라고요. ',
    '프롬프트 B',
    NOW(),
    NOW()
),
(
    NULL,
    '담담한 사색형 일기',
    '일상 속 작은 의미를 담아내는 테마',
    '아침엔 날이 좋았는데 갑자기 비가 왔네. 이런 날은 창가에 앉아 차 한잔과 책이 제격이다. 오래된 찻잔에 녹차를 우려 마시면서 창밖을 바라본다. 빗방울이 창문에 부딪히는 소리가 묘하게 마음을 편안하게 해준다.',
    '프롬프트 C',
    NOW(),
    NOW()
);

