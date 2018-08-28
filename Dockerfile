FROM debian
RUN apt update && DEBIAN_FRONTEND=noninteractive apt -y install git openjdk-8-jdk ant checkstyle wget tree && apt-get clean && mkdir /root/onionoo
COPY ./ /root/onionoo/
RUN cd /root/onionoo && /root/onionoo/fetchlibs.sh
