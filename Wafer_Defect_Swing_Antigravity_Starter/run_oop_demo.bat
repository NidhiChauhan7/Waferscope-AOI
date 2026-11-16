@echo off
cd /d "%~dp0\AI\Java_OOP"
echo ========================================================
echo    Java OOP Architecture Demonstration (For Faculty/Viva)
echo ========================================================
echo Compiling OOP classes...
javac *.java
if errorlevel 1 (
    echo Compilation failed.
    pause
    exit /b 1
)
echo Running OOP demonstration...
java Main
echo.
pause

