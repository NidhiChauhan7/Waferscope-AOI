@echo off
cd /d "%~dp0"
echo ========================================================
echo    Course 1: Programming Lab (2310214L) - DSA Demo
echo ========================================================
echo Compiling Programming Lab source files...
javac *.java
if errorlevel 1 (
    echo Compilation failed.
    pause
    exit /b 1
)
echo Running Programming Lab Demonstration...
java ProgrammingLabDemo
echo.
pause

