#!/bin/bash

set -e

function download_deb_jar {
    DESTDIR=`pwd`/lib
    WORKDIR=`mktemp -d`
    pushd $WORKDIR
    apt download $1/stable
    dpkg-deb -x $1*.deb .
    cp -L $2 $DESTDIR
    popd
    rm -rf $WORKDIR
}

function download_metrics_lib {
    DESTDIR=`pwd`/lib
    WORKDIR=`mktemp -d`
    pushd $WORKDIR
    wget https://dist.torproject.org/metrics-lib/2.4.0/metrics-lib-2.4.0.tar.gz
    tar xf metrics-lib-2.4.0.tar.gz
    mv metrics-lib-2.4.0/generated/dist/signed/metrics-lib-2.4.0.jar $DESTDIR
    popd
    rm -rf $WORKDIR
}

rm -rf lib
mkdir lib
#download_deb_jar libcobertura-java usr/share/java/cobertura-2.1.1.jar
download_deb_jar libcommons-codec-java usr/share/java/commons-codec-1.10.jar
download_deb_jar libcommons-compress-java usr/share/java/commons-compress-1.13.jar
download_deb_jar libcommons-lang3-java usr/share/java/commons-lang3-3.5.jar
download_deb_jar libhamcrest-java usr/share/java/hamcrest-all-1.3.jar
download_deb_jar libjackson2-annotations-java usr/share/java/jackson-annotations-2.8.6.jar
download_deb_jar libjackson2-core-java usr/share/java/jackson-core-2.8.6.jar
download_deb_jar libjackson2-databind-java usr/share/java/jackson-databind-2.8.6.jar
download_deb_jar libjetty9-java usr/share/java/jetty9-continuation-9.2.21.v20170120.jar
download_deb_jar libjetty9-java usr/share/java/jetty9-http-9.2.21.v20170120.jar
download_deb_jar libjetty9-java usr/share/java/jetty9-io-9.2.21.v20170120.jar
download_deb_jar libjetty9-java usr/share/java/jetty9-security-9.2.21.v20170120.jar
download_deb_jar libjetty9-java usr/share/java/jetty9-server-9.2.21.v20170120.jar
download_deb_jar libjetty9-java usr/share/java/jetty9-servlet-9.2.21.v20170120.jar
download_deb_jar libjetty9-java usr/share/java/jetty9-servlets-9.2.21.v20170120.jar
download_deb_jar libjetty9-java usr/share/java/jetty9-util-9.2.21.v20170120.jar
download_deb_jar libjetty9-java usr/share/java/jetty9-webapp-9.2.21.v20170120.jar
download_deb_jar libjetty9-java usr/share/java/jetty9-xml-9.2.21.v20170120.jar
download_deb_jar junit4 usr/share/java/junit4-4.12.jar
download_deb_jar liblogback-java usr/share/java/logback-classic-1.1.9.jar
download_deb_jar liblogback-java usr/share/java/logback-core-1.1.9.jar
download_deb_jar liboro-java usr/share/java/oro-2.0.8.jar
download_deb_jar libservlet3.1-java usr/share/java/servlet-api-3.1.jar
download_deb_jar libslf4j-java usr/share/java/slf4j-api-1.7.22.jar
download_deb_jar libxz-java usr/share/java/xz-1.6.jar
download_deb_jar libxz-java usr/share/maven-repo/org/tukaani/xz/1.6/xz-1.6.jar
download_metrics_lib
