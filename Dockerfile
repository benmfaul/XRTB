FROM maven:3-jdk-8-alpine

RUN mkdir -p /usr/src/app
WORKDIR /usr/src/app

ADD . /usr/src/app

RUN mvn site && \
	mvn assembly:assembly -DdescriptorId=jar-with-dependencies -Dmaven.test.skip=true

VOLUME /usr/src/app/logs /usr/src/app/Campaigns /usr/src/app/data

ENV HEAP_SIZE 1g

CMD /usr/bin/java -Xmx${HEAP_SIZE} -jar target/XRTB-0.0.1-SNAPSHOT-jar-with-dependencies.jar