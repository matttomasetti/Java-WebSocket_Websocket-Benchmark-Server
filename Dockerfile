FROM ubuntu

# Set timezone and environment variables
ENV TZ=America/New_York
ENV PATH=$PATH:/usr/bin/node
ENV GRADLE_HOME=/opt/gradle/gradle-5.0
ENV PATH=${GRADLE_HOME}/bin:${PATH}
RUN ln -snf /usr/share/zoneinfo/$TZ /etc/localtime && echo $TZ > /etc/timezone

# Add source files to docker image
ADD .	/home/websocket

# Update and install dependencies
RUN apt-get -y update \
    && apt-get -y upgrade \
    && apt-get -y install openjdk-8-jdk wget unzip

# Install Gradle
RUN wget https://services.gradle.org/distributions/gradle-5.0-bin.zip -P /tmp \
    && unzip -d /opt/gradle /tmp/gradle-*.zip

# Build project
RUN cd /home/websocket \
    && gradle build
    
EXPOSE 8080

WORKDIR /home/websocket
CMD ["gradle", "run"]
