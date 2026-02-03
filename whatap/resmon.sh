#!/usr/bin/env bash
EXE_JAR=`ls whatap.agent-* | sort | tail -1`

echo JAVA_HOME=${JAVA_HOME}
${JAVA_HOME}/bin/java  ${JAVA_OPTS} -cp $EXE_JAR whatap.agent.test.ResMon