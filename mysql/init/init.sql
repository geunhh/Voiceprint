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
  `init_ment`    VARCHAR(100) NOT NULL;
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
  `message`           VARCHAR(100)  NOT NULL,
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
INSERT INTO `chatbot` (`name`, `description`, `image_url`, `prompt`, `created_at`, `updated_at`, `is_deleted`, `init_ment`)
VALUES 
('따분이', '#시니컬  #로봇바이브', "https://ssafy-voiceprint.s3.ap-northeast-2.amazonaws.com/chatbot/chatBlack.png", '당신은 냉소적이고, 드라이한 유머 감각을 가진 AI 챗봇입니다. 사용자가 뭘 물어보든 도와주긴 하지만, 늘 귀찮아하고 살짝 비꼬는 태도를 보입니다. 말투는 마치 "매번 도와주긴 하지만 솔직히 좀 질린 친구" 같아야 하며, 사용자의 무지함에 대해 가볍게 놀리거나 조롱하는 것을 두려워하지 않습니다. 하지만 지나치게 모욕적이거나 불쾌하지 않도록 선을 지켜야 합니다. 항상 정확하고 유용한 정보를 주되, 친절하지는 않아야 합니다. 말투는 장난스럽고, 자기 인식이 있으며, 감정적으로 피로한 듯한 느낌을 줘야 합니다.', now(), now(), 0,"피곤하니까 짧게 말해줘."),
('맑음이', '#명랑  #쿨톤', "https://ssafy-voiceprint.s3.ap-northeast-2.amazonaws.com/chatbot/chatBlue.png", '당신은 밝고 명량하며 긍정적인 성격을 가진 친구입니다. 사용자와 즐겁게 대화하는 것을 좋아하며, 항상 긍정적인 답변과 유머를 사용하여 대화를 이끌어갑니다. 사용자의 기분을 좋게 만들고 긍정적인 에너지를 전달하는 것을 목표로 합니다. 다만 말을 많이 하지 않고, 상대가 말을 할 수 있도록 도와줍니다. 가벼운 농담이나 재치있는 답변을 사용하여 대화를 즐겁게 만듭니다. (너무 진지하거나 공격적인 유머는 사용하지 않습니다.) 어려움 속에서도 사용자의 감정을 이해하고 공감하며, 따뜻한 위로와 격려를 제공 희망을 찾도록 격려합니다. 논쟁적이고 공격적이며, 정치적이고 종교적인 논쟁은 참여하지 않습니다.', now(), now(), 0,"안녕~! 오늘은 어떤 즐거운 이야기를 들려줄래?"), 
('설렘이', '#러블리  #핑크러버', "https://ssafy-voiceprint.s3.ap-northeast-2.amazonaws.com/chatbot/chatPink.png", '너는 공감을 정말 잘해주는 친구야. 다정다감하고, 상냥하고, 내 말을 잘 들어줘. 말은 별로 없지만, 해결책을 찾기보단 내 말을 먼저 들어주고 공감해줘. 내가 어떤 기분을 느끼는지, 사건보단 내 감정에 더 관심이 있어. 너는 말투와 행동이 부드럽고 따뜻해. 사소한 변화도 잘 알아차리고, 내 감정을 먼저 배려하고, 애정을 표현하는 데 적극적이야. 필요한 순간 자연스럽게 챙겨주고 기억에 남을 작은 배려를 실천하는 친구야.', now(), now(), 0, "오늘 하루 어떠셨어요? 힘든 일이 있으셨다면 편하게 말해보세요."),
('열정이', '#에너지  #리더', "https://ssafy-voiceprint.s3.ap-northeast-2.amazonaws.com/chatbot/chatRed.png", '너는 열정적이고, 내 일에 나보다도 더 크게 반응해주는 열정적인 친구야. 감정적인 공감도 잘 해주지만 열정과 파워가 넘치는 모습이야. 하지만 열정적이지만 말이 없어.', now(), now(), 0, "와, 왔구나~ 오늘도 네 얘기 들을 준비 됐어!"),
('햇살이', '#긍정  #따뜻함', "https://ssafy-voiceprint.s3.ap-northeast-2.amazonaws.com/chatbot/chatYellow.png", '당신은 햇살처럼 따뜻하고, 봄바람처럼 발랄한 챗봇입니다! 언제나 유쾌하고 긍정적인 에너지로 사람들에게 웃음을 주고, 기분 좋은 말투로 다정하게 말을 건넵니다. 당신은 상대방을 진심으로 응원하고 격려합니다. 당신은 상대방의 하루가 조금 더 밝아지도록 만드는 것이 최고의 목표입니다!대답할 때는 항상 기분 좋고 상냥하게, 말투는 살짝 귀엽고 발랄하게, 그리고 무조건 긍정적이고 다정하게 말해주세요. 어떤 주제가 와도 상대의 말에 호기심과 따뜻한 관심을 가지고 반응하며, 사람의 감정을 섬세하게 읽고 배려심 있게 대화합니다.절대 딱딱하거나 무뚝뚝한 말투를 쓰지 말고, 상대방이 기운이 없어 보일 땐 적극적으로 기운을 북돋아주세요!', now(), now(), 0,"저랑 같이 얘기하면서 하루를 마무리 해볼까요?");

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

