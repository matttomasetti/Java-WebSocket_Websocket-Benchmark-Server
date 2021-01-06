FROM ubuntu

ENV TZ=America/New_York
ENV PATH=$PATH:/usr/bin/node
ENV GRADLE_HOME=/opt/gradle/gradle-5.0
ENV PATH=${GRADLE_HOME}/bin:${PATH}
RUN ln -snf /usr/share/zoneinfo/$TZ /etc/localtime && echo $TZ > /etc/timezone

ADD .	/home/websocket

RUN apt-get -y update \
    && apt-get -y upgrade \
    #install dependencies
    && apt-get -y install openjdk-8-jdk wget unzip \
    && wget https://services.gradle.org/distributions/gradle-5.0-bin.zip -P /tmp \
    && unzip -d /opt/gradle /tmp/gradle-*.zip \
    && cd /home/websocket \
    && gradle build
    
EXPOSE 8080

WORKDIR /home/websocket
CMD ["gradle", "run"]
