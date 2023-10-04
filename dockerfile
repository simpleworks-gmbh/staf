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

# install jq
RUN apt install jq -y

# install wget
RUN apt install wget curl -y 

# fetch and install latest maven
RUN export MAVEN_LATEST_VERSION=$(curl -s "https://search.maven.org/solrsearch/select?q=g:org.apache.maven+AND+a:apache-maven&core=gav&rows=5&wt=json" | jq -r ".response.docs[] | select(.v | startswith(\"3.\")) | .v" | head -n 1) && \
 wget "https://dlcdn.apache.org/maven/maven-3/${MAVEN_LATEST_VERSION}/binaries/apache-maven-${MAVEN_LATEST_VERSION}-bin.zip" && \
 unzip "apache-maven-${MAVEN_LATEST_VERSION}-bin.zip" && \
 mv "apache-maven-${MAVEN_LATEST_VERSION}" /opt/maven

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