@echo off 
setlocal
for /f %%f in ('dir /b /on whatap.agent-*') do set last=%%f
set EXE_JAR=%last%
echo JAVA_HOME=%JAVA_HOME%
"%JAVA_HOME%\bin\java.exe" -classpath %EXE_JAR% whatap.agent.setup.NetPing
endlocal