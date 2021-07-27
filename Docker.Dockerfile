#This is a sample Image 
FROM ubuntu 
MAINTAINER hellomohanakrishnan@gmail.com 

RUN apt-get update -y
RUN apt-get upgrade -y
RUN apt-get update && DEBIAN_FRONTEND=noninteractive apt-get install -yq apt-utils
RUN DEBIAN_FRONTEND=noninteractive apt-get install build-essential chrpath libssl-dev libxft-dev libfreetype6-dev libfreetype6 libfontconfig1-dev libfontconfig1 -y
RUN DEBIAN_FRONTEND=noninteractive apt-get install -yq wget
RUN wget https://bitbucket.org/ariya/phantomjs/downloads/phantomjs-2.1.1-linux-x86_64.tar.bz2
RUN tar xvjf phantomjs-2.1.1-linux-x86_64.tar.bz2 -C /usr/local/share/
RUN ln -s /usr/local/share/phantomjs-2.1.1-linux-x86_64/bin/phantomjs /usr/local/bin/
RUN phantomjs --version
CMD [“echo”,”==> phantomjs installed”] 

RUN apt-get update -y
RUN apt-get upgrade -y
RUN DEBIAN_FRONTEND=noninteractive apt-get install -yq openjdk-8-jdk
RUN java -version
CMD [“echo”,”==> java installed”] 

WORKDIR /
#COPY analysis-news-and-time-0.0.1-SNAPSHOT.jar analysis-news-and-time-0.0.1-SNAPSHOT.jar
#ADD https://github.com/NaughtyCodes/ANT/blob/master/target/analysis-news-and-time-0.0.1-SNAPSHOT.jar analysis-news-and-time-0.0.1-SNAPSHOT.jar
RUN wget https://github.com/NaughtyCodes/ANT/blob/master/target/analysis-news-and-time-0.0.1-SNAPSHOT.jar
#ADD ./ANT/target/*.jar analysis-news-and-time-0.0.1-SNAPSHOT.jar
RUN ls -lrt
RUN chmod 777 analysis-news-and-time-0.0.1-SNAPSHOT.jar
#RUN mv analysis-news-and-time-0.0.1-SNAPSHOT.jar /analysis-news-and-time-0.0.1-SNAPSHOT.jar
CMD [“echo”,”==> getting ant jar”] 
#EXPOSE 8080
#CMD java -jar analysis-news-and-time-0.0.1-SNAPSHOT.jar
ENTRYPOINT [“java","-jar","/analysis-news-and-time-0.0.1-SNAPSHOT.jar”]

