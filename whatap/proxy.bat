@echo off

setlocal
for /f %%f in ('dir /b /on *.proxy*') do set last=%%f
set EXE_JAR=%last%
echo JAVA_HOME=%JAVA_HOME%
echo JAVA_OPTS=%JAVA_OPTS%
echo EXE_JAR=%EXE_JAR%
"%JAVA_HOME%\bin\javaw.exe" %JAVA_OPTS% -jar %EXE_JAR%
endlocal