# 构建阶段
FROM 10.170.6.40:30002/maven/maven:3.6-jdk-8 AS builder

WORKDIR /usr/src/app

# 复制 pom.xml 和 settings.xml，利用缓存
COPY ./pom.xml /usr/src/app/pom.xml
COPY ./settings.xml /usr/src/app/settings.xml

# 下载依赖
RUN mvn dependency:go-offline -s /usr/src/app/settings.xml || (echo "Qtech-Im dependency download failed" && exit 1)

# 复制源代码
COPY ./src /usr/src/app/src

# 编译打包
RUN mvn -f /usr/src/app/pom.xml clean package -DskipTests -s /usr/src/app/settings.xml dependency:resolve || (echo "Qtech-Service-Api build failed" && exit 1) && \
    rm -rf /root/.m2/repository

# 运行阶段
FROM 10.170.6.40:30002/java8/openjdk:8-jre-alpine-font
ARG MAINTAINER_EMAIL
LABEL maintainer=${MAINTAINER_EMAIL}

# 设置时区为上海时间
RUN apk add --no-cache tzdata && \
    cp /usr/share/zoneinfo/Asia/Shanghai /etc/localtime && \
    echo "Asia/Shanghai" > /etc/timezone && \
    apk del tzdata

# 环境变量
ENV PARAMS="--server.port=8080"
ENV LOG_PATH=/home/qtech/logs
ENV SPRING_PROFILES_ACTIVE=prod
ENV JAVA_OPTS="-Dloader.path=/home/libs/ -Dfile.encoding=utf8 -Djava.security.egd=file:/dev/./urandom -Dlogging.file.path=${LOG_PATH}"

# 创建日志目录
RUN mkdir -p ${LOG_PATH} && chmod -MesR 777 ${LOG_PATH}

EXPOSE 8080

# 复制构建阶段的输出
COPY --from=builder /usr/src/app/target/*.jar /home/app.jar
COPY --from=builder /usr/src/app/target/libs /home/libs/

WORKDIR /home

# 健康检查
HEALTHCHECK --interval=30s --timeout=30s --retries=5 \
  CMD curl -f http://localhost:8080/actuator/health/status || exit 1

# 启动命令
ENTRYPOINT ["sh", "-c", "java ${JAVA_OPTS} -jar /home/app.jar ${PARAMS} --spring.profiles.active=${SPRING_PROFILES_ACTIVE}"]