FROM openjdk:8-jdk-alpine

# install ffmpeg
RUN apk update
RUN apk add yasm && apk add ffmpeg


# install kbase-media
VOLUME /tmp

ARG DEPENDENCY=target/dependency
COPY ${DEPENDENCY}/BOOT-INF/lib /app/lib
COPY ${DEPENDENCY}/META-INF /app/META-INF
COPY ${DEPENDENCY}/BOOT-INF/classes /app

COPY application.yml /app/config/

RUN echo "Asia/Shanghai" > /etc/timezone

ENTRYPOINT ["java", "-Xmx512M", "-cp", "app:app/lib/*", "com.eastrobot.kbs.media.KbaseMediaApp", "--spring.config.location=file:/app/config/application.yml"]
