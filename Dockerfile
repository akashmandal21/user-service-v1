FROM ubuntu:16.04
ADD . /opt/user/
ADD  core-utils/ /opt/core-utils/
RUN set -x && \
  apt-get update -y && \
  apt-get upgrade -y && \
  apt-get -y install -y software-properties-common ssh vim net-tools python-minimal python-pip sudo git p7zip-full && \
  add-apt-repository -y ppa:openjdk-r/ppa && \
  apt-get update -y && \
  apt-get install openjdk-8-jdk -y && \
  rm -rf /var/lib/apt/lists/*i

WORKDIR /opt

RUN wget http://www-eu.apache.org/dist/maven/maven-3/3.5.4/binaries/apache-maven-3.5.4-bin.tar.gz && \
    tar -xvf apache-maven-3.5.4-bin.tar.gz && \
    rm -rf apache-maven-3.5.4-bin.tar.gz

ENV JAVA_HOME /usr/lib/jvm/java-8-openjdk-amd64
ENV M2_HOME=/opt/apache-maven-3.5.4
ENV PATH=${M2_HOME}/bin:${PATH}

WORKDIR /opt/user
RUN mvn clean package
EXPOSE 8080

ENTRYPOINT ["/usr/bin/java","-Dspring.profiles.active=beta","-jar","user-service/target/user.jar"]

