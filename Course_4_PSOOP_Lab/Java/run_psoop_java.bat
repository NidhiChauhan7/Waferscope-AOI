@echo off
cd /d "%~dp0"
echo ========================================================
echo    Course 4: PSOOP Lab (Java - 2310262L) - Demonstration
echo ========================================================
echo Compiling Java OOP classes...
javac *.java
if errorlevel 1 (
    echo Compilation failed.
    pause
    exit /b 1
)
echo Running PSOOP Java Demonstration...
java PsoopJavaDemo
echo.
pause
