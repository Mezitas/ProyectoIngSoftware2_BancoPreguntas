@echo off
setlocal
set "MAVEN_PROJECTBASEDIR=%~dp0."
for /f "tokens=3" %%V in ('java -version 2^>^&1 ^| findstr /i "version"') do set "JAVA_VERSION=%%~V"
for /f "tokens=1 delims=." %%M in ("%JAVA_VERSION%") do set "JAVA_MAJOR=%%M"
if defined JAVA_MAJOR if %JAVA_MAJOR% GEQ 24 set "JAVA_NATIVE_ACCESS=--enable-native-access=ALL-UNNAMED"
if not exist "%MAVEN_PROJECTBASEDIR%\.mvn\wrapper\maven-wrapper.jar" (
  echo Maven Wrapper JAR is missing: %MAVEN_PROJECTBASEDIR%\.mvn\wrapper\maven-wrapper.jar
  exit /b 1
)
java %JAVA_NATIVE_ACCESS% -Dmaven.multiModuleProjectDirectory="%MAVEN_PROJECTBASEDIR%" -classpath "%MAVEN_PROJECTBASEDIR%\.mvn\wrapper\maven-wrapper.jar" org.apache.maven.wrapper.MavenWrapperMain %*
exit /b %ERRORLEVEL%
