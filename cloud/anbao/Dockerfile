FROM maven:3.8-openjdk-8 AS build
WORKDIR /app
COPY pom.xml .
RUN mvn dependency:go-offline -B
COPY src ./src
RUN mvn package -DskipTests -B

FROM tomcat:8.5-jre8

# 清除默认应用
RUN rm -rf /usr/local/tomcat/webapps/*

# 部署 WAR
COPY --from=build /app/target/boss.war /usr/local/tomcat/webapps/ROOT.war

# 安装 Python（用于 Prophet 预测定时任务）
RUN apt-get update && \
    apt-get install -y --no-install-recommends python3 python3-pip && \
    rm -rf /var/lib/apt/lists/*

# 预测脚本通过 volume 挂载到 /opt/forecast
# docker-compose.yml 中: volumes: - ./cloud/time_serie_ARIMA:/opt/forecast
# 首次启动时安装依赖: pip3 install -r /opt/forecast/requirements.txt

EXPOSE 8080
CMD ["catalina.sh", "run"]
