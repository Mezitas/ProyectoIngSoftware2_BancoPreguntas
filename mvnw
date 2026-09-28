#!/bin/sh
MAVEN_PROJECTBASEDIR=$(CDPATH= cd -- "$(dirname -- "$0")" && pwd)
JAVA_MAJOR=$(java -version 2>&1 | sed -n '1s/.*version "\([0-9]*\).*/\1/p')
if [ -n "$JAVA_MAJOR" ] && [ "$JAVA_MAJOR" -ge 24 ]; then
  JAVA_NATIVE_ACCESS=--enable-native-access=ALL-UNNAMED
fi
exec java ${JAVA_NATIVE_ACCESS:+$JAVA_NATIVE_ACCESS} -Dmaven.multiModuleProjectDirectory="$MAVEN_PROJECTBASEDIR" \
  -classpath "$MAVEN_PROJECTBASEDIR/.mvn/wrapper/maven-wrapper.jar" \
  org.apache.maven.wrapper.MavenWrapperMain "$@"
