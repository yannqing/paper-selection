# 使用 OpenJDK 17 slim 版本作为基础镜像
FROM openjdk:17-slim

# 设置维护者标签
LABEL maintainer="yannqing <yannqing.com>"
LABEL version="1.0"
LABEL description="Paper Selection Backend"

# 安装字体
# 安装字体和 fontconfig
#RUN apt-get update && apt-get install -y \
#    fontconfig \
#    fonts-dejavu \
#    && apt-get clean \
#    && rm -rf /var/lib/apt/lists/*

# 设置工作目录
WORKDIR /yannqing/paper-selection/paper-selection-backend/java

# 创建一个挂载点
VOLUME /yannqing/paper-selection/paper-selection-backend/logs

# 复制应用程序
COPY ./target/paperSelection-0.0.1-SNAPSHOT.jar /tmp/app.jar

# 暴露端口
EXPOSE 8093

# 启动命令
CMD ["java", "-jar", "/tmp/app.jar", "--spring.profiles.active=test"]
