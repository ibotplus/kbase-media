FROM openjdk:8-jre-alpine

# Replace repository as domestic source
RUN echo "https://mirror.tuna.tsinghua.edu.cn/alpine/v3.9/main" > /etc/apk/repositories \
    && echo "https://mirror.tuna.tsinghua.edu.cn/alpine/v3.9/community" >> /etc/apk/repositories \
    && apk update \
    && apk add yasm && apk add ffmpeg

# install kbase-media
VOLUME /tmp

ARG DEPENDENCY=target/dependency
COPY ${DEPENDENCY}/BOOT-INF/lib /app/lib
COPY ${DEPENDENCY}/META-INF /app/META-INF
COPY ${DEPENDENCY}/BOOT-INF/classes /app

COPY application.yml /app/config/

RUN echo "Asia/Shanghai" > /etc/timezone

ENTRYPOINT ["java", "-Xmx512M", "-cp", "app:app/lib/*", "com.eastrobot.kbs.media.KbaseMediaApp", "--spring.config.location=file:/app/config/application.yml"]
