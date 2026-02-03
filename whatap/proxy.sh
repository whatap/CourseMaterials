#!/usr/bin/env bash
EXE_JAR=`ls *.proxy* | sort | tail -1`

echo JAVA_HOME=${JAVA_HOME}
${JAVA_HOME}/bin/java  ${JAVA_OPTS} -jar $EXE_JAR