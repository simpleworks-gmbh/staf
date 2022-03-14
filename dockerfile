FROM ubuntu:18.04

###
## Install needed software
#

# refresh package list
RUN apt update -y

# install java openjdk 8
RUN apt install openjdk-8-jdk -y

# install unzip
RUN apt install unzip -y

# install wget
RUN apt install wget -y 

# fetch maven-3.8.5
RUN wget https://dlcdn.apache.org/maven/maven-3/3.8.5/binaries/apache-maven-3.8.5-bin.zip

# unzip maven-3.8.5
RUN unzip apache-maven-3.8.5-bin.zip

RUN mv apache-maven-3.8.5 /opt/maven

# Set mvn environment variables
ENV M2_HOME=/opt/maven
ENV MAVEN_HOME=/opt/maven
ENV PATH=${M2_HOME}/bin:${PATH}

###
## configure software build
#

# copy work directory to mount point
COPY . /mnt

# setting working directory
WORKDIR /mnt

# create directory for test artefacts
RUN mkdir artefactory

# remove maven repository
RUN rm -rf ~/.m2

RUN mvn clean install dependency:resolve dependency:resolve-plugins