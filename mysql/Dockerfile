FROM mysql:8.0.42

ENV TZ=Asia/Seoul

COPY mysql.cnf /etc/mysql/conf.d/custom.cnf

COPY init/ /docker-entrypoint-initdb.d/

EXPOSE 3306


