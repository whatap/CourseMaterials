@echo off

setlocal
for /f %%f in ('dir /b /on whatap.agent-*') do set last=%%f
set EXE_JAR=%last%
echo JAVA_HOME=%JAVA_HOME%
"%JAVA_HOME%\bin\java.exe" %JAVA_OPTS% -cp %EXE_JAR% whatap.agent.test.ResMon
endlocal