#!/bin/bash

#This file generates the tarball.
VERSION=1.3
FILE=JCAppStore-${VERSION}

ROOT=.;

if ls ${ROOT}/build/libs/${FILE}.jar > /dev/null  2>&1 ; then
    echo "Source binary found!" ;
else
    echo "Unknown source: ${ROOT}/build/libs/${FILE}.jar. Run jar gradle target to generate the binary before tar ball target." >&2 &&
    exit 1 ;
fi

rm -rf ${ROOT}/${FILE}/ > /dev/null 2>&1
if ! mkdir ${ROOT}/${FILE} > /dev/null  2>&1 ; then
    echo "Failed to create temp build folder." >&2 &&
    exit 1 ;
fi
if ! mkdir -p ${ROOT}/${FILE}/src/main/ > /dev/null  2>&1 ; then
    echo "Failed to create resources build folder." >&2 &&
    exit 1 ;
fi

cp ${ROOT}/build/libs/${FILE}.jar ${FILE}/
cp ${ROOT}/installer-unix/launcher.sh ${ROOT}/${FILE}/
cp ${ROOT}/installer-unix/store.asc ${ROOT}/${FILE}/
chmod 0755 ${ROOT}/${FILE}/${FILE}.jar
cp ${ROOT}/LICENSE ${ROOT}/${FILE}/
cp -r ${ROOT}/src/main/resources/ ${ROOT}/${FILE}/src/main/

cd ${ROOT}/
tar -cvzf ${FILE}-unix.tar.gz ${FILE}/
cd -

rm -rf ${ROOT}/build/deploy-unix/ > /dev/null 2>&1
if ! mkdir ${ROOT}/build/deploy-unix/ > /dev/null 2>&1 ; then
    echo "Failed to create build folder." >&2 &&
    exit 1 ;
fi

mv ${ROOT}/${FILE}-unix.tar.gz ${ROOT}/build/deploy-unix/
rm -rf ${ROOT}/${FILE}/

exit 0;



