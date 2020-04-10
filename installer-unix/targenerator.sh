#!/bin/bash

#This file generates the tarball.
VERSION=1.0
FILE=JCAppStore-${VERSION}

if ls ../build/libs/${FILE}.jar > /dev/null  2>&1 ; then
    echo "Source binary found!" ;
else
    echo "Unknown source: build/libs/${FILE}.jar. Run jar gradle target to generate the binary before tar ball target." &&
    exit 1 ;
fi

rm -rf ${FILE}/ > /dev/null 2>&1
if ! mkdir ${FILE} > /dev/null  2>&1 ; then
    echo "Failed to create temp build folder." &&
    exit 1 ;
fi
if ! mkdir -p ${FILE}/src/main/ > /dev/null  2>&1 ; then
    echo "Failed to create resources build folder." &&
    exit 1 ;
fi

cp ../build/libs/${FILE}.jar ${FILE}/
cp launcher.sh ${FILE}/
cp store.asc ${FILE}/
chmod 0755 ${FILE}/${FILE}.jar
cp ../LICENSE ${FILE}/
cp -r ../src/main/resources/ ${FILE}/src/main/
tar -cvzf ${FILE}-unix.tar.gz ${FILE}/

rm -rf ../build/deploy-unix/ > /dev/null 2>&1
if ! mkdir ../build/deploy-unix/ > /dev/null 2>&1 ; then
    echo "Failed to create build folder." &&
    exit 1 ;
fi

mv ${FILE}-unix.tar.gz ../build/deploy-unix/
rm -rf ${FILE}/



