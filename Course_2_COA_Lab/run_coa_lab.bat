@echo off
cd /d "%~dp0"
echo ========================================================
echo    Course 2: COA Lab (2310221L) - Demonstration
echo ========================================================
echo Compiling COA Simulator...
javac *.java
if errorlevel 1 (
    echo Compilation failed.
    pause
    exit /b 1
)
echo Running COA Architecture Demonstration...
java CoaLabDemo
echo.
pause

