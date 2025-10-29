CREATE DATABASE IF NOT EXISTS notification_db DEFAULT CHARACTER SET utf8mb4;

-- Create a exporter 전용 유저.
CREATE USER IF NOT EXISTS 'exporter'@'%' IDENTIFIED BY 'exporter_password_notif';

-- 최소 권한 부여
GRANT SELECT, PROCESS, REPLICATION CLIENT ON *.* TO 'exporter'@'%';
FLUSH PRIVILEGES;

-- 확인
SELECT User, Host, plugin FROM mysql.user WHERE User='exporter';
SHOW GRANTS FOR 'exporter'@'%';